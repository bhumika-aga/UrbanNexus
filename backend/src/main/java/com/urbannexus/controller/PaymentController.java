package com.urbannexus.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.urbannexus.security.UserPrincipal;
import com.urbannexus.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/{trans_no}/pay")
    public ResponseEntity<?> payBill(@AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable("trans_no") String transNo) {
        try {
            paymentService.payInvoice(transNo, currentUser.getResidentId());
            return ResponseEntity.ok(Map.of("message", "Transaction " + transNo + " processed successfully!"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("permission")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Payment processing failed."));
        }
    }
}
