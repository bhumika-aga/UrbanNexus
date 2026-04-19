package com.urbannexus.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.urbannexus.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    @Procedure(procedureName = "ProcessOverduePayments")
    void processOverduePayments();

    @Procedure(procedureName = "GetResidentPendingDues")
    List<Map<String, Object>> getPendingDues(Long resident_id);

    @Query(value = """
            SELECT
                p.trans_no, p.status, p.type, p.cost,
                COALESCE(r_am.name, r_tm.name) AS resident_name,
                COALESCE(r_am.house_block, r_tm.house_block) AS house_block,
                COALESCE(r_am.house_unit, r_tm.house_unit) AS house_unit
            FROM payment p
            LEFT JOIN amenity_mgmt am ON p.trans_no = am.trans_no
            LEFT JOIN resident r_am ON am.resident_id = r_am.resident_id
            LEFT JOIN technician_management tm ON p.trans_no = tm.trans_no
            LEFT JOIN resident r_tm ON tm.resident_id = r_tm.resident_id
            WHERE (:status IS NULL OR p.status = :status)
            AND (:block IS NULL OR r_am.house_block = :block OR r_tm.house_block = :block)
            AND (:residentName IS NULL OR r_am.name LIKE :residentName OR r_tm.name LIKE :residentName)
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
}
