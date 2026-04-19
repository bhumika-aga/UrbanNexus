package com.urbannexus.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.urbannexus.security.UserPrincipal;
import com.urbannexus.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/residents/search")
    public ResponseEntity<?> searchResidents(@AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String block,
            @RequestParam(required = false) String floor,
            @RequestParam(required = false) String unit) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. SuperAdmin clearance required."));
        }

        try {
            return ResponseEntity.ok(adminService.searchResidents(name, block, floor, unit));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to search residents."));
        }
    }

    @GetMapping("/technicians")
    public ResponseEntity<?> getTechnicians(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied."));
        }
        try {
            return ResponseEntity.ok(adminService.getTechnicians());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch technicians"));
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getTransactions(@AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String block,
            @RequestParam(required = false) String resident_name) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. SuperAdmin clearance required."));
        }

        try {
            return ResponseEntity.ok(adminService.searchTransactions(status, block, resident_name));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to search transactions."));
        }
    }

    @PostMapping("/process-overdue")
    public ResponseEntity<?> processOverdue(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. SuperAdmin clearance required."));
        }

        try {
            adminService.processOverduePayments();
            return ResponseEntity
                    .ok(Map.of("message", "Successfully ran the cursor. Pending payments are now marked as Overdue."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process overdue payments."));
        }
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<?> getAuditLogs(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. SuperAdmin clearance required."));
        }

        try {
            return ResponseEntity.ok(adminService.getAuditLogs());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch audit logs."));
        }
    }
}
