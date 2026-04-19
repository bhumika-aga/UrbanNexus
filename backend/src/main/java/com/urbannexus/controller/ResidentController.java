package com.urbannexus.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.urbannexus.security.UserPrincipal;
import com.urbannexus.service.ResidentService;

@RestController
@RequestMapping("/api/residents")
public class ResidentController {

    @Autowired
    private ResidentService residentService;

    @PostMapping
    public ResponseEntity<?> addResident(@AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody Map<String, Object> payload) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied."));
        }

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
