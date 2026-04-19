package com.urbannexus.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.urbannexus.dto.AuthResponse;
import com.urbannexus.dto.LoginRequest;
import com.urbannexus.security.UserPrincipal;
import com.urbannexus.service.AuthService;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile/me")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        try {
            if (currentUser.getResidentId() != null) {
                Map<String, Object> data = jdbcTemplate.queryForMap(
                        "SELECT name, contact FROM resident WHERE resident_id = ?", currentUser.getResidentId());
                return ResponseEntity.ok(data);
            } else if (currentUser.getTechId() != null) {
                Map<String, Object> data = jdbcTemplate.queryForMap(
                        "SELECT name, contact FROM technician WHERE tech_id = ?", currentUser.getTechId());
                return ResponseEntity.ok(data);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch profile info"));
        }
    }
}
