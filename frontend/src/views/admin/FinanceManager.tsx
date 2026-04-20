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
import { ArrowUpRight, RefreshCw, Search } from "lucide-react";
import React, { useEffect, useState } from "react";
import api from "../../api/axiosClient";
import { Transaction } from "../../types";

const FinanceManager: React.FC = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(false);
  const [scanning, setScanning] = useState(false);

  const fetchTransactions = async () => {
    setLoading(true);
    try {
      const res = await api.get(`/admin/transactions?resident_name=${search}`);
      setTransactions(res.data || []);
    } catch (err) {
      console.error("Ledger sync failed");
    } finally {
      setLoading(false);
    }
  };

  const processPayment = async (transNo: string) => {
    try {
      await api.post(`/payments/${transNo}/pay`);
      fetchTransactions();
    } catch (err: unknown) {
      const errorMsg =
        err && typeof err === "object" && "response" in err
          ? (err as any).response?.data?.error
          : "Processing failed";
      alert(errorMsg || "Processing failed");
    }
  };

  const runOverdueScan = async () => {
    setScanning(true);
    try {
      await api.post("/admin/process-overdue");
      alert("Overdue cursor executed mapping penalties to the ledger.");
      fetchTransactions();
    } catch (err) {
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
  }, [search]);

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
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <Search size={16} />
              </InputAdornment>
            ),
          }}
        />
        <Button
          variant="outlined"
          color="warning"
          startIcon={
            scanning ? (
              <CircularProgress size={16} color="inherit" />
            ) : (
              <RefreshCw size={18} />
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
                Driver
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
                      Unit {tx.house_block}-{tx.house_unit}
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
                        backgroundColor:
                          tx.status === "Paid" ? "#f0fdf4" : "#fef2f2",
                        color: tx.status === "Paid" ? "#166534" : "#991b1b",
                        fontWeight: 700,
                        fontSize: "0.6rem",
                        textTransform: "uppercase",
                      }}
                    >
                      {tx.status}
                    </Box>
                  </TableCell>
                  <TableCell align="right">
                    {tx.status !== "Paid" && (
                      <Button
                        size="small"
                        variant="text"
                        endIcon={<ArrowUpRight size={14} />}
                        onClick={() => processPayment(tx.trans_no)}
                        sx={{ fontWeight: 700, color: "#1e40af" }}
                      >
                        Process Pay
                      </Button>
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
