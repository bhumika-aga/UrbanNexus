/*
 * Copyright (c) 2026 Bhumika Agarwal
 */

import {
  Box,
  CircularProgress,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import React, { useEffect, useState } from "react";
import api from "../../api/axiosClient";
import { Booking } from "../../types";

const ServiceHistory: React.FC = () => {
  const [history, setHistory] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchHistory = async () => {
      try {
        const response = await api.get("/admin/technicians/bookings");
        setHistory(response.data);
      } catch (error) {
        console.error("Failed to fetch service history:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchHistory();
  }, []);

  return (
    <Box>
      <Typography variant="h6" sx={{ fontWeight: 800, mb: 3 }}>
        Complete Service Ticket History
      </Typography>
      <TableContainer component={Paper} elevation={0}>
        <Table>
          <TableHead sx={{ backgroundColor: "#fafafa" }}>
            <TableRow>
              <TableCell sx={{ fontWeight: 700 }}>RESIDENT</TableCell>
              <TableCell sx={{ fontWeight: 700 }}>TECHNICIAN</TableCell>
              <TableCell sx={{ fontWeight: 700 }}>SKILL</TableCell>
              <TableCell sx={{ fontWeight: 700 }}>DATE</TableCell>
              <TableCell sx={{ fontWeight: 700 }}>SLOT</TableCell>
              <TableCell sx={{ fontWeight: 700 }}>TRANSACTION</TableCell>
              <TableCell sx={{ fontWeight: 700 }}>STATUS</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                  <CircularProgress size={24} />
                </TableCell>
              </TableRow>
            ) : history.length === 0 ? (
              <TableRow>
                <TableCell
                  colSpan={7}
                  align="center"
                  sx={{ py: 4, color: "#999" }}
                >
                  No service tickets found.
                </TableCell>
              </TableRow>
            ) : (
              history.map((h, i) => (
                <TableRow key={h.assignment_id || i}>
                  <TableCell sx={{ fontWeight: 600 }}>
                    {h.resident_name || "N/A"}
                  </TableCell>
                  <TableCell>{h.technician}</TableCell>
                  <TableCell>{h.skill}</TableCell>
                  <TableCell>{h.assign_date}</TableCell>
                  <TableCell>Slot {h.slot}</TableCell>
                  <TableCell
                    sx={{ fontFamily: "monospace", fontSize: "0.75rem" }}
                  >
                    {(h as any).trans_no || "N/A"}
                  </TableCell>
                  <TableCell>
                    <Box
                      sx={{
                        display: "inline-flex",
                        px: 1,
                        py: 0.5,
                        borderRadius: 1,
                        backgroundColor:
                          h.status === "Resolved" ? "#f0fdf4" : "#fff7ed",
                        color: h.status === "Resolved" ? "#166534" : "#c2410c",
                        fontWeight: 700,
                        fontSize: "0.65rem",
                        textTransform: "uppercase",
                      }}
                    >
                      {h.status}
                    </Box>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

export default ServiceHistory;
