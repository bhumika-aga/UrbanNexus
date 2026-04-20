/*
 * Copyright (c) 2026 Bhumika Agarwal
 */

package com.urbannexus.util;

import java.util.Map;

import com.urbannexus.repository.PaymentRepository;
import com.urbannexus.service.BookingService;

/**
 * Static bridge for H2 Stored Procedure compatibility.
 * These methods are registered as ALIASes in schema.sql.
 */
public class H2DatabaseFunctions {

    public static Map<String, Object> autoBookTechnician(Long residentId, String skill, Integer slot,
            String assignDate) {
        return StaticContextAccessor.getBean(BookingService.class).executeTechnicianBooking(residentId, skill, slot,
                assignDate);
    }

    public static Map<String, Object> autoBookAmenity(Long residentId, Long amenityId, String date, Integer slot,
            Integer capacityBooked) {
        return StaticContextAccessor.getBean(BookingService.class).executeAmenityBooking(residentId, amenityId, date,
                slot, capacityBooked);
    }

    public static java.util.List<Map<String, Object>> getResidentPendingDues(Long residentId) {
        return StaticContextAccessor.getBean(PaymentRepository.class)
                .findPendingDuesByResidentId(residentId);
    }

    public static void processOverduePayments() {
        StaticContextAccessor.getBean(PaymentRepository.class).processOverdueH2();
    }
}
