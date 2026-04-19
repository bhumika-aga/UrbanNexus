package com.urbannexus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "technician")
public class Technician {

    @Id
    @Column(name = "tech_id")
    private Long techId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String skill;

    @Column(nullable = false)
    private String contact;
}
