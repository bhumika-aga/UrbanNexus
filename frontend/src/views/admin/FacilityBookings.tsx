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

const FacilityBookings: React.FC = () => {
    const [bookings, setBookings] = useState<Booking[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchBookings = async () => {
            try {
                const response = await api.get("/admin/amenities/bookings");
                setBookings(response.data);
            } catch (error) {
                console.error("Failed to fetch facility bookings:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchBookings();
    }, []);

    return (
        <Box>
            <Typography variant="h6" sx={{fontWeight: 800, mb: 3}}>
                Facility Usage History
            </Typography>
            <TableContainer component={Paper} elevation={0}>
                <Table>
                    <TableHead sx={{backgroundColor: "#fafafa"}}>
                        <TableRow>
                            <TableCell sx={{fontWeight: 700}}>RESIDENT</TableCell>
                            <TableCell sx={{fontWeight: 700}}>FACILITY</TableCell>
                            <TableCell sx={{fontWeight: 700}}>DATE</TableCell>
                            <TableCell sx={{fontWeight: 700}}>SLOT</TableCell>
                            <TableCell sx={{fontWeight: 700}}>PEOPLE</TableCell>
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
                        ) : bookings.length === 0 ? (
                            <TableRow>
                                <TableCell
                                    colSpan={6}
                                    align="center"
                                    sx={{py: 4, color: "#999"}}
                                >
                                    No facility reservations found.
                                </TableCell>
                            </TableRow>
                        ) : (
                            bookings.map((b) => (
                                <TableRow key={b.booking_id}>
                                    <TableCell sx={{fontWeight: 600}}>
                                        {b.resident_name || "N/A"}
                                    </TableCell>
                                    <TableCell>{b.amenity}</TableCell>
                                    <TableCell>{b.date}</TableCell>
                                    <TableCell>Slot {b.slot}</TableCell>
                                    <TableCell>{(b as any).capacity_booked || 1}</TableCell>
                                    <TableCell>
                                        <Box
                                            sx={{
                                                display: "inline-flex",
                                                px: 1,
                                                py: 0.5,
                                                borderRadius: 1,
                                                backgroundColor: "#f0fdf4",
                                                color: "#166534",
                                                fontWeight: 700,
                                                fontSize: "0.65rem",
                                                textTransform: "uppercase",
                                            }}
                                        >
                                            {b.status}
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

export default FacilityBookings;
