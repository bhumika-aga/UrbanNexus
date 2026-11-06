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
  faCheck,
  faSearch,
  faSyncAlt,
  faXmark,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  Box,
  Button,
  CircularProgress,
  InputAdornment,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import axios from "axios";
import React, { useCallback, useEffect, useState } from "react";
import api from "../../api/axiosClient";
import { Transaction } from "../../types";

const STATUS_COLORS: Record<string, { bg: string; fg: string }> = {
  Paid: { bg: "#f0fdf4", fg: "#166534" },
  "Under Review": { bg: "#eff6ff", fg: "#1d4ed8" },
  Pending: { bg: "#fff7ed", fg: "#c2410c" },
  Overdue: { bg: "#fef2f2", fg: "#991b1b" },
  "Written Off": { bg: "#f4f4f5", fg: "#52525b" },
};

interface Props {
  // Called after payments change status, so the dashboard cards stay current.
  onDataChanged?: () => void;
}

const FinanceManager: React.FC<Props> = ({ onDataChanged }) => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [search, setSearch] = useState("");
  // Starts true so the table shows a spinner, not "No transactions", before the first load.
  const [loading, setLoading] = useState(true);
  const [scanning, setScanning] = useState(false);

  const fetchTransactions = useCallback(async () => {
    setLoading(true);
    try {
      // params are URL-encoded, so names with "&", "#" or "+" search correctly.
      const res = await api.get("/admin/transactions", {
        params: search ? { resident_name: search } : {},
      });
      setTransactions(res.data || []);
    } catch {
      console.error("Ledger sync failed");
    } finally {
      setLoading(false);
    }
  }, [search]);

  // "confirm" marks the invoice Paid (after checking the resident's reference, or when recording
  // an offline payment directly); "reject" sends a resident's submission back to unpaid.
  const reviewPayment = async (
    tx: Transaction,
    action: "confirm" | "reject",
  ) => {
    const prompt =
      action === "reject"
        ? `Reject the payment reported for ${tx.trans_no} (ref: ${tx.payment_reference})? The invoice becomes unpaid again.`
        : tx.status === "Under Review"
          ? `Confirm that ₹${tx.cost} for ${tx.trans_no} (ref: ${tx.payment_reference}) has been received?`
          : `Record ₹${tx.cost} for ${tx.trans_no} as received? Only do this once the money has arrived.`;
    if (!window.confirm(prompt)) return;
    try {
      await api.post(`/payments/${encodeURIComponent(tx.trans_no)}/${action}`);
      fetchTransactions();
      onDataChanged?.();
    } catch (err) {
      const errorMsg = axios.isAxiosError(err)
        ? err.response?.data?.error
        : null;
      alert(errorMsg || "Processing failed");
    }
  };

  const runOverdueScan = async () => {
    setScanning(true);
    try {
      const res = await api.post("/admin/process-overdue");
      alert(res.data.message);
      fetchTransactions();
      onDataChanged?.();
    } catch {
      alert("Scan failed");
    } finally {
      setScanning(false);
    }
  };

  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      fetchTransactions();
    }, 300);
    return () => clearTimeout(delayDebounceFn);
  }, [fetchTransactions]);

  return (
    <Box>
      <Box
        sx={{
          mb: 4,
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <TextField
          size="small"
          placeholder="Search by resident name..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          sx={{ width: 400 }}
          slotProps={{
            input: {
              startAdornment: (
                <InputAdornment position="start">
                  <FontAwesomeIcon icon={faSearch} style={{ fontSize: 16 }} />
                </InputAdornment>
              ),
            },
          }}
        />
        <Button
          variant="outlined"
          color="warning"
          startIcon={
            scanning ? (
              <CircularProgress size={16} color="inherit" />
            ) : (
              <FontAwesomeIcon icon={faSyncAlt} style={{ fontSize: 18 }} />
            )
          }
          onClick={runOverdueScan}
          disabled={scanning}
          sx={{
            borderColor: "#ffedd5",
            backgroundColor: "#fffdfa",
            color: "#c2410c",
            fontWeight: 700,
            "&:hover": { backgroundColor: "#fff7ed", borderColor: "#fed7aa" },
          }}
        >
          Run Overdue Scan
        </Button>
      </Box>

      <TableContainer component={Paper} elevation={0}>
        <Table sx={{ minWidth: 650 }}>
          <TableHead sx={{ backgroundColor: "#fafafa" }}>
            <TableRow>
              <TableCell
                sx={{
                  fontWeight: 700,
                  textTransform: "uppercase",
                  fontSize: "0.65rem",
                  color: "#666",
                }}
              >
                TXN #
              </TableCell>
              <TableCell
                sx={{
                  fontWeight: 700,
                  textTransform: "uppercase",
                  fontSize: "0.65rem",
                  color: "#666",
                }}
              >
                Resident
              </TableCell>
              <TableCell
                sx={{
                  fontWeight: 700,
                  textTransform: "uppercase",
                  fontSize: "0.65rem",
                  color: "#666",
                }}
              >
                Service
              </TableCell>
              <TableCell
                sx={{
                  fontWeight: 700,
                  textTransform: "uppercase",
                  fontSize: "0.65rem",
                  color: "#666",
                }}
              >
                Amount
              </TableCell>
              <TableCell
                sx={{
                  fontWeight: 700,
                  textTransform: "uppercase",
                  fontSize: "0.65rem",
                  color: "#666",
                }}
              >
                Status
              </TableCell>
              <TableCell
                sx={{
                  fontWeight: 700,
                  textTransform: "uppercase",
                  fontSize: "0.65rem",
                  color: "#666",
                }}
                align="right"
              >
                Action
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                  <CircularProgress size={24} color="inherit" />
                </TableCell>
              </TableRow>
            ) : transactions.length === 0 ? (
              <TableRow>
                <TableCell
                  colSpan={6}
                  align="center"
                  sx={{ py: 4, color: "#999", fontStyle: "italic" }}
                >
                  No transactions matches the current filter.
                </TableCell>
              </TableRow>
            ) : (
              transactions.map((tx) => (
                <TableRow key={tx.trans_no} hover>
                  <TableCell
                    sx={{ fontFamily: "monospace", fontSize: "0.75rem" }}
                  >
                    {tx.trans_no}
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 700 }}>
                      {tx.resident_name}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {tx.house_block === "SYS"
                        ? "System Managed"
                        : tx.house_block == null
                          ? "Former resident"
                          : `Unit ${tx.house_block}-${tx.house_unit}`}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Typography
                      variant="caption"
                      sx={{ fontWeight: 600, color: "#666" }}
                    >
                      {tx.service_type}
                    </Typography>
                  </TableCell>
                  <TableCell sx={{ fontWeight: 800, color: "#000" }}>
                    ₹{tx.cost}
                  </TableCell>
                  <TableCell>
                    <Box
                      sx={{
                        display: "inline-flex",
                        px: 1,
                        py: 0.5,
                        borderRadius: 1,
                        backgroundColor: (
                          STATUS_COLORS[tx.status] ?? STATUS_COLORS.Overdue
                        ).bg,
                        color: (
                          STATUS_COLORS[tx.status] ?? STATUS_COLORS.Overdue
                        ).fg,
                        fontWeight: 700,
                        fontSize: "0.6rem",
                        textTransform: "uppercase",
                      }}
                    >
                      {tx.status}
                    </Box>
                    {tx.status === "Under Review" && tx.payment_reference && (
                      <Typography
                        variant="caption"
                        color="text.secondary"
                        sx={{ display: "block", mt: 0.5 }}
                      >
                        Ref: {tx.payment_reference}
                      </Typography>
                    )}
                    {tx.status === "Paid" && tx.paid_at && (
                      <Typography
                        variant="caption"
                        color="text.secondary"
                        sx={{ display: "block", mt: 0.5 }}
                      >
                        on {new Date(tx.paid_at).toLocaleDateString()}
                      </Typography>
                    )}
                  </TableCell>
                  <TableCell align="right">
                    {tx.status === "Under Review" ? (
                      <>
                        <Button
                          size="small"
                          variant="text"
                          startIcon={
                            <FontAwesomeIcon
                              icon={faCheck}
                              style={{ fontSize: 14 }}
                            />
                          }
                          onClick={() => reviewPayment(tx, "confirm")}
                          sx={{ fontWeight: 700, color: "#166534" }}
                        >
                          Confirm
                        </Button>
                        <Button
                          size="small"
                          variant="text"
                          startIcon={
                            <FontAwesomeIcon
                              icon={faXmark}
                              style={{ fontSize: 14 }}
                            />
                          }
                          onClick={() => reviewPayment(tx, "reject")}
                          sx={{ fontWeight: 700, color: "#991b1b" }}
                        >
                          Reject
                        </Button>
                      </>
                    ) : (
                      (tx.status === "Pending" || tx.status === "Overdue") && (
                        <Button
                          size="small"
                          variant="text"
                          startIcon={
                            <FontAwesomeIcon
                              icon={faCheck}
                              style={{ fontSize: 14 }}
                            />
                          }
                          onClick={() => reviewPayment(tx, "confirm")}
                          sx={{ fontWeight: 700, color: "#1e40af" }}
                        >
                          Record Payment
                        </Button>
                      )
                    )}
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

export default FinanceManager;
