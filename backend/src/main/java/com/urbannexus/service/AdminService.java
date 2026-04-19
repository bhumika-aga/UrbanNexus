package com.urbannexus.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.urbannexus.model.Resident;
import com.urbannexus.model.Technician;
import com.urbannexus.repository.AdminRepository;
import com.urbannexus.repository.PaymentRepository;
import com.urbannexus.repository.ResidentRepository;
import com.urbannexus.repository.TechnicianRepository;

@Service
public class AdminService {

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AdminRepository adminRepository;

    public List<Resident> searchResidents(String name, String block, String floor, String unit) {
        return residentRepository.searchResidents(name, block, floor, unit);
    }

    public List<Technician> getTechnicians() {
        return technicianRepository.findAll();
    }

    public List<Map<String, Object>> searchTransactions(String status, String block, String residentName) {
        return paymentRepository.searchTransactions(status, block, residentName);
    }

    public void processOverduePayments() {
        paymentRepository.processOverduePayments();
    }

    public List<Map<String, Object>> getAuditLogs() {
        return adminRepository.getAuditLogs();
    }
}
