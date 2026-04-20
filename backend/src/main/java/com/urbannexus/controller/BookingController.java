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
import com.urbannexus.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    
    private final BookingService bookingService;
    
    @PostMapping("/technician")
    public ResponseEntity<?> bookTechnician(@AuthenticationPrincipal UserPrincipal currentUser,
                                            @RequestBody Map<String, Object> payload) {
        log.info("Booking request for technician by user: {}", currentUser.getUsername());
        try {
            Long residentId = "Resident".equals(currentUser.getRole()) ? currentUser.getResidentId()
                                  : (payload.get("resident_id") != null
                                     ? Long.parseLong(payload.get("resident_id").toString())
                                     : null);
            
            String skill = (String) payload.get("skill");
            Integer slot = payload.get("slot") != null ? Integer.parseInt(payload.get("slot").toString())
                               : null;
            String assignDate = (String) payload.get("assign_date");
            
            return ResponseEntity.status(HttpStatus.CREATED)
                       .body(bookingService.bookTechnician(residentId, skill, slot, assignDate));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                       .body(Map.of("error", "Booking failed"));
        }
    }
    
    @PostMapping("/amenity")
    public ResponseEntity<?> bookAmenity(@AuthenticationPrincipal UserPrincipal currentUser,
                                         @RequestBody Map<String, Object> payload) {
        if (!"Resident".equals(currentUser.getRole()) && !"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(Map.of("error", "Only Residents or Admins can create bookings."));
        }
        
        try {
            Long residentId = "Resident".equals(currentUser.getRole()) ? currentUser.getResidentId()
                                  : (payload.get("resident_id") != null
                                     ? Long.parseLong(payload.get("resident_id").toString())
                                     : null);
            
            Long amenityId = payload.get("amenity_id") != null
                                 ? Long.parseLong(payload.get("amenity_id").toString())
                                 : null;
            String date = (String) payload.get("date");
            Integer slot = payload.get("slot") != null ? Integer.parseInt(payload.get("slot").toString())
                               : null;
            Integer capacityBooked = payload.get("capacity_booked") != null
                                         ? Integer.parseInt(payload.get("capacity_booked").toString())
                                         : null;
            
            return ResponseEntity.status(HttpStatus.CREATED)
                       .body(bookingService.bookAmenity(residentId, amenityId, date, slot,
                           capacityBooked));
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null && e.getMessage().contains("45000") ? e.getMessage()
                                  : "Failed to book amenity.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", errorMsg));
        }
    }
}
