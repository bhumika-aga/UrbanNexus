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
    private BigDecimal unpaidLedger;
    private long pendingTickets;
    private String gridUptime;
}
