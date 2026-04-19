package com.urbannexus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.urbannexus.model.Amenity;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {

}
