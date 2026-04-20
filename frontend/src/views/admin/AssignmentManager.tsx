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
import React, {useEffect, useState} from "react";
import api from "../../api/axiosClient";
import {Booking} from "../../types";

const AssignmentManager: React.FC = () => {
    const [assignments, setAssignments] = useState<Booking[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchAssignments = async () => {
            try {
                const response = await api.get("/admin/assignments");
                setAssignments(response.data);
            } catch (error) {
                console.error("Failed to fetch assignments:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchAssignments();
    }, []);

    return (
        <Box>
            <Typography variant="h6" sx={{fontWeight: 800, mb: 3}}>
                Live Technician Assignments
            </Typography>
            <TableContainer component={Paper} elevation={0}>
                <Table>
                    <TableHead sx={{backgroundColor: "#fafafa"}}>
                        <TableRow>
                            <TableCell sx={{fontWeight: 700}}>RESIDENT</TableCell>
                            <TableCell sx={{fontWeight: 700}}>TECHNICIAN</TableCell>
                            <TableCell sx={{fontWeight: 700}}>SKILL</TableCell>
                            <TableCell sx={{fontWeight: 700}}>DATE</TableCell>
                            <TableCell sx={{fontWeight: 700}}>SLOT</TableCell>
                            <TableCell sx={{fontWeight: 700}}>STATUS</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {loading ? (
                            <TableRow>
                                <TableCell colSpan={6} align="center" sx={{py: 4}}>
                                    <CircularProgress size={24}/>
                                </TableCell>
                            </TableRow>
                        ) : assignments.length === 0 ? (
                            <TableRow>
                                <TableCell
                                    colSpan={6}
                                    align="center"
                                    sx={{py: 4, color: "#999"}}
                                >
                                    No active assignments found.
                                </TableCell>
                            </TableRow>
                        ) : (
                            assignments.map((as) => (
                                <TableRow key={as.assignment_id}>
                                    <TableCell sx={{fontWeight: 600}}>
                                        {as.resident_name || "N/A"}
                                    </TableCell>
                                    <TableCell>{as.technician}</TableCell>
                                    <TableCell>{as.skill}</TableCell>
                                    <TableCell>{as.assign_date}</TableCell>
                                    <TableCell>Slot {as.slot}</TableCell>
                                    <TableCell>
                                        <Box
                                            sx={{
                                                display: "inline-flex",
                                                px: 1,
                                                py: 0.5,
                                                borderRadius: 1,
                                                backgroundColor:
                                                    as.status === "Resolved" ? "#f0fdf4" : "#fff7ed",
                                                color: as.status === "Resolved" ? "#166534" : "#c2410c",
                                                fontWeight: 700,
                                                fontSize: "0.65rem",
                                                textTransform: "uppercase",
                                            }}
                                        >
                                            {as.status}
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

export default AssignmentManager;
