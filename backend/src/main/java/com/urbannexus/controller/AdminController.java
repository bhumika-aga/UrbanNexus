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

import java.util.Map;

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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

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
        if (!"SuperAdmin".equals(currentUser.getRole()) && !"Resident".equals(currentUser.getRole())) {
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

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied."));
        }
        try {
            return ResponseEntity.ok(adminService.getDashboardStats());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch dashboard statistics."));
        }
    }

    @GetMapping("/assignments")
    public ResponseEntity<?> getAllAssignments(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied."));
        }
        return ResponseEntity.ok(adminService.getAllAssignments());
    }

    @GetMapping("/amenities/bookings")
    public ResponseEntity<?> getAllAmenityBookings(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied."));
        }
        return ResponseEntity.ok(adminService.getAllAmenityBookings());
    }

    @GetMapping("/technicians/bookings")
    public ResponseEntity<?> getAllTechnicianBookings(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied."));
        }
        return ResponseEntity.ok(adminService.getAllTechnicianBookings());
    }
}
