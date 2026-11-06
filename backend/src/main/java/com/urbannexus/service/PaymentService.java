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

import com.urbannexus.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Invoices are settled offline (there is no payment gateway): a resident submits an invoice with a payment
 * reference, which moves it to "Under Review", and only the SuperAdmin can mark it Paid or send it back.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    static final int MAX_REFERENCE_LENGTH = 100;
    
    private final PaymentRepository paymentRepository;
    private final AuditService auditService;
    
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void autoProcessOverdueInvoices() {
        paymentRepository.processOverduePayments();
    }
    
    @Transactional
    public void submitForReview(String transNo, Long residentId, String reference) {
        String trimmed = reference == null ? "" : reference.trim();
        if (trimmed.isEmpty()) {
            throw new RuntimeException("A payment reference (UPI ID, cheque number, etc.) is required.");
        }
        if (trimmed.length() > MAX_REFERENCE_LENGTH) {
            throw new RuntimeException("Payment reference must be at most " + MAX_REFERENCE_LENGTH + " characters.");
        }
        
        if (paymentRepository.submitForReview(transNo, residentId, trimmed) == 0) {
            throw new RuntimeException("Invoice not found, not yours, or not awaiting payment.");
        }
        auditService.log("payment", transNo, "UPDATE", "Resident submitted payment for review. Reference: " + trimmed);
    }
    
    @Transactional
    public void confirmPayment(String transNo) {
        if (paymentRepository.confirmPayment(transNo) == 0) {
            throw new RuntimeException("Invoice not found or already paid.");
        }
        auditService.log("payment", transNo, "UPDATE", "Payment confirmed as received.");
    }
    
    @Transactional
    public void rejectPayment(String transNo) {
        if (paymentRepository.rejectPayment(transNo) == 0) {
            throw new RuntimeException("Invoice not found or not under review.");
        }
        auditService.log("payment", transNo, "UPDATE", "Submitted payment rejected.");
    }
}
