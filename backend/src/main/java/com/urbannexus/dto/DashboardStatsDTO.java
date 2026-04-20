/*
 * Copyright (c) 2026 Bhumika Agarwal
 */

package com.urbannexus.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDTO {
    private long totalResidents;
    private BigDecimal unpaidLedger;
    private long pendingTickets;
    private String gridUptime;
}
