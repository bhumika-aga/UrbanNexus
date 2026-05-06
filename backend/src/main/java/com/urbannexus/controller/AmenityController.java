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
import com.urbannexus.service.AmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/amenities")
@RequiredArgsConstructor
public class AmenityController {
    
    private final AmenityService amenityService;
    
    @PostMapping
    public ResponseEntity<?> addAmenity(@AuthenticationPrincipal UserPrincipal currentUser,
                                        @RequestBody Map<String, Object> payload) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(Map.of("error", "Access denied. SuperAdmin clearance required."));
        }
        
        try {
            if (payload.get("amenity_id") == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "amenity_id is required."));
            }
            Long amenityId = Long.parseLong(payload.get("amenity_id").toString());
            String name = (String) payload.get("name");
            Integer capacity = payload.get("capacity") != null ? Integer.parseInt(payload.get("capacity").toString())
                                   : null;
            
            amenityService.addAmenity(amenityId, name, capacity);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Amenity added successfully!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
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
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAmenity(@AuthenticationPrincipal UserPrincipal currentUser,
                                           @PathVariable Long id, @RequestBody Map<String, Object> payload) {
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
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAmenity(@AuthenticationPrincipal UserPrincipal currentUser,
                                           @PathVariable Long id) {
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
