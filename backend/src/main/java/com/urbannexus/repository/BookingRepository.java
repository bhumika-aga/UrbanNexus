package com.urbannexus.repository;

import java.util.Map;

import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends CrudRepository<com.urbannexus.model.TechnicianManagement, Long> {

    @Procedure(procedureName = "AutoBookTechnician")
    Map<String, Object> autoBookTechnician(Long resident_id, String skill, Integer slot, String assign_date);

    @Procedure(procedureName = "AutoBookAmenity")
    Map<String, Object> autoBookAmenity(Long resident_id, Long amenity_id, String date, Integer slot,
            Integer capacity_booked);
}
