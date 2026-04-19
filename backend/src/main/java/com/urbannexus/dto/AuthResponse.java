package com.urbannexus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String message;
    private String token;
    private AdminInfo admin;

    @Data
    @AllArgsConstructor
    public static class AdminInfo {
        private String username;
        private String role;
    }
}
