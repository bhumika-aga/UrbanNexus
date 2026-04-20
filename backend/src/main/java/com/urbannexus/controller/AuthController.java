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

package com.urbannexus.controller;

import com.urbannexus.dto.AuthResponse;
import com.urbannexus.dto.LoginRequest;
import com.urbannexus.model.Resident;
import com.urbannexus.model.Technician;
import com.urbannexus.repository.ResidentRepository;
import com.urbannexus.repository.TechnicianRepository;
import com.urbannexus.security.UserPrincipal;
import com.urbannexus.service.AdminService;
import com.urbannexus.service.AuthService;
import com.urbannexus.service.ResidentService;
import com.urbannexus.service.TechnicianService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    private final ResidentRepository residentRepository;
    private final TechnicianRepository technicianRepository;
    private final ResidentService residentService;
    private final TechnicianService technicianService;
    private final AdminService adminService;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        try {
            AuthResponse response = authService.login(loginRequest);
            log.info("Login successful for user: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("Login failed for user: {}. Reason: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/profile/me")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }
        
        try {
            if ("Resident".equals(currentUser.getRole())) {
                if (currentUser.getResidentId() == null) {
                    log.warn("Authenticated Resident '{}' has no linked residentId", currentUser.getUsername());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No resident profile linked to this account"));
                }
                Resident r = residentRepository.findById(currentUser.getResidentId())
                                 .orElseThrow(() -> new RuntimeException("Resident record not found in database"));
                return ResponseEntity.ok(r);
            } else if ("Technician".equals(currentUser.getRole())) {
                if (currentUser.getTechId() == null) {
                    log.warn("Authenticated Technician '{}' has no linked techId", currentUser.getUsername());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "No technician profile linked to this account"));
                }
                Technician t = technicianRepository.findById(currentUser.getTechId())
                                   .orElseThrow(() -> new RuntimeException("Technician record not found in database"));
                return ResponseEntity.ok(t);
            } else if ("SuperAdmin".equals(currentUser.getRole())) {
                return ResponseEntity.ok(Map.of(
                    "username", currentUser.getUsername(),
                    "name", "System Administrator",
                    "role", "SuperAdmin"
                ));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Unsupported role for profile retrieval"));
        } catch (Exception e) {
            log.error("Internal error fetching profile for user '{}'", currentUser.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                       .body(Map.of("error", "Failed to fetch profile info: " + e.getMessage()));
        }
    }
    
    @PutMapping("/profile/update")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal UserPrincipal currentUser,
                                           @RequestBody Map<String, Object> updates) {
        try {
            if ("Resident".equals(currentUser.getRole())) {
                residentService.updateResidentProfile(currentUser.getResidentId(), updates);
                return ResponseEntity.ok(Map.of("message", "Profile updated successfully."));
            } else if ("Technician".equals(currentUser.getRole())) {
                technicianService.updateTechnicianProfile(currentUser.getTechId(), updates);
                return ResponseEntity.ok(Map.of("message", "Profile updated successfully."));
            } else if ("SuperAdmin".equals(currentUser.getRole())) {
                adminService.updateAdminProfile(currentUser.getId(), updates);
                return ResponseEntity.ok(Map.of("message", "Profile updated successfully."));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid user role."));
        } catch (Exception e) {
            log.error("Update profile failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                       .body(Map.of("error", "Failed to update profile: " + e.getMessage()));
        }
    }
}
