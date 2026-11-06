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

import com.urbannexus.exception.UnpaidInvoicesException;
import com.urbannexus.model.Admin;
import com.urbannexus.model.Resident;
import com.urbannexus.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResidentService {
    
    private final ResidentRepository residentRepository;
    private final AdminRepository adminRepository;
    private final AuthService authService;
    private final PaymentRepository paymentRepository;
    private final AmenityMgmtRepository amenityMgmtRepository;
    private final TechnicianManagementRepository technicianManagementRepository;
    private final AuditService auditService;
    private final UsernameGenerator usernameGenerator;
    
    @Transactional
    public Map<String, Object> addResident(String name, String houseBlock, String houseFloor, String houseUnit,
                                           String ownershipStatus, String contact, Integer noOfMembers) {
        // Lengths match the resident table's columns.
        String residentName = Inputs.requireText(name, "name", 100);
        Resident resident = new Resident();
        resident.setName(residentName);
        resident.setHouseBlock(Inputs.requireText(houseBlock, "house_block", 10));
        resident.setHouseFloor(Inputs.requireText(houseFloor, "house_floor", 10));
        resident.setHouseUnit(Inputs.requireText(houseUnit, "house_unit", 10));
        resident.setOwnershipStatus(Inputs.requireText(ownershipStatus, "ownership_status", 50));
        resident.setContact(Inputs.requireText(contact, "contact", 20));
        if (noOfMembers != null && noOfMembers < 1) {
            throw Inputs.badRequest("no_of_members must be at least 1.");
        }
        resident.setNoOfMembers(noOfMembers);
        log.info("Adding new resident: {} in block: {}", residentName, resident.getHouseBlock());
        
        resident = residentRepository.save(resident);
        Long newResidentId = resident.getResidentId();
        
        String username = usernameGenerator.generate(residentName, newResidentId);
        
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setRole("Resident");
        admin.setResident(resident);
        String temporaryPassword = authService.issueTemporaryPassword(admin);
        log.info("Resident created successfully inside database with generated username: {}", username);
        
        auditService.log("resident", newResidentId.toString(), "ADD", "Added new resident: " + residentName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Resident and Login created!");
        response.put("username", username);
        response.put("password", temporaryPassword);
        return response;
    }
    
    /**
     * Replaces the resident's password with a new one-time password and signs them out everywhere.
     */
    @Transactional
    public Map<String, Object> resetCredentials(Long residentId) {
        Admin admin = adminRepository.findByResident_ResidentId(residentId)
                          .orElseThrow(() -> Inputs.notFound("Resident not found."));
        String temporaryPassword = authService.issueTemporaryPassword(admin);
        auditService.log("resident", residentId.toString(), "UPDATE", "Reset login password for: " + admin.getUsername());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password reset. Share the new temporary password with the resident.");
        response.put("username", admin.getUsername());
        response.put("password", temporaryPassword);
        return response;
    }
    
    public Map<String, Object> getPendingDues(Long residentId) {
        List<Map<String, Object>> dues = normalizeMaps(paymentRepository.findPendingDuesByResidentId(residentId));
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Your pending dues retrieved successfully.");
        response.put("total_unpaid_invoices", dues.size());
        response.put("invoices", dues);
        return response;
    }
    
    public Map<String, Object> getBookings(Long residentId) {
        List<Map<String, Object>> amenities = normalizeMaps(amenityMgmtRepository.getAmenityBookingsForResident(residentId));
        List<Map<String, Object>> technicians = normalizeMaps(technicianManagementRepository.findBookingsByResidentId(residentId));
        
        Map<String, Object> response = new HashMap<>();
        response.put("amenities", amenities);
        response.put("technicians", technicians);
        return response;
    }
    
    private List<Map<String, Object>> normalizeMaps(List<Map<String, Object>> rows) {
        return rows.stream()
                   .map(row -> {
                       // Not Collectors.toMap: it throws on null values, and nullable columns are common here.
                       Map<String, Object> normalized = new HashMap<>();
                       row.forEach((key, value) -> normalized.put(key.toLowerCase(), value));
                       return normalized;
                   })
                   .collect(Collectors.toList());
    }
    
    /**
     * Deletes the resident with their login and bookings. Their invoices are kept, labelled with their name. If any
     * are still unpaid, deletion is refused with {@link UnpaidInvoicesException} unless {@code writeOffUnpaid} is
     * set, in which case those invoices are marked "Written Off" (kept in the ledger, no longer counted as owed).
     */
    @Transactional
    public void deleteResident(Long residentId, boolean writeOffUnpaid) {
        Optional<Resident> resident = residentRepository.findById(residentId);
        if (resident.isEmpty()) {
            throw Inputs.notFound("Resident not found.");
        }
        Resident r = resident.get();
        
        long unpaidCount = paymentRepository.countUnpaidForResident(residentId);
        if (unpaidCount > 0) {
            BigDecimal unpaidTotal = paymentRepository.sumUnpaidForResident(residentId);
            if (!writeOffUnpaid) {
                throw new UnpaidInvoicesException(unpaidCount, unpaidTotal);
            }
            paymentRepository.writeOffUnpaidForResident(residentId);
            auditService.log("payment", "resident " + residentId, "WRITE_OFF",
                "Wrote off " + unpaidCount + " unpaid invoice(s) totalling ₹" + unpaidTotal.toPlainString()
                    + " for " + r.getName());
        }
        
        // Must run before the delete: it finds the invoices through the bookings that the delete cascades away.
        paymentRepository.recordBilledTo(residentId, r.getName());
        auditService.log("resident", residentId.toString(), "DELETE", "Deleted resident: " + r.getName());
        residentRepository.delete(r);
    }
    
    @Transactional
    public void updateResidentProfile(Long residentId, Map<String, Object> updates) {
        Resident resident = residentRepository.findById(residentId)
                                .orElseThrow(() -> Inputs.notFound("Resident not found."));
        
        StringBuilder details = new StringBuilder("Updated: ");
        if (updates.containsKey("name")) {
            resident.setName(Inputs.requireText(Inputs.asText(updates.get("name"), "name"), "name", 100));
            details.append("name, ");
        }
        if (updates.containsKey("contact")) {
            resident.setContact(Inputs.requireText(Inputs.asText(updates.get("contact"), "contact"), "contact", 20));
            details.append("contact, ");
        }
        if (updates.containsKey("no_of_members")) {
            // Form inputs arrive as strings ("3"), API clients may send numbers; accept both.
            Integer members = Inputs.asInteger(updates.get("no_of_members"), "no_of_members");
            if (members == null || members < 1) {
                throw Inputs.badRequest("no_of_members must be at least 1.");
            }
            resident.setNoOfMembers(members);
            details.append("no_of_members, ");
        }
        residentRepository.save(resident);
        auditService.log("resident", residentId.toString(), "UPDATE", details.toString());
    }
}
