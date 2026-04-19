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
  Button,
  Chip,
  CircularProgress,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import { CheckCircle2, Clock, MapPin } from "lucide-react";
import React, { useEffect, useState } from "react";
import api from "../api/axiosClient";
import { Booking } from "../types";

const TechnicianDashboard: React.FC = () => {
  const [tasks, setTasks] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchTasks = async () => {
    try {
      const res = await api.get("/technician/me/tasks");
      setTasks(res.data);
    } catch (err) {
      console.error("Task sync failed");
    } finally {
      setLoading(false);
    }
  };

  const updateStatus = async (id: number, status: string) => {
    try {
      await api.put(`/technician/tasks/${id}/status`, { status });
      fetchTasks();
    } catch (err) {
      alert("Status update failed");
    }
  };

  useEffect(() => {
    fetchTasks();
  }, []);

  return (
    <Box>
      <Box sx={{ mb: 6 }}>
        <Typography variant="h3" sx={{ fontWeight: 800, mb: 1 }}>
          Field Support Grid
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Monitor and resolve your assigned technical tickets in real-time.
        </Typography>
      </Box>

      {loading ? (
        <Box sx={{ display: "flex", justifyContent: "center", py: 10 }}>
          <CircularProgress />
        </Box>
      ) : (
        <Stack spacing={3}>
          {tasks.length === 0 ? (
            <Paper
              sx={{
                p: 6,
                textAlign: "center",
                backgroundColor: "#fafafa",
                borderStyle: "dashed",
              }}
            >
              <Typography variant="body1" color="text.secondary">
                All systems clear. No pending assignments.
              </Typography>
            </Paper>
          ) : (
            tasks.map((task) => (
              <Paper
                key={task.assignment_id}
                sx={{
                  p: 3,
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  "&:hover": { borderColor: "#000" },
                }}
              >
                <Box>
                  <Box
                    sx={{
                      display: "flex",
                      alignItems: "center",
                      gap: 1,
                      mb: 0.5,
                    }}
                  >
                    <Typography
                      variant="caption"
                      sx={{
                        fontWeight: 800,
                        color: "#3b82f6",
                        textTransform: "uppercase",
                        letterSpacing: "0.05em",
                      }}
                    >
                      Unit{" "}
                      {task.booking_id
                        ? "N/A"
                        : `${(task as any).house_block}-${(task as any).house_unit}`}
                    </Typography>
                  </Box>
                  <Typography variant="h6" sx={{ fontWeight: 800 }}>
                    {(task as any).resident_name}
                  </Typography>
                  <Box
                    sx={{
                      display: "flex",
                      alignItems: "center",
                      gap: 2,
                      mt: 1,
                    }}
                  >
                    <Box
                      sx={{
                        display: "flex",
                        alignItems: "center",
                        gap: 0.5,
                        color: "#666",
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
                        color: "#666",
                      }}
                    >
                      <MapPin size={14} />
                      <Typography variant="caption" sx={{ fontWeight: 600 }}>
                        Ground Support
                      </Typography>
                    </Box>
                  </Box>
                </Box>
                <Box>
                  {task.status !== "Resolved" ? (
                    <Button
                      variant="contained"
                      color="success"
                      onClick={() =>
                        updateStatus(task.assignment_id!, "Resolved")
                      }
                      sx={{ fontWeight: 700 }}
                    >
                      Mark Resolved
                    </Button>
                  ) : (
                    <Chip
                      icon={<CheckCircle2 size={14} />}
                      label="Resolved"
                      color="success"
                      variant="outlined"
                      sx={{ fontWeight: 700 }}
                    />
                  )}
                </Box>
              </Paper>
            ))
          )}
        </Stack>
      )}
    </Box>
  );
};

export default TechnicianDashboard;
