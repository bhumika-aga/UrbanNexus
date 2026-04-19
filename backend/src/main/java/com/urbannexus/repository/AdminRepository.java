package com.urbannexus.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.urbannexus.model.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsername(String username);

    @Query(value = "SELECT * FROM audit_log ORDER BY changed_at DESC", nativeQuery = true)
    List<Map<String, Object>> getAuditLogs();
}
