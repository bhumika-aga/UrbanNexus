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

package com.urbannexus.exception;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Thrown when deleting a resident who still owes money and the caller has not agreed to write it off.
 */
@Getter
public class UnpaidInvoicesException extends RuntimeException {
    
    private final long invoiceCount;
    private final BigDecimal total;
    
    public UnpaidInvoicesException(long invoiceCount, BigDecimal total) {
        super("Resident has " + invoiceCount + " unpaid invoice(s) totalling ₹" + total.toPlainString()
                  + ". Record the payments, or confirm a write-off to delete the resident.");
        this.invoiceCount = invoiceCount;
        this.total = total;
    }
}
