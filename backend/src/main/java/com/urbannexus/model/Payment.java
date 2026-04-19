package com.urbannexus.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @Column(name = "trans_no")
    private String transNo;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private BigDecimal cost;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
}
