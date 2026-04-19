package com.urbannexus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.urbannexus.repository.PaymentRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void autoProcessOverdueInvoices() {
        paymentRepository.processOverduePayments();
    }

    @Transactional
    public void payInvoice(String transNo, Long residentId) {
        int updated;
        if (residentId != null) {
            updated = paymentRepository.payTransactionForResident(transNo, residentId);
        } else {
            updated = paymentRepository.payTransaction(transNo);
        }

        if (updated == 0) {
            throw new RuntimeException("Invoice not found or already paid.");
        }
    }
}
