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
    Card,
    CardContent,
    CircularProgress,
    Grid,
    IconButton,
    MenuItem,
    Paper,
    Stack,
    TextField,
    Typography,
} from "@mui/material";

import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faChevronLeft, faInfoCircle, faWrench,} from "@fortawesome/free-solid-svg-icons";
import React, {useState} from "react";
import api from "../../api/axiosClient";

interface Props {
    onBack: () => void;
}

const techSkills = ["Plumber", "Electrician", "Carpenter", "Maintenance"];

const TechBooker: React.FC<Props> = ({onBack}) => {
    const [submitting, setSubmitting] = useState(false);
    const [formData, setFormData] = useState({
        skill: "",
        assign_date: new Date().toISOString().split("T")[0],
        slot: 1,
    });

    const handleBook = async () => {
        setSubmitting(true);
        try {
            await api.post("/bookings/technician", formData);
            alert(
                "Support ticket created! A technician will be assigned according to the community grid availability.",
            );
            onBack();
        } catch (err: unknown) {
            const errorMsg =
                err && typeof err === "object" && "response" in err
                    ? (err as any).response?.data?.error
                    : "Failed to request technician.";
            alert(errorMsg || "Failed to request technician.");
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Box>
            <Box sx={{mb: 4, display: "flex", alignItems: "center", gap: 2}}>
                <IconButton onClick={onBack} size="small">
                    <FontAwesomeIcon icon={faChevronLeft} style={{fontSize: 20}}/>
                </IconButton>
                <Typography variant="h4" sx={{fontWeight: 800}}>
                    Request Technical Support
                </Typography>
            </Box>

            <Grid container spacing={4}>
                <Grid size={{xs: 12, md: 7}}>
                    <Grid container spacing={2}>
                        {techSkills.map((skill) => (
                            <Grid size={{xs: 12, sm: 6}} key={skill}>
                                <Card
                                    sx={{
                                        cursor: "pointer",
                                        borderColor:
                                            formData.skill === skill ? "primary.main" : "divider",
                                        backgroundColor:
                                            formData.skill === skill
                                                ? "action.hover"
                                                : "background.paper",
                                        "&:hover": {borderColor: "primary.light"},
                                    }}
                                    onClick={() => setFormData({...formData, skill})}
                                >
                                    <CardContent
                                        sx={{display: "flex", alignItems: "center", gap: 2}}
                                    >
                                        <Box
                                            sx={{
                                                p: 1,
                                                backgroundColor: "action.selected",
                                                borderRadius: "8px",
                                                color:
                                                    formData.skill === skill
                                                        ? "primary.main"
                                                        : "text.secondary",
                                            }}
                                        >
                                            <FontAwesomeIcon
                                                icon={faWrench}
                                                style={{fontSize: 20}}
                                            />
                                        </Box>
                                        <Typography
                                            variant="subtitle1"
                                            sx={{
                                                fontWeight: 700,
                                                color:
                                                    formData.skill === skill
                                                        ? "primary.main"
                                                        : "text.primary",
                                            }}
                                        >
                                            {skill}
                                        </Typography>
                                    </CardContent>
                                </Card>
                            </Grid>
                        ))}
                    </Grid>
                </Grid>

                <Grid size={{xs: 12, md: 5}}>
                    <Paper sx={{p: 4}}>
                        <Typography variant="h6" sx={{fontWeight: 800, mb: 3}}>
                            Service Information
                        </Typography>
                        <Stack spacing={3}>
                            <TextField
                                fullWidth
                                type="date"
                                label="Preferred Date"
                                value={formData.assign_date}
                                onChange={(e) =>
                                    setFormData({...formData, assign_date: e.target.value})
                                }
                                slotProps={{inputLabel: {shrink: true}}}
                            />
                            <TextField
                                fullWidth
                                select
                                label="Availability Slot"
                                value={formData.slot}
                                onChange={(e) =>
                                    setFormData({...formData, slot: parseInt(e.target.value)})
                                }
                            >
                                <MenuItem value={1}>Morning (09:00 - 12:00)</MenuItem>
                                <MenuItem value={2}>Afternoon (12:00 - 15:00)</MenuItem>
                                <MenuItem value={3}>Evening (15:00 - 18:00)</MenuItem>
                            </TextField>
                            <Box
                                sx={{
                                    p: 2,
                                    backgroundColor: "#fffbeb",
                                    borderRadius: 2,
                                    border: "1px solid #fef3c7",
                                    display: "flex",
                                    gap: 1.5,
                                }}
                            >
                                <FontAwesomeIcon
                                    icon={faInfoCircle}
                                    style={{fontSize: 18, color: "#f59e0b"}}
                                />
                                <Typography
                                    variant="caption"
                                    sx={{color: "#92400e", fontWeight: 500}}
                                >
                                    The community engine will auto-allocate a technician based on
                                    skill-concurrency and available pits.
                                </Typography>
                            </Box>
                            <Button
                                fullWidth
                                variant="contained"
                                size="large"
                                disabled={!formData.skill || submitting}
                                onClick={handleBook}
                                sx={{
                                    py: 1.5,
                                    fontWeight: 800,
                                    "&.Mui-disabled": {
                                        backgroundColor: "rgba(0,0,0,0.06)",
                                        color: "rgba(0,0,0,0.2)",
                                    },
                                }}
                            >
                                {submitting ? (
                                    <CircularProgress size={24} color="inherit"/>
                                ) : (
                                    "Deploy Support Ticket"
                                )}
                            </Button>
                        </Stack>
                    </Paper>
                </Grid>
            </Grid>
        </Box>
    );
};

export default TechBooker;
