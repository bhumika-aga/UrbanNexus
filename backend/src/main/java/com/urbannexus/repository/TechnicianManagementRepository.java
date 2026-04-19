package com.urbannexus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.urbannexus.model.TechnicianManagement;

public interface TechnicianManagementRepository extends JpaRepository<TechnicianManagement, Long> {

    @Modifying
    @Query("UPDATE TechnicianManagement t SET t.status = :status WHERE t.assignmentId = :assignmentId")
    void updateTaskStatus(@Param("assignmentId") Long assignmentId, @Param("status") String status);
}
