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

import {
  Box,
  Card,
  Chip,
  Paper,
  Stack,
  Typography,
  alpha,
} from "@mui/material";
import { CheckCircle2, Clock, MapPin, User } from "lucide-react";
import React from "react";
import { Booking } from "../../types";

interface HistoryManagerProps {
  tasks: Booking[];
}

const HistoryManager: React.FC<HistoryManagerProps> = ({ tasks }) => {
  const resolvedTasks = tasks.filter((t) => t.status === "Resolved");

  return (
    <Box>
      <Stack spacing={2.5}>
        {resolvedTasks.length === 0 ? (
          <Paper
            sx={{
              p: 6,
              textAlign: "center",
              backgroundColor: "background.default",
              borderStyle: "dashed",
            }}
          >
            <Typography variant="body1" color="text.secondary">
              No completed missions logged yet.
            </Typography>
          </Paper>
        ) : (
          resolvedTasks.map((task) => (
            <Card
              key={task.assignment_id}
              sx={{
                p: 3,
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                backgroundColor: alpha("#10b981", 0.02),
              }}
            >
              <Box>
                <Box
                  sx={{
                    display: "flex",
                    alignItems: "center",
                    gap: 1.5,
                    mb: 1,
                  }}
                >
                  <Box
                    sx={{
                      p: 0.5,
                      borderRadius: "6px",
                      backgroundColor: alpha("#10b981", 0.1),
                      color: "#10b981",
                    }}
                  >
                    <CheckCircle2 size={16} />
                  </Box>
                  <Typography
                    variant="caption"
                    sx={{
                      fontWeight: 800,
                      color: "#10b981",
                      textTransform: "uppercase",
                      letterSpacing: "0.1em",
                    }}
                  >
                    Resolved
                  </Typography>
                </Box>
                <Typography variant="h6" sx={{ fontWeight: 800 }}>
                  {(task as any).resident_name}
                </Typography>
                <Box
                  sx={{
                    display: "flex",
                    alignItems: "center",
                    gap: 2.5,
                    mt: 1.5,
                  }}
                >
                  <Box
                    sx={{
                      display: "flex",
                      alignItems: "center",
                      gap: 0.5,
                      color: "text.secondary",
                    }}
                  >
                    <User size={14} />
                    <Typography variant="caption" sx={{ fontWeight: 600 }}>
                      Unit{" "}
                      {task.booking_id
                        ? "N/A"
                        : `${(task as any).house_block}-${(task as any).house_unit}`}
                    </Typography>
                  </Box>
                  <Box
                    sx={{
                      display: "flex",
                      alignItems: "center",
                      gap: 0.5,
                      color: "text.secondary",
                    }}
                  >
                    <Clock size={14} />
                    <Typography variant="caption" sx={{ fontWeight: 600 }}>
                      Slot {task.slot}
                    </Typography>
                  </Box>
                  <Box
                    sx={{
                      display: "flex",
                      alignItems: "center",
                      gap: 0.5,
                      color: "text.secondary",
                    }}
                  >
                    <MapPin size={14} />
                    <Typography variant="caption" sx={{ fontWeight: 600 }}>
                      Field Ops
                    </Typography>
                  </Box>
                </Box>
              </Box>
              <Box sx={{ textAlign: "right" }}>
                <Chip
                  label="Success"
                  size="small"
                  sx={{
                    backgroundColor: alpha("#10b981", 0.1),
                    color: "#10b981",
                    fontWeight: 700,
                    borderRadius: "6px",
                  }}
                />
              </Box>
            </Card>
          ))
        )}
      </Stack>
    </Box>
  );
};

export default HistoryManager;
