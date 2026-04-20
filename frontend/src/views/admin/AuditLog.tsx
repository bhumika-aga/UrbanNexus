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
import {alpha} from "@mui/material/styles";
import {History} from "lucide-react";
import React, {useEffect, useState} from "react";
import api from "../../api/axiosClient";
import {AuditLogEntry} from "../../types";

const AuditLog: React.FC = () => {
    const [logs, setLogs] = useState<AuditLogEntry[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchLogs = async () => {
            try {
                const response = await api.get("/admin/audit-logs");
                setLogs(response.data);
            } catch (error) {
                console.error("Failed to fetch audit logs:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchLogs();
    }, []);

    const getActionColor = (action: string) => {
        switch (action) {
            case "ADD":
                return "#10b981";
            case "DELETE":
                return "#ef4444";
            case "UPDATE":
                return "#3b82f6";
            default:
                return "#6366f1";
        }
    };

    if (loading) {
        return (
            <Box sx={{display: "flex", justifyContent: "center", py: 8}}>
                <CircularProgress/>
            </Box>
        );
    }

    return (
        <Box>
            <Box sx={{mb: 4, display: "flex", alignItems: "center", gap: 2}}>
                <Box
                    sx={{
                        p: 1,
                        borderRadius: "10px",
                        bgcolor: "rgba(99, 102, 241, 0.1)",
                        color: "primary.main",
                    }}
                >
                    <History size={24}/>
                </Box>
                <Box>
                    <Typography variant="h5" sx={{fontWeight: 800}}>
                        System Audit Trail
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                        Verifiable history of administrative actions and system events.
                    </Typography>
                </Box>
            </Box>

            <Card
                sx={{
                    borderRadius: 4,
                    overflow: "hidden",
                    border: "1px solid rgba(0,0,0,0.05)",
                }}
            >
                <TableContainer component={Paper} elevation={0}>
                    <Table>
                        <TableHead sx={{bgcolor: "#f8fafc"}}>
                            <TableRow>
                                <TableCell sx={{fontWeight: 700, color: "#64748b"}}>
                                    ACTION
                                </TableCell>
                                <TableCell sx={{fontWeight: 700, color: "#64748b"}}>
                                    TABLE
                                </TableCell>
                                <TableCell sx={{fontWeight: 700, color: "#64748b"}}>
                                    RECORD ID
                                </TableCell>
                                <TableCell sx={{fontWeight: 700, color: "#64748b"}}>
                                    DETAILS
                                </TableCell>
                                <TableCell sx={{fontWeight: 700, color: "#64748b"}}>
                                    TIMESTAMP
                                </TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {logs.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={5} align="center" sx={{py: 4}}>
                                        <Typography color="text.secondary">
                                            No audit logs found.
                                        </Typography>
                                    </TableCell>
                                </TableRow>
                            ) : (
                                logs.map((log) => (
                                    <TableRow key={log.logId} hover>
                                        <TableCell>
                                            <Box
                                                sx={{
                                                    display: "inline-block",
                                                    px: 1,
                                                    py: 0.5,
                                                    borderRadius: "6px",
                                                    fontSize: "0.7rem",
                                                    fontWeight: 800,
                                                    bgcolor: alpha(getActionColor(log.actionType), 0.1),
                                                    color: getActionColor(log.actionType),
                                                }}
                                            >
                                                {log.actionType}
                                            </Box>
                                        </TableCell>
                                        <TableCell sx={{fontWeight: 600, fontSize: "0.85rem"}}>
                                            {log.tableAffected.toUpperCase()}
                                        </TableCell>
                                        <TableCell
                                            sx={{
                                                fontFamily: "monospace",
                                                fontSize: "0.8rem",
                                                color: "#64748b",
                                            }}
                                        >
                                            #{log.recordId}
                                        </TableCell>
                                        <TableCell sx={{fontSize: "0.85rem"}}>
                                            {log.details}
                                        </TableCell>
                                        <TableCell sx={{fontSize: "0.85rem", color: "#64748b"}}>
                                            {new Date(log.changedAt).toLocaleString()}
                                        </TableCell>
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>
            </Card>
        </Box>
    );
};

export default AuditLog;
