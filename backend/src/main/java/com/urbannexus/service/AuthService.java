package com.urbannexus.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.urbannexus.dto.AuthResponse;
import com.urbannexus.dto.LoginRequest;
import com.urbannexus.model.Admin;
import com.urbannexus.repository.AdminRepository;
import com.urbannexus.security.JwtTokenProvider;
import com.urbannexus.security.UserPrincipal;

@Service
public class AuthService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public AuthResponse login(LoginRequest request) {
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

        return new AuthResponse("Login successful!", token, new AuthResponse.AdminInfo(
                principal.getUsername(), principal.getRole()));
    }
}
