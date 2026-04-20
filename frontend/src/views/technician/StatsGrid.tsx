/*
 * Copyright (c) 2026 Bhumika Agarwal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import { Box, Card, Typography, alpha } from "@mui/material";
import Grid from "@mui/material/Grid2";
import { CheckCircle2, ListChecks, Star, Zap } from "lucide-react";
import React from "react";
import { Booking } from "../../types";

interface StatsGridProps {
  tasks: Booking[];
}

const StatsGrid: React.FC<StatsGridProps> = ({ tasks }) => {
  const resolvedCount = tasks.filter((t) => t.status === "Resolved").length;
  const activeCount = tasks.filter((t) => t.status !== "Resolved").length;

  const stats = [
    {
      label: "Resolved",
      value: resolvedCount,
      icon: <CheckCircle2 size={20} />,
      color: "#10b981",
    },
    {
      label: "Active",
      value: activeCount,
      icon: <ListChecks size={20} />,
      color: "#6366f1",
    },
    {
      label: "Efficiency",
      value: resolvedCount > 0 ? "94%" : "--",
      icon: <Zap size={20} />,
      color: "#a855f7",
    },
    {
      label: "Rating",
      value: "4.9",
      icon: <Star size={20} />,
      color: "#f59e0b",
    },
  ];

  return (
    <Grid container spacing={3} sx={{ mb: 6 }}>
      {stats.map((stat, i) => (
        <Grid size={{ xs: 12, sm: 6, md: 3 }} key={i}>
          <Card
            sx={{
              p: 3,
              display: "flex",
              alignItems: "center",
              gap: 2.5,
              position: "relative",
              overflow: "hidden",
              "&::after": {
                content: '""',
                position: "absolute",
                top: 0,
                right: 0,
                width: "40%",
                height: "100%",
                background: `linear-gradient(90deg, transparent 0%, ${alpha(
                  stat.color,
                  0.05,
                )} 100%)`,
                pointerEvents: "none",
              },
            }}
          >
            <Box
              sx={{
                p: 1.5,
                borderRadius: "12px",
                backgroundColor: alpha(stat.color, 0.1),
                color: stat.color,
                display: "flex",
              }}
            >
              {stat.icon}
            </Box>
            <Box>
              <Typography variant="h4" sx={{ fontWeight: 800, lineHeight: 1 }}>
                {stat.value}
              </Typography>
              <Typography
                variant="caption"
                sx={{
                  color: "text.secondary",
                  fontWeight: 700,
                  textTransform: "uppercase",
                  fontSize: "0.65rem",
                }}
              >
                {stat.label}
              </Typography>
            </Box>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
};

export default StatsGrid;
