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

import { faChevronLeft, faCreditCard } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  Box,
  Button,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
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
import React, { useEffect, useState } from "react";
import api from "../../api/axiosClient";
import { Transaction } from "../../types";

interface Props {
  onBack: () => void;
}

const STATUS_COLORS: Record<string, { bg: string; fg: string }> = {
  Pending: { bg: "#fff7ed", fg: "#c2410c" },
  Overdue: { bg: "#fef2f2", fg: "#991b1b" },
  "Under Review": { bg: "#eff6ff", fg: "#1d4ed8" },
};

const DuesManager: React.FC<Props> = ({ onBack }) => {
  const [invoices, setInvoices] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(false);
  // The invoice being reported as paid, and the reference the resident enters for it.
  const [payingInvoice, setPayingInvoice] = useState<Transaction | null>(null);
  const [reference, setReference] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const fetchDues = async () => {
    setLoading(true);
    try {
      const res = await api.get("/residents/me/dues");
      setInvoices(res.data.invoices);
    } catch {
      console.error("Failed to fetch dues");
    } finally {
      setLoading(false);
    }
  };

  const openSubmitDialog = (invoice: Transaction) => {
    setReference("");
    setPayingInvoice(invoice);
  };

  // There is no payment gateway: the resident reports an offline payment, and the invoice
  // stays unpaid ("Under Review") until the administrator confirms the money arrived.
  const handleSubmitPayment = async () => {
    if (!payingInvoice) return;
    setSubmitting(true);
    try {
      await api.post(`/payments/${payingInvoice.trans_no}/submit`, {
        reference: reference.trim(),
      });
      setPayingInvoice(null);
      alert(
        "Payment submitted for review. It will show as paid once the administrator confirms it.",
      );
      fetchDues();
    } catch (err) {
      const msg = axios.isAxiosError(err) ? err.response?.data?.error : null;
      alert(msg || "Could not submit the payment.");
    } finally {
      setSubmitting(false);
    }
  };

  useEffect(() => {
    fetchDues();
  }, []);

  return (
    <Box>
      <Box sx={{ mb: 4, display: "flex", alignItems: "center", gap: 2 }}>
        <IconButton onClick={onBack} size="small">
          <FontAwesomeIcon icon={faChevronLeft} style={{ fontSize: 20 }} />
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
                sx={{
                  fontWeight: 700,
                  textTransform: "uppercase",
                  fontSize: "0.65rem",
                }}
              >
                Status
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
                <TableCell colSpan={5} align="center" sx={{ py: 4 }}>
                  <CircularProgress size={24} />
                </TableCell>
              </TableRow>
            ) : invoices.length === 0 ? (
              <TableRow>
                <TableCell
                  colSpan={5}
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
                  <TableCell>
                    <Chip
                      label={inv.status}
                      size="small"
                      sx={{
                        fontWeight: 700,
                        backgroundColor: STATUS_COLORS[inv.status]?.bg,
                        color: STATUS_COLORS[inv.status]?.fg,
                      }}
                    />
                  </TableCell>
                  <TableCell align="right">
                    {inv.status === "Under Review" ? (
                      <Typography
                        variant="caption"
                        color="text.secondary"
                        sx={{ fontWeight: 600 }}
                      >
                        Awaiting admin confirmation
                        <br />
                        Ref: {inv.payment_reference}
                      </Typography>
                    ) : (
                      <Button
                        variant="contained"
                        size="small"
                        color="success"
                        startIcon={
                          <FontAwesomeIcon
                            icon={faCreditCard}
                            style={{ fontSize: 14 }}
                          />
                        }
                        onClick={() => openSubmitDialog(inv)}
                        sx={{ fontWeight: 700 }}
                      >
                        I've Paid
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog
        open={payingInvoice !== null}
        onClose={() => !submitting && setPayingInvoice(null)}
        fullWidth
        maxWidth="xs"
      >
        <DialogTitle sx={{ fontWeight: 800 }}>Report a payment</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Pay ₹{payingInvoice?.cost} for {payingInvoice?.trans_no} by UPI,
            bank transfer or cheque, then enter the transaction reference. The
            administrator will verify it and mark the invoice paid.
          </Typography>
          <TextField
            autoFocus
            fullWidth
            label="Payment reference"
            placeholder="e.g. UPI ref no. or cheque no."
            value={reference}
            onChange={(e) => setReference(e.target.value)}
            slotProps={{ htmlInput: { maxLength: 100 } }}
          />
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setPayingInvoice(null)} disabled={submitting}>
            Cancel
          </Button>
          <Button
            variant="contained"
            onClick={handleSubmitPayment}
            disabled={submitting || !reference.trim()}
            sx={{ fontWeight: 700 }}
          >
            Submit for review
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default DuesManager;
