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

import java.time.LocalDate;
import java.util.Map;
import java.util.random.RandomGenerator;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.urbannexus.model.Amenity;
import com.urbannexus.model.AmenityMgmt;
import com.urbannexus.model.Payment;
import com.urbannexus.model.Pricing;
import com.urbannexus.model.Pricing.PricingCategory;
import com.urbannexus.model.Resident;
import com.urbannexus.model.Technician;
import com.urbannexus.model.TechnicianManagement;
import com.urbannexus.repository.AmenityMgmtRepository;
import com.urbannexus.repository.AmenityRepository;
import com.urbannexus.repository.PaymentRepository;
import com.urbannexus.repository.PricingRepository;
import com.urbannexus.repository.ResidentRepository;
import com.urbannexus.repository.TechnicianManagementRepository;
import com.urbannexus.repository.TechnicianRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final TechnicianRepository technicianRepository;
    private final TechnicianManagementRepository technicianManagementRepository;
    private final ResidentRepository residentRepository;
    private final PricingRepository pricingRepository;
    private final PaymentRepository paymentRepository;
    private final AmenityRepository amenityRepository;
    private final AmenityMgmtRepository amenityMgmtRepository;

    @Transactional
    public Map<String, Object> bookTechnician(Long residentId, String skill, Integer slot, String assignDateStr) {
        LocalDate assignDate = LocalDate.parse(assignDateStr);

        // 1. Find Available Technician
        Technician tech = technicianRepository.findBySkill(skill).stream()
                .filter(t -> !technicianManagementRepository
                        .existsByTechnician_TechIdAndAssignDateAndSlot(t.getTechId(), assignDate, slot))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Booking Failed: No available technicians with that skill for the requested date and slot."));

        // 2. Fetch Pricing
        Pricing pricing = pricingRepository.findByItemNameAndCategory(skill, PricingCategory.Technician)
                .orElseThrow(() -> new RuntimeException("Booking Failed: No pricing found for skill: " + skill));

        // 3. Create Payment (GST auto-calculated in @PrePersist)
        String transNo = "TXN-TECH-" + RandomGenerator.getDefault().nextInt(100000, 999999);
        Payment payment = new Payment();
        payment.setTransNo(transNo);
        payment.setStatus("Pending");
        payment.setType("Technician");
        payment.setCost(pricing.getBasePrice());
        paymentRepository.save(payment);

        // 4. Create Assignment
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new RuntimeException("Resident not found"));

        TechnicianManagement assignment = new TechnicianManagement();
        assignment.setResident(resident);
        assignment.setTechnician(tech);
        assignment.setTransNo(transNo);
        assignment.setAssignDate(assignDate);
        assignment.setSlot(slot);
        assignment.setStatus("Assigned");
        technicianManagementRepository.save(assignment);

        return Map.of(
                "assignment_id", assignment.getAssignmentId(),
                "technician_name", tech.getName(),
                "trans_no", transNo,
                "total_with_gst", payment.getCost() // This will be the original cost until after commit, but that's
                                                    // fine for the immediate response
        );
    }

    @Transactional
    public Map<String, Object> bookAmenity(Long residentId, Long amenityId, String dateStr, Integer slot,
            Integer capacityBooked) {
        LocalDate date = LocalDate.parse(dateStr);

        // 1. Validate Amenity & Capacity
        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new RuntimeException("Amenity not found"));

        if (capacityBooked > amenity.getCapacity()) {
            throw new RuntimeException("Booking failed: Capacity requested exceeds max capacity.");
        }

        // 2. Fetch Pricing
        Pricing pricing = pricingRepository.findByItemNameAndCategory(amenity.getName(), PricingCategory.Amenity)
                .orElseThrow(() -> new RuntimeException(
                        "Booking Failed: No pricing found for amenity: " + amenity.getName()));

        // 3. Create Payment
        String transNo = "TXN-AMEN-" + RandomGenerator.getDefault().nextInt(100000, 999999);
        Payment payment = new Payment();
        payment.setTransNo(transNo);
        payment.setStatus("Pending");
        payment.setType("Amenity");
        payment.setCost(pricing.getBasePrice());
        paymentRepository.save(payment);

        // 4. Create Booking
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new RuntimeException("Resident not found"));

        AmenityMgmt booking = new AmenityMgmt();
        booking.setResident(resident);
        booking.setAmenity(amenity);
        booking.setTransNo(transNo);
        booking.setDate(date);
        booking.setSlot(slot);
        booking.setCapacityBooked(capacityBooked);
        booking.setStatus("Confirmed");
        amenityMgmtRepository.save(booking);

        return Map.of(
                "booking_id", booking.getBookingId(),
                "amenity_name", amenity.getName(),
                "trans_no", transNo,
                "total_with_gst", payment.getCost());
    }
}
