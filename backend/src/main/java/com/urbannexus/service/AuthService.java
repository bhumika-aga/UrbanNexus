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
import com.urbannexus.model.Admin;
import com.urbannexus.repository.AdminRepository;
import com.urbannexus.security.JwtTokenProvider;
import com.urbannexus.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    
    public AuthResponse login(LoginRequest request) {
        log.debug("Attempting to find admin with username: {}", request.getUsername());
        Optional<Admin> adminOpt = adminRepository.findByUsername(request.getUsername());
        
        if (adminOpt.isEmpty()) {
            throw new RuntimeException("Invalid username or password");
        }
        
        Admin admin = adminOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }
        
        Long residentId = admin.getResident() != null ? admin.getResident().getResidentId() : null;
        Long techId = admin.getTechnician() != null ? admin.getTechnician().getTechId() : null;
        
        UserPrincipal principal = new UserPrincipal(
            admin.getAdminId(),
            admin.getUsername(),
            admin.getPasswordHash(),
            admin.getRole(),
            residentId,
            techId);
        
        String token = tokenProvider.generateToken(principal);
        log.info("Successfully generated JWT token for user: {} with role: {}", principal.getUsername(),
            principal.getRole());
        
        return new AuthResponse("Login successful!", token, new AuthResponse.AdminInfo(
            principal.getUsername(), principal.getRole()));
    }
}
