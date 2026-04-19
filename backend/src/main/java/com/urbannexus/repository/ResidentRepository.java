package com.urbannexus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.urbannexus.model.Resident;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {

       @Query("SELECT r FROM Resident r WHERE " +
                     "(:name IS NULL OR r.name LIKE %:name%) AND " +
                     "(:block IS NULL OR r.houseBlock = :block) AND " +
                     "(:floor IS NULL OR r.houseFloor = :floor) AND " +
                     "(:unit IS NULL OR r.houseUnit = :unit)")
       List<Resident> searchResidents(@Param("name") String name,
                     @Param("block") String block,
                     @Param("floor") String floor,
                     @Param("unit") String unit);
}
