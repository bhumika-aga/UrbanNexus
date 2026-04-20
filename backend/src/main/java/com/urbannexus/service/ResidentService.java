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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.urbannexus.model.Admin;
import com.urbannexus.model.Resident;
import com.urbannexus.repository.AdminRepository;
import com.urbannexus.repository.AmenityMgmtRepository;
import com.urbannexus.repository.PaymentRepository;
import com.urbannexus.repository.ResidentRepository;
import com.urbannexus.repository.TechnicianManagementRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final PaymentRepository paymentRepository;
    private final AmenityMgmtRepository amenityMgmtRepository;
    private final TechnicianManagementRepository technicianManagementRepository;

    @Transactional
    public Map<String, Object> addResident(String name, String houseBlock, String houseFloor, String houseUnit,
            String ownershipStatus, String contact, Integer noOfMembers) {
        log.info("Adding new resident: {} in block: {}", name, houseBlock);
        Resident resident = new Resident();
        resident.setName(name);
        resident.setHouseBlock(houseBlock);
        resident.setHouseFloor(houseFloor);
        resident.setHouseUnit(houseUnit);
        resident.setOwnershipStatus(ownershipStatus);
        resident.setContact(contact);
        resident.setNoOfMembers(noOfMembers);

        resident = residentRepository.save(resident);
        Long newResidentId = resident.getResidentId();

        String defaultPassword = "pwd123#";
        String hashedPassword = passwordEncoder.encode(defaultPassword);
        String username = name.toLowerCase().replaceAll("\\s+", "_") + newResidentId;

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPasswordHash(hashedPassword);
        admin.setRole("Resident");
        admin.setResident(resident);

        adminRepository.save(admin);
        log.info("Resident created successfully inside database with generated username: {}", username);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Resident and Login created!");
        response.put("username", username);
        response.put("password", defaultPassword);
        return response;
    }

    public Map<String, Object> getPendingDues(Long residentId) {
        List<Map<String, Object>> dues = paymentRepository.findPendingDuesByResidentId(residentId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Your pending dues retrieved successfully.");
        response.put("total_unpaid_invoices", dues.size());
        response.put("invoices", dues);
        return response;
    }

    public Map<String, Object> getBookings(Long residentId) {
        List<Map<String, Object>> amenities = amenityMgmtRepository.getAmenityBookingsForResident(residentId);
        List<Map<String, Object>> technicians = technicianManagementRepository.findBookingsByResidentId(residentId);

        Map<String, Object> response = new HashMap<>();
        response.put("amenities", amenities);
        response.put("technicians", technicians);
        return response;
    }

    @Transactional
    public void deleteResident(Long residentId) {
        Optional<Resident> resident = residentRepository.findById(residentId);
        if (resident.isEmpty()) {
            throw new RuntimeException("Resident not found.");
        }
        residentRepository.delete(resident.get());
    }
}
