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

import com.urbannexus.dto.DashboardStatsDTO;
import com.urbannexus.model.Admin;
import com.urbannexus.model.Resident;
import com.urbannexus.model.Technician;
import com.urbannexus.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final ResidentRepository residentRepository;
    private final TechnicianRepository technicianRepository;
    private final PaymentRepository paymentRepository;
    private final AdminRepository adminRepository;
    private final TechnicianManagementRepository technicianManagementRepository;
    private final AmenityMgmtRepository amenityMgmtRepository;
    private final AuditService auditService;
    private final Clock clock;
    
    // An empty query parameter (e.g. "?block=") means "no filter", not "match the empty string".
    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
    
    public DashboardStatsDTO getDashboardStats() {
        long totalResidents = residentRepository.count();
        BigDecimal pendingLedger = paymentRepository.sumPendingCosts();
        BigDecimal overdueLedger = paymentRepository.sumOverdueCosts();
        // Open work: technician jobs not yet completed (even if their date has passed), plus amenity
        // bookings still to come. Past amenity bookings are over, not pending.
        long pendingAssignments = technicianManagementRepository.countByStatus("Assigned");
        long upcomingAmenities = amenityMgmtRepository.countByStatusAndDateGreaterThanEqual("Confirmed",
            LocalDate.now(clock));
        
        return DashboardStatsDTO.builder()
                   .totalResidents(totalResidents)
                   .pendingLedger(pendingLedger)
                   .overdueLedger(overdueLedger)
                   .pendingTickets(pendingAssignments + upcomingAmenities)
                   .paymentsToReview(paymentRepository.countByStatus("Under Review"))
                   .build();
    }
    
    public List<Resident> searchResidents(String name, String block, String floor, String unit) {
        return residentRepository.searchResidents(blankToNull(name), blankToNull(block), blankToNull(floor),
            blankToNull(unit));
    }
    
    public List<Technician> getTechnicians() {
        return technicianRepository.findAll();
    }
    
    public List<Map<String, Object>> searchTransactions(String status, String block, String residentName) {
        return normalizeMaps(paymentRepository.searchTransactions(status, block, residentName));
    }
    
    @Transactional
    public void processOverduePayments() {
        paymentRepository.processOverduePayments();
        auditService.log("payment", "ALL", "OVERDUE_PROCESS", "Manual trigger of overdue payment processing.");
    }
    
    public String getUsername(Long adminId) {
        return adminRepository.findById(adminId)
                   .map(Admin::getUsername)
                   .orElseThrow(() -> Inputs.notFound("Admin not found."));
    }
    
    @Transactional
    public void updateAdminProfile(Long adminId, Map<String, Object> updates) {
        Admin admin = adminRepository.findById(adminId)
                          .orElseThrow(() -> Inputs.notFound("Admin not found."));
        
        if (updates.containsKey("username")) {
            String username = Inputs.requireText(Inputs.asText(updates.get("username"), "username"), "username", 50);
            if (!username.matches("[A-Za-z0-9._-]{3,50}")) {
                throw Inputs.badRequest("username must be 3-50 characters: letters, digits, '.', '_' or '-'.");
            }
            if (!username.equals(admin.getUsername())) {
                // Usernames are shared by every account (admin, residents, technicians).
                if (adminRepository.existsByUsername(username)) {
                    throw Inputs.conflict("That username is already taken.");
                }
                String previous = admin.getUsername();
                admin.setUsername(username);
                auditService.log("admin", adminId.toString(), "UPDATE",
                    "Changed username from " + previous + " to " + username);
            }
        }
        adminRepository.save(admin);
    }
    
    public List<Map<String, Object>> getAuditLogs() {
        return normalizeMaps(adminRepository.getAuditLogs());
    }
    
    public List<Map<String, Object>> getAllAssignments() {
        return normalizeMaps(technicianManagementRepository.findOpenAssignmentsDetailed());
    }
    
    public List<Map<String, Object>> getAllAmenityBookings() {
        return normalizeMaps(amenityMgmtRepository.findAllBookingsDetailed());
    }
    
    public List<Map<String, Object>> getAllTechnicianBookings() {
        return normalizeMaps(technicianManagementRepository.findCompletedAssignmentsDetailed());
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
}
