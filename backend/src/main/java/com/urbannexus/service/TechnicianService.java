package com.urbannexus.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.urbannexus.model.Admin;
import com.urbannexus.model.Technician;
import com.urbannexus.repository.AdminRepository;
import com.urbannexus.repository.TechnicianManagementRepository;
import com.urbannexus.repository.TechnicianRepository;

@Service
public class TechnicianService {

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private TechnicianManagementRepository technicianManagementRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
}
