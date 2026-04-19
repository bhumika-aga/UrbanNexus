package com.urbannexus.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.urbannexus.security.UserPrincipal;
import com.urbannexus.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

        @Autowired
        private BookingService bookingService;

        @PostMapping("/technician")
        public ResponseEntity<?> bookTechnician(@AuthenticationPrincipal UserPrincipal currentUser,
                        @RequestBody Map<String, Object> payload) {
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
