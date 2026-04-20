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
import java.util.List;
import java.util.Map;

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
    
    public DashboardStatsDTO getDashboardStats() {
        long totalResidents = residentRepository.count();
        
        BigDecimal unpaidLedger = paymentRepository.findAll().stream()
                                      .filter(p -> "Pending".equals(p.getStatus()) || "Overdue".equals(p.getStatus()))
                                      .map(com.urbannexus.model.Payment::getCost)
                                      .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long pendingAssignments = technicianManagementRepository.findAll().stream()
                                      .filter(tm -> "Assigned".equals(tm.getStatus()))
                                      .count();
        
        long pendingAmenities = amenityMgmtRepository.findAll().stream()
                                    .filter(am -> "Confirmed".equals(am.getStatus()))
                                    .count();
        
        return DashboardStatsDTO.builder()
                   .totalResidents(totalResidents)
                   .unpaidLedger(unpaidLedger)
                   .pendingTickets(pendingAssignments + pendingAmenities)
                   .gridUptime("99.8%") // Simulated infrastructure metric
                   .build();
    }
    
    public List<Resident> searchResidents(String name, String block, String floor, String unit) {
        return residentRepository.searchResidents(name, block, floor, unit);
    }
    
    public List<Technician> getTechnicians() {
        return technicianRepository.findAll();
    }
    
    public List<Map<String, Object>> searchTransactions(String status, String block, String residentName) {
        return paymentRepository.searchTransactions(status, block, residentName);
    }
    
    @Transactional
    public void processOverduePayments() {
        paymentRepository.processOverduePayments();
        auditService.log("payment", "ALL", "OVERDUE_PROCESS", "Manual trigger of overdue payment processing.");
    }
    
    @Transactional
    public void updateAdminProfile(Long adminId, Map<String, Object> updates) {
        Admin admin = adminRepository.findById(adminId)
                          .orElseThrow(() -> new RuntimeException("Admin not found."));
        
        if (updates.containsKey("username")) {
            admin.setUsername((String) updates.get("username"));
            auditService.log("admin", adminId.toString(), "UPDATE", "Updated username to: " + admin.getUsername());
        }
        adminRepository.save(admin);
    }
    
    public List<Map<String, Object>> getAuditLogs() {
        return adminRepository.getAuditLogs();
    }
    
    public List<Map<String, Object>> getAllAssignments() {
        return technicianManagementRepository.findAllAssignmentsDetailed();
    }
    
    public List<Map<String, Object>> getAllAmenityBookings() {
        return amenityMgmtRepository.findAllBookingsDetailed();
    }
    
    public List<Map<String, Object>> getAllTechnicianBookings() {
        // Current assignments detailed view serves as the history for technicians
        return technicianManagementRepository.findAllAssignmentsDetailed();
    }
}
