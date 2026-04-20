package com.urbannexus.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class PricingId implements Serializable {
    private String itemName;
    private Pricing.PricingCategory category;
}