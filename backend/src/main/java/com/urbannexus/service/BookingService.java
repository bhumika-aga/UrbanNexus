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

package com.urbannexus.service;

import com.urbannexus.model.*;
import com.urbannexus.model.Pricing.PricingCategory;
import com.urbannexus.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    // Booking slots offered by the UI: 1 = 09:00-12:00, 2 = 12:00-15:00, 3 = 15:00-18:00.
    static final int FIRST_SLOT = 1;
    static final int LAST_SLOT = 3;

    private final TechnicianRepository technicianRepository;
    private final TechnicianManagementRepository technicianManagementRepository;
    private final ResidentRepository residentRepository;
    private final PricingRepository pricingRepository;
    private final PaymentRepository paymentRepository;
    private final AmenityRepository amenityRepository;
    private final AmenityMgmtRepository amenityMgmtRepository;

    @Transactional
    public Map<String, Object> bookTechnician(Long residentId, String skill, Integer slot, String assignDateStr) {
        Resident resident = requireResident(residentId);
        String requestedSkill = Inputs.requireText(skill, "skill", 50);
        int requestedSlot = requireSlot(slot);
        LocalDate assignDate = requireDate(assignDateStr, "assign_date");

        // Locks every technician with this skill until commit, so a concurrent request for the same skill waits
        // here and then sees this booking. The unique (tech_id, assign_date, slot) constraint backs this up.
        Technician tech = technicianRepository.findBySkillForUpdate(requestedSkill).stream()
                              .filter(t -> Boolean.TRUE.equals(t.getAvailable()))
                              .filter(t -> !technicianManagementRepository
                                                .existsByTechnician_TechIdAndAssignDateAndSlot(t.getTechId(),
                                                    assignDate, requestedSlot))
                              .findFirst()
                              .orElseThrow(() -> Inputs.conflict(
                                  "Booking Failed: No " + requestedSkill + " is available for that date and slot."));

        Pricing pricing = pricingRepository.findByItemNameAndCategory(requestedSkill, PricingCategory.Technician)
                              .orElseThrow(() -> Inputs.badRequest("No pricing found for " + requestedSkill));

        String transNo = "TXN-TECH-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        Payment payment = new Payment();
        payment.setTransNo(transNo);
        payment.setStatus("Pending");
        payment.setType("Technician");
        payment.setCost(pricing.getBasePrice()); // @PrePersist applies 18% GST
        payment = paymentRepository.saveAndFlush(payment);

        TechnicianManagement assignment = new TechnicianManagement();
        assignment.setResident(resident);
        assignment.setTechnician(tech);
        assignment.setTransNo(transNo);
        assignment.setAssignDate(assignDate);
        assignment.setSlot(requestedSlot);
        assignment.setStatus("Assigned");
        technicianManagementRepository.save(assignment);

        return Map.of(
            "assignment_id", assignment.getAssignmentId(),
            "technician_name", tech.getName(),
            "trans_no", transNo,
            "total_with_gst", payment.getCost());
    }

    @Transactional
    public Map<String, Object> bookAmenity(Long residentId, Long amenityId, String dateStr, Integer slot,
                                           Integer capacityBooked) {
        Resident resident = requireResident(residentId);
        if (amenityId == null) {
            throw Inputs.badRequest("amenity_id is required.");
        }
        int requestedSlot = requireSlot(slot);
        LocalDate date = requireDate(dateStr, "date");
        if (capacityBooked == null || capacityBooked < 1) {
            throw Inputs.badRequest("capacity_booked must be at least 1.");
        }

        // Locks the amenity row until commit, so concurrent bookings for it are checked against the capacity one
        // at a time and each sees the places taken by the ones before it.
        Amenity amenity = amenityRepository.findByIdForUpdate(amenityId)
                              .orElseThrow(() -> Inputs.notFound("Amenity not found"));

        // A NULL capacity means the amenity has no headcount limit.
        if (amenity.getCapacity() != null) {
            long alreadyBooked = amenityMgmtRepository.sumBookedCapacity(amenityId, date, requestedSlot);
            long remaining = Math.max(0, amenity.getCapacity() - alreadyBooked);
            if (capacityBooked > remaining) {
                throw Inputs.conflict(remaining == 0
                                          ? amenity.getName() + " is fully booked for that slot."
                                          : "Only " + remaining + " place(s) left at " + amenity.getName()
                                                + " for that slot.");
            }
        }

        Pricing pricing = pricingRepository
                              .findByItemNameAndCategory(amenity.getName(), PricingCategory.Amenity)
                              .orElseThrow(() -> Inputs.badRequest("No pricing found for " + amenity.getName()));

        String transNo = "TXN-AMEN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        Payment payment = new Payment();
        payment.setTransNo(transNo);
        payment.setStatus("Pending");
        payment.setType("Amenity");
        payment.setCost(pricing.getBasePrice()); // @PrePersist applies 18% GST
        payment = paymentRepository.saveAndFlush(payment);

        AmenityMgmt booking = new AmenityMgmt();
        booking.setResident(resident);
        booking.setAmenity(amenity);
        booking.setTransNo(transNo);
        booking.setDate(date);
        booking.setSlot(requestedSlot);
        booking.setCapacityBooked(capacityBooked);
        booking.setStatus("Confirmed");
        amenityMgmtRepository.save(booking);

        return Map.of(
            "booking_id", booking.getBookingId(),
            "amenity_name", amenity.getName(),
            "trans_no", transNo,
            "total_with_gst", payment.getCost());
    }

    private Resident requireResident(Long residentId) {
        if (residentId == null) {
            throw Inputs.badRequest("resident_id is required.");
        }
        return residentRepository.findById(residentId)
                   .orElseThrow(() -> Inputs.notFound("Resident not found"));
    }

    private static int requireSlot(Integer slot) {
        if (slot == null || slot < FIRST_SLOT || slot > LAST_SLOT) {
            throw Inputs.badRequest("slot must be between " + FIRST_SLOT + " and " + LAST_SLOT + ".");
        }
        return slot;
    }

    private static LocalDate requireDate(String value, String field) {
        if (value == null || value.isBlank()) {
            throw Inputs.badRequest(field + " is required.");
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw Inputs.badRequest(field + " must be a date in YYYY-MM-DD format.");
        }
    }
}
