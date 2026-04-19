package com.urbannexus.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.urbannexus.security.UserPrincipal;
import com.urbannexus.service.AmenityService;

@RestController
@RequestMapping("/api/amenities")
public class AmenityController {

    @Autowired
    private AmenityService amenityService;

    @PostMapping
    public ResponseEntity<?> addAmenity(@AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody Map<String, Object> payload) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. SuperAdmin clearance required."));
        }

        try {
            Long amenityId = payload.get("amenity_id") != null ? Long.parseLong(payload.get("amenity_id").toString())
                    : null;
            String name = (String) payload.get("name");
            Integer capacity = payload.get("capacity") != null ? Integer.parseInt(payload.get("capacity").toString())
                    : null;

            amenityService.addAmenity(amenityId, name, capacity);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Amenity added successfully!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add amenity"));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAmenities() {
        try {
            return ResponseEntity.ok(amenityService.getAllAmenities());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve facilities."));
        }
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public ResponseEntity<?> updateAmenity(@AuthenticationPrincipal UserPrincipal currentUser,
            @org.springframework.web.bind.annotation.PathVariable Long id, @RequestBody Map<String, Object> payload) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied."));
        }

        try {
            String name = (String) payload.get("name");
            Integer capacity = payload.get("capacity") != null ? Integer.parseInt(payload.get("capacity").toString())
                    : null;

            amenityService.updateAmenity(id, name, capacity);
            return ResponseEntity.ok(Map.of("message", "Amenity updated successfully."));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Amenity not found.")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update amenity."));
        }
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAmenity(@AuthenticationPrincipal UserPrincipal currentUser,
            @org.springframework.web.bind.annotation.PathVariable Long id) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied."));
        }

        try {
            amenityService.deleteAmenity(id);
            return ResponseEntity.ok(Map.of("message", "Amenity deleted successfully."));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Amenity not found.")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete amenity."));
        }
    }
}
