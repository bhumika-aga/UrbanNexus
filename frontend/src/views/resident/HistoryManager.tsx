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

import {Box, Chip, CircularProgress, Divider, IconButton, Paper, Stack, Typography,} from "@mui/material";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faCalendarAlt, faChevronLeft, faWrench,} from "@fortawesome/free-solid-svg-icons";
import React, {useEffect, useState} from "react";
import api from "../../api/axiosClient";
import {Booking} from "../../types";

interface Props {
    onBack: () => void;
}

const HistoryManager: React.FC<Props> = ({onBack}) => {
    const [bookings, setBookings] = useState<{
        amenities: Booking[];
        technicians: Booking[];
    }>({amenities: [], technicians: []});
    const [loading, setLoading] = useState(false);

    const fetchHistory = async () => {
        setLoading(true);
        try {
            const res = await api.get("/residents/me/bookings");
            setBookings(res.data);
        } catch (err) {
            console.error("Failed to fetch history");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchHistory();
    }, []);

    return (
        <Box>
            <Box sx={{mb: 4, display: "flex", alignItems: "center", gap: 2}}>
                <IconButton onClick={onBack} size="small">
                    <FontAwesomeIcon icon={faChevronLeft} style={{fontSize: 20}}/>
                </IconButton>
                <Typography variant="h4" sx={{fontWeight: 800}}>
                    My Activities
                </Typography>
            </Box>

            {loading ? (
                <Box sx={{display: "flex", justifyContent: "center", py: 10}}>
                    <CircularProgress/>
                </Box>
            ) : (
                <Stack spacing={6}>
                    <Box>
                        <Typography
                            variant="h6"
                            sx={{
                                fontWeight: 800,
                                mb: 3,
                                display: "flex",
                                alignItems: "center",
                                gap: 1,
                            }}
                        >
                            <FontAwesomeIcon icon={faCalendarAlt} style={{fontSize: 20, color: "#166534"}}/> Facility
                            Reservations
                        </Typography>
                        <Stack spacing={2}>
                            {bookings.amenities.length === 0 ? (
                                <Typography
                                    variant="body2"
                                    color="text.secondary"
                                    sx={{fontStyle: "italic"}}
                                >
                                    No reservations found.
                                </Typography>
                            ) : (
                                bookings.amenities.map((b) => (
                                    <Paper
                                        key={b.booking_id}
                                        sx={{
                                            p: 3,
                                            display: "flex",
                                            justifyContent: "space-between",
                                            alignItems: "center",
                                        }}
                                    >
                                        <Box>
                                            <Typography variant="subtitle1" sx={{fontWeight: 700}}>
                                                {b.amenity}
                                            </Typography>
                                            <Typography
                                                variant="caption"
                                                color="text.secondary"
                                                sx={{fontFamily: "monospace"}}
                                            >
                                                {b.date ? new Date(b.date).toLocaleDateString() : "N/A"}{" "}
                                                • SLOT {b.slot}
                                            </Typography>
                                        </Box>
                                        <Chip
                                            size="small"
                                            label={b.status}
                                            color={b.status === "Confirmed" ? "success" : "default"}
                                            sx={{fontWeight: 700, fontSize: "0.6rem"}}
                                        />
                                    </Paper>
                                ))
                            )}
                        </Stack>
                    </Box>

                    <Divider/>

                    <Box>
                        <Typography
                            variant="h6"
                            sx={{
                                fontWeight: 800,
                                mb: 3,
                                display: "flex",
                                alignItems: "center",
                                gap: 1,
                            }}
                        >
                            <FontAwesomeIcon icon={faWrench} style={{fontSize: 20, color: "#9a3412"}}/> Technical
                            Support Tickets
                        </Typography>
                        <Stack spacing={2}>
                            {bookings.technicians.length === 0 ? (
                                <Typography
                                    variant="body2"
                                    color="text.secondary"
                                    sx={{fontStyle: "italic"}}
                                >
                                    No tickets found.
                                </Typography>
                            ) : (
                                bookings.technicians.map((t) => (
                                    <Paper
                                        key={t.assignment_id}
                                        sx={{
                                            p: 3,
                                            display: "flex",
                                            justifyContent: "space-between",
                                            alignItems: "center",
                                        }}
                                    >
                                        <Box>
                                            <Typography variant="subtitle1" sx={{fontWeight: 700}}>
                                                {t.technician}{" "}
                                                <Typography
                                                    component="span"
                                                    variant="caption"
                                                    sx={{fontWeight: 500, color: "#666"}}
                                                >
                                                    ({t.skill})
                                                </Typography>
                                            </Typography>
                                            <Typography
                                                variant="caption"
                                                color="text.secondary"
                                                sx={{fontFamily: "monospace"}}
                                            >
                                                {t.assign_date
                                                    ? new Date(t.assign_date).toLocaleDateString()
                                                    : "N/A"}{" "}
                                                • SLOT {t.slot}
                                            </Typography>
                                        </Box>
                                        <Chip
                                            size="small"
                                            label={t.status}
                                            color={t.status === "Resolved" ? "success" : "warning"}
                                            sx={{fontWeight: 700, fontSize: "0.6rem"}}
                                        />
                                    </Paper>
                                ))
                            )}
                        </Stack>
                    </Box>
                </Stack>
            )}
        </Box>
    );
};

export default HistoryManager;
