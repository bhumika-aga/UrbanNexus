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
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { ChevronLeft, CreditCard } from "lucide-react";
import React, { useEffect, useState } from "react";
import api from "../../api/axiosClient";
import { Transaction } from "../../types";

interface Props {
  onBack: () => void;
}

const DuesManager: React.FC<Props> = ({ onBack }) => {
  const [invoices, setInvoices] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchDues = async () => {
    setLoading(true);
    try {
      const res = await api.get("/residents/me/dues");
      setInvoices(res.data.invoices);
    } catch (err) {
      console.error("Failed to fetch dues");
    } finally {
      setLoading(false);
    }
  };

  const handlePay = async (transNo: string) => {
    try {
      await api.post(`/payments/${transNo}/pay`);
      alert("Payment confirmed! Ledger updated.");
      fetchDues();
    } catch (err) {
      alert("Payment failed");
    }
  };

  useEffect(() => {
    fetchDues();
  }, []);

  return (
    <Box>
      <Box sx={{ mb: 4, display: "flex", alignItems: "center", gap: 2 }}>
        <IconButton onClick={onBack} size="small">
          <ChevronLeft size={20} />
        </IconButton>
        <Typography variant="h4" sx={{ fontWeight: 800 }}>
          Pending Invoices
        </Typography>
      </Box>

      <TableContainer component={Paper} elevation={0}>
        <Table>
          <TableHead sx={{ backgroundColor: "#fafafa" }}>
            <TableRow>
              <TableCell
                sx={{
                  fontWeight: 700,
                  textTransform: "uppercase",
                  fontSize: "0.65rem",
                }}
              >
                TXN #
              </TableCell>
              <TableCell
                sx={{
                  fontWeight: 700,
                  textTransform: "uppercase",
                  fontSize: "0.65rem",
                }}
              >
                Service
              </TableCell>
              <TableCell
                sx={{
                  fontWeight: 700,
                  textTransform: "uppercase",
                  fontSize: "0.65rem",
                }}
              >
                Amount
              </TableCell>
              <TableCell
                align="right"
                sx={{
                  fontWeight: 700,
                  textTransform: "uppercase",
                  fontSize: "0.65rem",
                }}
              >
                Action
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={4} align="center" sx={{ py: 4 }}>
                  <CircularProgress size={24} />
                </TableCell>
              </TableRow>
            ) : invoices.length === 0 ? (
              <TableRow>
                <TableCell
                  colSpan={4}
                  align="center"
                  sx={{ py: 10, color: "#999", fontStyle: "italic" }}
                >
                  You are all caught up! No pending dues.
                </TableCell>
              </TableRow>
            ) : (
              invoices.map((inv) => (
                <TableRow key={inv.trans_no}>
                  <TableCell sx={{ fontFamily: "monospace" }}>
                    {inv.trans_no}
                  </TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>
                    {inv.service_type}
                  </TableCell>
                  <TableCell sx={{ fontWeight: 800, color: "#166534" }}>
                    ₹{inv.cost}
                  </TableCell>
                  <TableCell align="right">
                    <Button
                      variant="contained"
                      size="small"
                      color="success"
                      startIcon={<CreditCard size={14} />}
                      onClick={() => handlePay(inv.trans_no)}
                      sx={{ fontWeight: 700 }}
                    >
                      Pay Now
                    </Button>
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

export default DuesManager;
