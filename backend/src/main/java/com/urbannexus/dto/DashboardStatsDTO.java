/*
 * Copyright (c) 2026 Bhumika Agarwal
 */

package com.urbannexus.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardStatsDTO {
    private long totalResidents;
    private BigDecimal pendingLedger;
    private BigDecimal overdueLedger;
    private long pendingTickets;
    // Residents' reported payments waiting for the admin to confirm or reject.
    private long paymentsToReview;
}
