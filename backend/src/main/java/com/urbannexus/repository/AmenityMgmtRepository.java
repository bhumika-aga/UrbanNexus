package com.urbannexus.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.urbannexus.model.AmenityMgmt;

public interface AmenityMgmtRepository extends JpaRepository<AmenityMgmt, Long> {

    @Query(value = """
            SELECT am.booking_id, a.name AS amenity, am.date, am.slot, am.status
            FROM amenity_mgmt am
            JOIN amenity a ON am.amenity_id = a.amenity_id
            WHERE am.resident_id = :residentId ORDER BY am.date DESC
            """, nativeQuery = true)
    List<Map<String, Object>> getAmenityBookingsForResident(@Param("residentId") Long residentId);
}
