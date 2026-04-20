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
import com.urbannexus.repository.BookingRepository;
import com.urbannexus.repository.PaymentRepository;
import com.urbannexus.repository.PricingRepository;
import com.urbannexus.repository.ResidentRepository;
import com.urbannexus.repository.TechnicianManagementRepository;
import com.urbannexus.repository.TechnicianRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

        private final BookingRepository bookingRepository;
        private final TechnicianRepository technicianRepository;
        private final TechnicianManagementRepository technicianManagementRepository;
        private final ResidentRepository residentRepository;
        private final PricingRepository pricingRepository;
        private final PaymentRepository paymentRepository;
        private final AmenityRepository amenityRepository;
        private final AmenityMgmtRepository amenityMgmtRepository;

        /**
         * Entry Point: Calls the Database Procedure (MySQL proc or H2 Alias)
         */
        @Transactional
        public Map<String, Object> bookTechnician(Long residentId, String skill, Integer slot, String assignDate) {
                return bookingRepository.autoBookTechnician(residentId, skill, slot, assignDate);
        }

        @Transactional
        public Map<String, Object> bookAmenity(Long residentId, Long amenityId, String date, Integer slot,
                        Integer capacityBooked) {
                return bookingRepository.autoBookAmenity(residentId, amenityId, date, slot, capacityBooked);
        }

        /**
         * H2 Internal Logic: Executed via ALIAS
         */
        @Transactional
        public Map<String, Object> executeTechnicianBooking(Long residentId, String skill, Integer slot,
                        String assignDateStr) {
                LocalDate assignDate = LocalDate.parse(assignDateStr);

                Technician tech = technicianRepository.findBySkill(skill).stream()
                                .filter(Technician::getAvailable)
                                .filter(t -> !technicianManagementRepository
                                                .existsByTechnician_TechIdAndAssignDateAndSlot(t.getTechId(),
                                                                assignDate, slot))
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("Booking Failed: No available technicians."));

                Pricing pricing = pricingRepository.findByItemNameAndCategory(skill, PricingCategory.Technician)
                                .orElseThrow(() -> new RuntimeException("No pricing found for " + skill));

                String transNo = "TXN-TECH-" + RandomGenerator.getDefault().nextInt(100000, 999999);
                Payment payment = new Payment();
                payment.setTransNo(transNo);
                payment.setStatus("Pending");
                payment.setType("Technician");
                payment.setCost(pricing.getBasePrice());
                paymentRepository.save(payment);

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
                                "total_with_gst", payment.getCost());
        }

        @Transactional
        public Map<String, Object> executeAmenityBooking(Long residentId, Long amenityId, String dateStr, Integer slot,
                        Integer capacityBooked) {
                LocalDate date = LocalDate.parse(dateStr);

                Amenity amenity = amenityRepository.findById(amenityId)
                                .orElseThrow(() -> new RuntimeException("Amenity not found"));

                if (capacityBooked > amenity.getCapacity()) {
                        throw new RuntimeException("Capacity exceeded.");
                }

                Pricing pricing = pricingRepository
                                .findByItemNameAndCategory(amenity.getName(), PricingCategory.Amenity)
                                .orElseThrow(() -> new RuntimeException("No pricing found for " + amenity.getName()));

                String transNo = "TXN-AMEN-" + RandomGenerator.getDefault().nextInt(100000, 999999);
                Payment payment = new Payment();
                payment.setTransNo(transNo);
                payment.setStatus("Pending");
                payment.setType("Amenity");
                payment.setCost(pricing.getBasePrice());
                paymentRepository.save(payment);

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
