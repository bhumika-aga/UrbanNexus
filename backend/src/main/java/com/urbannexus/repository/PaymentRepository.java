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

package com.urbannexus.repository;

import com.urbannexus.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    
    long countByStatus(String status);
    
    // Returns how many invoices were marked Overdue.
    @Modifying
    @Query(value = "UPDATE payment SET status = 'Overdue' WHERE status = 'Pending' AND payment_date < NOW() - INTERVAL '30' DAY", nativeQuery = true)
    int processOverduePayments();
    
    // Fills payment.resident_id for invoices created before the column existed, from the booking that
    // produced them. Rows that already have it are untouched, so running it on every startup is safe.
    @Modifying
    @Query(value = """
        UPDATE payment p SET resident_id = COALESCE(
            (SELECT MIN(am.resident_id) FROM amenity_mgmt am WHERE am.trans_no = p.trans_no),
            (SELECT MIN(tm.resident_id) FROM technician_management tm WHERE tm.trans_no = p.trans_no))
        WHERE p.resident_id IS NULL
        AND (EXISTS (SELECT 1 FROM amenity_mgmt am WHERE am.trans_no = p.trans_no)
             OR EXISTS (SELECT 1 FROM technician_management tm WHERE tm.trans_no = p.trans_no))
        """, nativeQuery = true)
    int backfillResidentIds();
    
    @Query(value = """
        SELECT
            p.trans_no,
            p.status,
            p.type as service_type,
            p.cost,
            p.payment_date,
            p.payment_reference
        FROM payment p
        WHERE p.resident_id = :residentId
        AND p.status IN ('Pending', 'Overdue', 'Under Review')
        ORDER BY p.payment_date
        """, nativeQuery = true)
    List<Map<String, Object>> findPendingDuesByResidentId(@Param("residentId") Long residentId);
    
    // namePattern is lower-cased with wildcards by the caller ("%jane%"); a function is never applied to a
    // parameter, because a null parameter has no type on PostgreSQL.
    @Query(value = """
        SELECT
            p.trans_no, p.status, p.type as service_type, p.cost, p.payment_reference,
            p.payment_date AS issued_at, p.paid_at,
            COALESCE(r.name, p.billed_to, 'UrbanNexus System') AS resident_name,
            -- NULL block/unit with a name means a former (deleted) resident; 'SYS' means a system charge.
            COALESCE(r.house_block, CASE WHEN p.billed_to IS NULL THEN 'SYS' END) AS house_block,
            COALESCE(r.house_unit, CASE WHEN p.billed_to IS NULL THEN 'ADMIN' END) AS house_unit
        FROM payment p
        LEFT JOIN resident r ON r.resident_id = p.resident_id
        WHERE (:status IS NULL OR p.status = :status)
        AND (:block IS NULL OR r.house_block = :block)
        AND (:namePattern IS NULL OR LOWER(COALESCE(r.name, p.billed_to, 'UrbanNexus System')) LIKE :namePattern)
        ORDER BY p.payment_date DESC
        """, nativeQuery = true)
    List<Map<String, Object>> searchTransactions(@Param("status") String status,
                                                 @Param("block") String block,
                                                 @Param("namePattern") String namePattern);
    
    // Resident side: claim an unpaid invoice of their own as paid offline. It stays unpaid
    // until the admin confirms it.
    @Modifying
    @Query(value = """
        UPDATE payment SET status = 'Under Review', payment_reference = :reference
        WHERE trans_no = :transNo
        AND status IN ('Pending', 'Overdue')
        AND resident_id = :residentId
        """, nativeQuery = true)
    int submitForReview(@Param("transNo") String transNo, @Param("residentId") Long residentId,
                        @Param("reference") String reference);
    
    // Admin side: record the money as received, whether or not the resident submitted a claim.
    @Modifying
    @Query(value = "UPDATE payment SET status = 'Paid', paid_at = CURRENT_TIMESTAMP WHERE trans_no = :transNo AND status IN ('Pending', 'Overdue', 'Under Review')", nativeQuery = true)
    int confirmPayment(@Param("transNo") String transNo);
    
    // Admin side: the claimed payment was not received. Return the invoice to whichever unpaid
    // status it would have now (same 30-day rule as processOverduePayments).
    @Modifying
    @Query(value = """
        UPDATE payment SET
            status = CASE WHEN payment_date < NOW() - INTERVAL '30' DAY THEN 'Overdue' ELSE 'Pending' END,
            payment_reference = NULL
        WHERE trans_no = :transNo AND status = 'Under Review'
        """, nativeQuery = true)
    int rejectPayment(@Param("transNo") String transNo);
    
    @Query(value = "SELECT COUNT(*) FROM payment WHERE status IN ('Pending', 'Overdue', 'Under Review') "
                       + "AND resident_id = :residentId", nativeQuery = true)
    long countUnpaidForResident(@Param("residentId") Long residentId);
    
    @Query(value = "SELECT COALESCE(SUM(cost), 0) FROM payment WHERE status IN ('Pending', 'Overdue', 'Under Review') "
                       + "AND resident_id = :residentId", nativeQuery = true)
    java.math.BigDecimal sumUnpaidForResident(@Param("residentId") Long residentId);
    
    // Written-off invoices stay in the ledger for the record but no longer count as money owed.
    @Modifying
    @Query(value = "UPDATE payment SET status = 'Written Off' WHERE status IN ('Pending', 'Overdue', 'Under Review') "
                       + "AND resident_id = :residentId", nativeQuery = true)
    int writeOffUnpaidForResident(@Param("residentId") Long residentId);
    
    // Copies the resident's name onto their invoices so the ledger still shows who was billed after the
    // resident is deleted.
    @Modifying
    @Query(value = "UPDATE payment SET billed_to = :name WHERE resident_id = :residentId", nativeQuery = true)
    int recordBilledTo(@Param("residentId") Long residentId, @Param("name") String name);
    
    // Money not yet received: includes claims still awaiting the admin's confirmation.
    @Query(value = "SELECT COALESCE(SUM(cost), 0) FROM payment WHERE status IN ('Pending', 'Under Review')", nativeQuery = true)
    java.math.BigDecimal sumPendingCosts();
    
    @Query(value = "SELECT COALESCE(SUM(cost), 0) FROM payment WHERE status = 'Overdue'", nativeQuery = true)
    java.math.BigDecimal sumOverdueCosts();
}
