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

package com.urbannexus.controller;

import com.urbannexus.security.UserPrincipal;
import com.urbannexus.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/{trans_no}/submit")
    public ResponseEntity<?> submitPayment(@AuthenticationPrincipal UserPrincipal currentUser,
                                           @PathVariable("trans_no") String transNo,
                                           @RequestBody(required = false) Map<String, String> payload) {
        if (!"Resident".equals(currentUser.getRole()) || currentUser.getResidentId() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(Map.of("error", "Only Residents can submit their invoices for payment review."));
        }
        try {
            String reference = payload == null ? null : payload.get("reference");
            paymentService.submitForReview(transNo, currentUser.getResidentId(), reference);
            return ResponseEntity.ok(Map.of("message",
                "Payment for " + transNo + " submitted. It will show as Paid once the admin confirms it."));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (RuntimeException e) {
            log.error("Payment update failed for {}", transNo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Payment update failed."));
        }
    }
    
    @PostMapping("/{trans_no}/confirm")
    public ResponseEntity<?> confirmPayment(@AuthenticationPrincipal UserPrincipal currentUser,
                                            @PathVariable("trans_no") String transNo) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(Map.of("error", "Only the Admin can confirm payments."));
        }
        try {
            paymentService.confirmPayment(transNo);
            return ResponseEntity.ok(Map.of("message", "Transaction " + transNo + " marked as Paid."));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (RuntimeException e) {
            log.error("Payment update failed for {}", transNo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Payment update failed."));
        }
    }
    
    @PostMapping("/{trans_no}/reject")
    public ResponseEntity<?> rejectPayment(@AuthenticationPrincipal UserPrincipal currentUser,
                                           @PathVariable("trans_no") String transNo) {
        if (!"SuperAdmin".equals(currentUser.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(Map.of("error", "Only the Admin can reject payments."));
        }
        try {
            paymentService.rejectPayment(transNo);
            return ResponseEntity.ok(Map.of("message", "Payment for " + transNo + " rejected; invoice is unpaid again."));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        } catch (RuntimeException e) {
            log.error("Payment update failed for {}", transNo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Payment update failed."));
        }
    }
}
