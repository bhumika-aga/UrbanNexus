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

        List<Technician> findBySkill(String skill);
}
