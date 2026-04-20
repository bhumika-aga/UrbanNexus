package com.urbannexus.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class PricingId implements Serializable {
    private String itemName;
    private Pricing.PricingCategory category;
}