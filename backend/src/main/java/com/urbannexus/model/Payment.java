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

package com.urbannexus.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment")
public class Payment {
    
    @Id
    @Column(name = "trans_no")
    private String transNo;
    
    @Column(nullable = false)
    private String status;
    
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false)
    private BigDecimal cost;
    
    // When the invoice was issued (the booking was made). Despite the column name this is not when it was
    // paid; the 30-day overdue rule counts from here.
    @Column(name = "payment_date")
    private LocalDateTime issuedAt;
    
    // When the admin confirmed the money as received; null until the invoice is Paid.
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    // The resident's proof of an offline payment (UPI ref, cheque no., ...), set when they submit
    // the invoice for review and cleared if the admin rejects it.
    @Column(name = "payment_reference", length = 100)
    private String paymentReference;
    
    // The billed resident's name, recorded when that resident is deleted (see ResidentService.deleteResident).
    @Column(name = "billed_to", length = 100)
    private String billedTo;
    
    /**
     * A base price plus 18% GST, rounded to paise.
     */
    public static BigDecimal withGst(BigDecimal basePrice) {
        return basePrice.multiply(new BigDecimal("1.18")).setScale(2, RoundingMode.HALF_UP);
    }
    
    @PrePersist
    public void calculateGST() {
        if (this.cost != null) {
            this.cost = withGst(this.cost);
        }
        if (this.issuedAt == null) {
            this.issuedAt = LocalDateTime.now();
        }
    }
}
