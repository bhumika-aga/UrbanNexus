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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.urbannexus.model.TechnicianManagement;

public interface TechnicianManagementRepository extends JpaRepository<TechnicianManagement, Long> {

    @Modifying
    @Query("UPDATE TechnicianManagement t SET t.status = :status WHERE t.assignmentId = :assignmentId")
    void updateTaskStatus(@Param("assignmentId") Long assignmentId, @Param("status") String status);

    @Query(value = "SELECT tm.assignment_id, t.name as technician, t.skill, tm.assign_date, tm.slot, tm.status " +
            "FROM technician_management tm " +
            "JOIN technician t ON tm.tech_id = t.tech_id " +
            "WHERE tm.resident_id = :residentId", nativeQuery = true)
    List<Map<String, Object>> findBookingsByResidentId(@Param("residentId") Long residentId);

    boolean existsByTechnician_TechIdAndAssignDateAndSlot(Long techId, LocalDate assignDate, Integer slot);
}
