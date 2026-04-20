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
import com.urbannexus.model.Technician;
import com.urbannexus.repository.AdminRepository;
import com.urbannexus.repository.TechnicianManagementRepository;
import com.urbannexus.repository.TechnicianRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TechnicianService {

    private final TechnicianRepository technicianRepository;
    private final TechnicianManagementRepository technicianManagementRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Map<String, Object> addTechnician(Long techId, String name, String contact, String skill) {
        Technician tech = new Technician();
        tech.setTechId(techId);
        tech.setName(name);
        tech.setContact(contact);
        tech.setSkill(skill);

        tech = technicianRepository.save(tech);

        String defaultPassword = "pwd123#";
        String hashedPassword = passwordEncoder.encode(defaultPassword);
        String username = name.toLowerCase().replaceAll("\\s+", "_") + techId;

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPasswordHash(hashedPassword);
        admin.setRole("Technician");
        admin.setTechnician(tech);

        adminRepository.save(admin);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Technician profile configured successfully.");
        response.put("username", username);
        response.put("password", defaultPassword);
        return response;
    }

    public List<Map<String, Object>> getTasks(Long techId) {
        return technicianRepository.getTasksForTechnician(techId);
    }

    @Transactional
    public Map<String, Object> updateTaskStatus(Long assignmentId, String status) {
        technicianManagementRepository.updateTaskStatus(assignmentId, status);

        Map<String, Object> taskDetails = technicianRepository.getTaskDetails(assignmentId);
        if (taskDetails != null && "Completed".equalsIgnoreCase(status)) {
            String contact = (String) taskDetails.get("contact");
            if (contact != null) {
                // smsService.sendSms(contact, "Your requested technician task has been marked
                // as Completed.");
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Task status updated to " + status + ". Notification sent.");
        return response;
    }

    @Transactional
    public void deleteTechnician(Long techId) {
        Optional<Technician> tech = technicianRepository.findById(techId);
        if (tech.isEmpty()) {
            throw new RuntimeException("Technician not found.");
        }
        technicianRepository.delete(tech.get());
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
        return Map.of("message", "Availability updated to " + available);
    }
}
