package com.urbannexus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "resident")
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resident_id")
    private Long residentId;

    @Column(nullable = false)
    private String name;

    @Column(name = "house_block", nullable = false)
    private String houseBlock;

    @Column(name = "house_floor", nullable = false)
    private String houseFloor;

    @Column(name = "house_unit", nullable = false)
    private String houseUnit;

    @Column(name = "ownership_status", nullable = false)
    private String ownershipStatus;

    @Column(nullable = false)
    private String contact;

    @Column(name = "no_of_members")
    private Integer noOfMembers;
}
