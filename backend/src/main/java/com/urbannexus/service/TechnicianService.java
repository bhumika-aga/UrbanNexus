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

import com.urbannexus.model.Admin;
import com.urbannexus.model.Technician;
import com.urbannexus.model.TechnicianManagement;
import com.urbannexus.repository.AdminRepository;
import com.urbannexus.repository.TechnicianManagementRepository;
import com.urbannexus.repository.TechnicianRepository;
import com.urbannexus.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TechnicianService {
    
    private static final List<String> TASK_STATUSES = List.of("Assigned", "Completed");
    
    private final TechnicianRepository technicianRepository;
    private final TechnicianManagementRepository technicianManagementRepository;
    private final AdminRepository adminRepository;
    private final AuthService authService;
    private final AuditService auditService;
    
    @Transactional
    public Map<String, Object> addTechnician(Long techId, String name, String contact, String skill) {
        Technician tech = new Technician();
        tech.setTechId(techId);
        tech.setName(name);
        tech.setContact(contact);
        tech.setSkill(skill);
        
        tech = technicianRepository.save(tech);
        
        String username = name.toLowerCase().replaceAll("\\s+", "_") + techId;
        
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setRole("Technician");
        admin.setTechnician(tech);
        String temporaryPassword = authService.issueTemporaryPassword(admin);
        
        auditService.log("technician", techId.toString(), "ADD", "Added new technician: " + name);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Technician profile configured successfully.");
        response.put("username", username);
        response.put("password", temporaryPassword);
        return response;
    }
    
    /**
     * Replaces the technician's password with a new one-time password and signs them out everywhere.
     */
    @Transactional
    public Map<String, Object> resetCredentials(Long techId) {
        Admin admin = adminRepository.findByTechnician_TechId(techId)
                          .orElseThrow(() -> new RuntimeException("Technician not found."));
        String temporaryPassword = authService.issueTemporaryPassword(admin);
        auditService.log("technician", techId.toString(), "UPDATE", "Reset login password for: " + admin.getUsername());
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password reset. Share the new temporary password with the technician.");
        response.put("username", admin.getUsername());
        response.put("password", temporaryPassword);
        return response;
    }
    
    public List<Map<String, Object>> getTasks(Long techId) {
        return normalizeMaps(technicianRepository.getTasksForTechnician(techId));
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
     * Updates an assignment's status. A technician may only mark their own assignments Completed; the SuperAdmin may
     * set any assignment to any valid status.
     */
    @Transactional
    public Map<String, Object> updateTaskStatus(UserPrincipal actor, Long assignmentId, String status) {
        if (status == null || !TASK_STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Status must be one of: " + String.join(", ", TASK_STATUSES) + ".");
        }
        
        TechnicianManagement assignment = technicianManagementRepository.findById(assignmentId)
                                              .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                  "Assignment not found."));
        
        if ("Technician".equals(actor.getRole())) {
            if (actor.getTechId() == null || !actor.getTechId().equals(assignment.getTechnician().getTechId())) {
                log.warn("Technician '{}' attempted to update assignment {} assigned to someone else",
                    actor.getUsername(), assignmentId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This assignment is not assigned to you.");
            }
            if (!"Completed".equals(status)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Technicians can only mark their assignments as Completed.");
            }
        }
        
        assignment.setStatus(status);
        technicianManagementRepository.save(assignment);
        auditService.log("technician_management", assignmentId.toString(), "UPDATE",
            "Status set to " + status + " by " + actor.getUsername());
        return Map.of("message", "Task status updated to " + status + ".");
    }
    
    @Transactional
    public void deleteTechnician(Long techId) {
        Technician tech = technicianRepository.findById(techId)
                              .orElseThrow(() -> new RuntimeException("Technician not found."));
        auditService.log("technician", techId.toString(), "DELETE", "Deleted technician: " + tech.getName());
        technicianRepository.delete(tech);
    }
    
    public Technician getTechnician(Long techId) {
        return technicianRepository.findById(techId)
                   .orElseThrow(() -> new RuntimeException("Technician not found."));
    }
    
    @Transactional
    public Map<String, Object> updateAvailability(Long techId, Boolean available) {
        Technician tech = getTechnician(techId);
        tech.setAvailable(available);
        technicianRepository.save(tech);
        auditService.log("technician", techId.toString(), "UPDATE", "Updated availability to: " + available);
        return Map.of("message", "Availability updated to " + available);
    }
    
    @Transactional
    public void updateTechnicianProfile(Long techId, Map<String, Object> updates) {
        Technician tech = technicianRepository.findById(techId)
                              .orElseThrow(() -> new RuntimeException("Technician not found."));
        
        StringBuilder details = new StringBuilder("Updated: ");
        if (updates.containsKey("name")) {
            tech.setName((String) updates.get("name"));
            details.append("name, ");
        }
        if (updates.containsKey("contact")) {
            tech.setContact((String) updates.get("contact"));
            details.append("contact, ");
        }
        if (updates.containsKey("skill")) {
            tech.setSkill((String) updates.get("skill"));
            details.append("skill, ");
        }
        if (updates.containsKey("available")) {
            tech.setAvailable((Boolean) updates.get("available"));
            details.append("available, ");
        }
        
        technicianRepository.save(tech);
        auditService.log("technician", techId.toString(), "UPDATE", details.toString());
    }
}
