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
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    
    @Procedure(procedureName = "ProcessOverduePayments")
    void processOverduePayments();
    
    @Procedure(procedureName = "GetResidentPendingDues")
    List<Map<String, Object>> getPendingDues(Long resident_id);
    
    @Query(value = """
        SELECT
            p.trans_no,
            p.status,
            p.type as service_type,
            p.cost,
            p.payment_date
        FROM payment p
        LEFT JOIN amenity_mgmt am ON p.trans_no = am.trans_no
        LEFT JOIN technician_management tm ON p.trans_no = tm.trans_no
        WHERE (am.resident_id = :residentId OR tm.resident_id = :residentId)
        AND p.status IN ('Pending', 'Overdue')
        """, nativeQuery = true)
    List<Map<String, Object>> findPendingDuesByResidentId(@Param("residentId") Long residentId);
    
    @Query(value = """
        SELECT
            p.trans_no, p.status, p.type, p.cost,
            COALESCE(r_am.name, r_tm.name, 'UrbanNexus System') AS resident_name,
            COALESCE(r_am.house_block, r_tm.house_block, 'SYS') AS house_block,
            COALESCE(r_am.house_unit, r_tm.house_unit, 'ADMIN') AS house_unit
        FROM payment p
        LEFT JOIN amenity_mgmt am ON p.trans_no = am.trans_no
        LEFT JOIN resident r_am ON am.resident_id = r_am.resident_id
        LEFT JOIN technician_management tm ON p.trans_no = tm.trans_no
        LEFT JOIN resident r_tm ON tm.resident_id = r_tm.resident_id
        WHERE (:status IS NULL OR p.status = :status)
        AND (:block IS NULL OR r_am.house_block = :block OR r_tm.house_block = :block)
        AND (:residentName IS NULL OR :residentName = '' OR r_am.name LIKE CONCAT('%', :residentName, '%') OR r_tm.name LIKE CONCAT('%', :residentName, '%')
             OR (:residentName = 'UrbanNexus System' AND r_am.name IS NULL AND r_tm.name IS NULL))
        """, nativeQuery = true)
    List<Map<String, Object>> searchTransactions(@Param("status") String status,
                                                 @Param("block") String block,
                                                 @Param("residentName") String residentName);
    
    @Modifying
    @Query(value = """
        UPDATE payment p SET p.status = 'Paid'
        WHERE p.trans_no = :transNo AND (
            EXISTS (SELECT 1 FROM amenity_mgmt am WHERE am.trans_no = p.trans_no AND am.resident_id = :residentId)
            OR
            EXISTS (SELECT 1 FROM technician_management tm WHERE tm.trans_no = p.trans_no AND tm.resident_id = :residentId)
        )
        """, nativeQuery = true)
    int payTransactionForResident(@Param("transNo") String transNo, @Param("residentId") Long residentId);
    
    @Modifying
    @Query(value = "UPDATE payment SET status = 'Paid' WHERE trans_no = :transNo", nativeQuery = true)
    int payTransaction(@Param("transNo") String transNo);
    
    @Modifying
    @Query(value = "UPDATE payment SET status = 'Overdue' WHERE status = 'Pending' AND payment_date < DATEADD('DAY', -30, CURRENT_TIMESTAMP)", nativeQuery = true)
    void processOverdueH2();
}
