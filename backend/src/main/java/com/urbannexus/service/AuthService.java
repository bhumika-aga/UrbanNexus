/*
 * Copyright (c) 2026 Bhumika Agarwal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.urbannexus.service;

import com.urbannexus.dto.AuthResponse;
import com.urbannexus.dto.LoginRequest;
import com.urbannexus.exception.LoginLockedException;
import com.urbannexus.model.Admin;
import com.urbannexus.repository.AdminRepository;
import com.urbannexus.security.JwtTokenProvider;
import com.urbannexus.security.LoginAttemptService;
import com.urbannexus.security.TemporaryPasswordGenerator;
import com.urbannexus.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private static final String INVALID_CREDENTIALS = "Invalid username or password";
    
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final LoginAttemptService loginAttemptService;
    private final TemporaryPasswordGenerator temporaryPasswordGenerator;
    
    // Compared against when the username does not exist, so an unknown username takes as long to reject as a wrong
    // password and response timing does not reveal which usernames are real.
    private volatile String unknownUserHash;
    
    private static AuthResponse.AdminInfo adminInfo(Admin admin) {
        return new AuthResponse.AdminInfo(admin.getUsername(), admin.getRole(),
            Boolean.TRUE.equals(admin.getMustChangePassword()));
    }
    
    public AuthResponse login(LoginRequest request) {
        String username = request.getUsername();
        
        Duration lock = loginAttemptService.remainingLock(username);
        if (!lock.isZero()) {
            long minutes = Math.max(1, (lock.getSeconds() + 59) / 60);
            throw new LoginLockedException(
                "Too many failed login attempts. Try again in " + minutes + " minute(s).");
        }
        
        if (!StringUtils.hasText(username) || request.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
        }
        
        Optional<Admin> adminOpt = adminRepository.findByUsername(username);
        String hash = adminOpt.map(Admin::getPasswordHash).orElseGet(this::unknownUserHash);
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), hash);
        if (adminOpt.isEmpty() || !passwordMatches) {
            loginAttemptService.recordFailure(username);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
        }
        loginAttemptService.recordSuccess(username);
        
        Admin admin = adminOpt.get();
        String token = issueToken(admin);
        log.info("Successfully generated JWT token for user: {} with role: {}", admin.getUsername(), admin.getRole());
        
        return new AuthResponse("Login successful!", token, adminInfo(admin));
    }
    
    /**
     * Changes the password, clears any must-change flag, and revokes every token issued before the change. The
     * response carries a fresh token so the caller stays signed in.
     */
    @Transactional
    public AuthResponse changePassword(Long adminId, String currentPassword, String newPassword) {
        Admin admin = adminRepository.findById(adminId)
                          .orElseThrow(() -> Inputs.notFound("Account not found."));
        if (!passwordEncoder.matches(currentPassword, admin.getPasswordHash())) {
            throw Inputs.badRequest("Current password is incorrect.");
        }
        if (passwordEncoder.matches(newPassword, admin.getPasswordHash())) {
            throw Inputs.badRequest("New password must be different from the current password.");
        }
        admin.setPasswordHash(passwordEncoder.encode(newPassword));
        admin.setMustChangePassword(false);
        admin.setTokenVersion(admin.getTokenVersion() + 1);
        adminRepository.save(admin);
        log.info("Password changed for adminId: {}", adminId);
        
        return new AuthResponse("Password updated successfully.", issueToken(admin), adminInfo(admin));
    }
    
    /**
     * Gives the account a new random one-time password that must be changed at next login, revokes its existing
     * tokens, and lifts any login lockout. Returns the plaintext password; it is not stored and cannot be retrieved
     * again.
     */
    @Transactional
    public String issueTemporaryPassword(Admin admin) {
        String password = temporaryPasswordGenerator.generate();
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setMustChangePassword(true);
        admin.setTokenVersion(admin.getTokenVersion() + 1);
        adminRepository.save(admin);
        loginAttemptService.recordSuccess(admin.getUsername());
        return password;
    }
    
    private String issueToken(Admin admin) {
        Long residentId = admin.getResident() != null ? admin.getResident().getResidentId() : null;
        Long techId = admin.getTechnician() != null ? admin.getTechnician().getTechId() : null;
        
        UserPrincipal principal = new UserPrincipal(
            admin.getAdminId(),
            admin.getUsername(),
            admin.getPasswordHash(),
            admin.getRole(),
            residentId,
            techId,
            admin.getTokenVersion(),
            Boolean.TRUE.equals(admin.getMustChangePassword()));
        
        return tokenProvider.generateToken(principal);
    }
    
    private String unknownUserHash() {
        if (unknownUserHash == null) {
            unknownUserHash = passwordEncoder.encode(UUID.randomUUID().toString());
        }
        return unknownUserHash;
    }
}
