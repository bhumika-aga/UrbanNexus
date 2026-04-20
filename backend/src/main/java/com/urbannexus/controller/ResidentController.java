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

import com.urbannexus.security.UserPrincipal;
import com.urbannexus.service.ResidentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/residents")
@RequiredArgsConstructor
public class ResidentController {
    
    private final ResidentService residentService;
    
    @PostMapping
    public ResponseEntity<?> addResident(@AuthenticationPrincipal UserPrincipal currentUser,
                                         @RequestBody Map<String, Object> payload) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            log.warn("Unauthorized addResident attempt by user: {}", currentUser.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied."));
        }
        
        log.info("Processing addResident request by SuperAdmin for resident: {}", payload.get("name"));
        try {
            String name = (String) payload.get("name");
            String houseBlock = (String) payload.get("house_block");
            String houseFloor = (String) payload.get("house_floor");
            String houseUnit = (String) payload.get("house_unit");
            String ownershipStatus = (String) payload.get("ownership_status");
            String contact = (String) payload.get("contact");
            Integer noOfMembers = payload.get("no_of_members") != null
                                      ? ((Number) payload.get("no_of_members")).intValue()
                                      : null;
            
            Map<String, Object> res = residentService.addResident(name, houseBlock, houseFloor, houseUnit,
                ownershipStatus, contact, noOfMembers);
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                       .body(Map.of("error", "Failed to create resident account"));
        }
    }
    
    @GetMapping("/me/dues")
    public ResponseEntity<?> getPendingDues(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (!"Resident".equals(currentUser.getRole()) || currentUser.getResidentId() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(Map.of("error", "Access denied. You must be logged in as a Resident to view your dues."));
        }
        
        try {
            return ResponseEntity.ok(residentService.getPendingDues(currentUser.getResidentId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                       .body(Map.of("error", "Failed to fetch pending dues."));
        }
    }
    
    @GetMapping("/me/bookings")
    public ResponseEntity<?> getMyBookings(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (!"Resident".equals(currentUser.getRole()) || currentUser.getResidentId() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(Map.of("error", "Access denied. Must be a Resident."));
        }
        
        try {
            return ResponseEntity.ok(residentService.getBookings(currentUser.getResidentId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                       .body(Map.of("error", "Failed to fetch booking history."));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResident(@AuthenticationPrincipal UserPrincipal currentUser, @PathVariable Long id) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(Map.of("error", "Only SuperAdmin can remove drivers from the grid."));
        }
        
        try {
            residentService.deleteResident(id);
            return ResponseEntity.ok(Map.of("message", "Resident and all associated records deleted successfully."));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Resident not found.")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Deletion failed."));
        }
    }
}
