package com.urbannexus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "amenity")
public class Amenity {

    @Id
    @Column(name = "amenity_id")
    private Long amenityId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer capacity;
}
