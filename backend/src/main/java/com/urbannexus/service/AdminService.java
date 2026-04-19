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

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.urbannexus.model.Resident;
import com.urbannexus.model.Technician;
import com.urbannexus.repository.AdminRepository;
import com.urbannexus.repository.PaymentRepository;
import com.urbannexus.repository.ResidentRepository;
import com.urbannexus.repository.TechnicianRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ResidentRepository residentRepository;
    private final TechnicianRepository technicianRepository;
    private final PaymentRepository paymentRepository;
    private final AdminRepository adminRepository;

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
