package com.urbannexus.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.urbannexus.model.Technician;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Long> {

        @Query(value = """
                        SELECT tm.assignment_id, tm.assign_date, tm.slot, tm.status,
                               r.name as resident_name, r.house_block, r.house_unit, r.contact as resident_phone
                        FROM technician_management tm
                        JOIN resident r ON tm.resident_id = r.resident_id
                        WHERE tm.tech_id = :techId ORDER BY tm.assign_date ASC, tm.slot ASC
                        """, nativeQuery = true)
        List<Map<String, Object>> getTasksForTechnician(@Param("techId") Long techId);

        @Query(value = """
                        SELECT r.contact, r.name as resident_name, t.name as tech_name, t.skill
                        FROM technician_management tm
                        JOIN resident r ON tm.resident_id = r.resident_id
                        JOIN technician t ON tm.tech_id = t.tech_id
                        WHERE tm.assignment_id = :assignmentId
                        """, nativeQuery = true)
        Map<String, Object> getTaskDetails(@Param("assignmentId") Long assignmentId);
}
