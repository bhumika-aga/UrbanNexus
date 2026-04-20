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

import {Box, Button, Chip, CircularProgress, Stack, Switch, Tab, Tabs, Typography,} from "@mui/material";
import {alpha} from "@mui/material/styles";
import {CheckCircle2, Clock, MapPin, Radio} from "lucide-react";
import React, {useEffect, useState} from "react";
import api from "../../api/axiosClient";
import {Booking} from "../../types";
import HistoryManager from "./HistoryManager";
import StatsGrid from "./StatsGrid";

const TechnicianDashboard: React.FC = () => {
    const [tasks, setTasks] = useState<Booking[]>([]);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState(0);
    const [isAvailable, setIsAvailable] = useState(true);
    const [profileLoading, setProfileLoading] = useState(true);

    const fetchProfile = async () => {
        try {
            const res = await api.get("/technicians/me");
            setIsAvailable(res.data.available);
        } catch (err) {
            console.error("Profile sync failed");
        } finally {
            setProfileLoading(false);
        }
    };

    const fetchTasks = async () => {
        try {
            const res = await api.get("/technicians/me/tasks");
            setTasks(res.data);
        } catch (err) {
            console.error("Task sync failed");
        } finally {
            setLoading(false);
        }
    };

    const toggleAvailability = async () => {
        const newState = !isAvailable;
        try {
            await api.put("/technicians/me/availability", {available: newState});
            setIsAvailable(newState);
        } catch (err) {
            alert("Status update failed");
        }
    };

    const updateStatus = async (id: number, status: string) => {
        try {
            await api.put(`/technicians/tasks/${id}/status`, {status});
            fetchTasks();
        } catch (err) {
            alert("Status update failed");
        }
    };

    useEffect(() => {
        fetchProfile();
        fetchTasks();
    }, []);

    if (loading || profileLoading) {
        return (
            <Box sx={{display: "flex", justifyContent: "center", py: 10}}>
                <CircularProgress size={32} thickness={5}/>
            </Box>
        );
    }

    const activeTasks = tasks.filter((t) => t.status !== "Resolved");

    return (
        <Box>
            {/* Header Section */}
            <Box
                sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "flex-end",
                    mb: 6,
                }}
            >
                <Box>
                    <Typography variant="h3" sx={{mb: 1}}>
                        Field Ops Control
                    </Typography>
                    <Box sx={{display: "flex", alignItems: "center", gap: 1}}>
                        <Box
                            sx={{
                                width: 8,
                                height: 8,
                                borderRadius: "50%",
                                backgroundColor: isAvailable ? "#10b981" : "#94a3b8",
                                boxShadow: isAvailable ? "0 0 12px #10b981" : "none",
                                animation: isAvailable ? "pulse 2s infinite" : "none",
                                "@keyframes pulse": {
                                    "0%": {opacity: 1, transform: "scale(1)"},
                                    "50%": {opacity: 0.5, transform: "scale(1.5)"},
                                    "100%": {opacity: 1, transform: "scale(1)"},
                                },
                            }}
                        />
                        <Typography
                            variant="body2"
                            sx={{
                                fontWeight: 700,
                                color: "text.secondary",
                                textTransform: "uppercase",
                                letterSpacing: "0.05em",
                            }}
                        >
                            System {isAvailable ? "Online" : "Paused"}
                        </Typography>
                    </Box>
                </Box>

                <Box
                    sx={{
                        display: "flex",
                        alignItems: "center",
                        gap: 2,
                        p: 1,
                        px: 2,
                        borderRadius: "12px",
                        backgroundColor: alpha(isAvailable ? "#10b981" : "#000", 0.04),
                        border: "1px solid",
                        borderColor: alpha(isAvailable ? "#10b981" : "#000", 0.1),
                    }}
                >
                    <Typography variant="caption" sx={{fontWeight: 800}}>
                        DUTY STATUS
                    </Typography>
                    <Switch
                        checked={isAvailable}
                        onChange={toggleAvailability}
                        color="success"
                        size="small"
                    />
                </Box>
            </Box>

            {/* Metrics Section */}
            <StatsGrid tasks={tasks}/>

            {/* Main Content Area */}
            <Box sx={{borderBottom: 1, borderColor: "divider", mb: 4}}>
                <Tabs
                    value={activeTab}
                    onChange={(_, v) => setActiveTab(v)}
                    variant="scrollable"
                    scrollButtons="auto"
                >
                    <Tab
                        icon={<Radio size={18}/>}
                        iconPosition="start"
                        label="Live Feed"
                    />
                    <Tab
                        icon={<Clock size={18}/>}
                        iconPosition="start"
                        label="Archive"
                    />
                </Tabs>
            </Box>

            {activeTab === 0 ? (
                <Stack spacing={3}>
                    {activeTasks.length === 0 ? (
                        <Box
                            sx={{
                                p: 8,
                                textAlign: "center",
                                borderRadius: 4,
                                backgroundColor: alpha("#6366f1", 0.02),
                                border: "2px dashed",
                                borderColor: alpha("#6366f1", 0.1),
                            }}
                        >
                            <Typography variant="h6" sx={{color: "text.secondary", mb: 1}}>
                                All Clear
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                No active service requests in your queue.
                            </Typography>
                        </Box>
                    ) : (
                        activeTasks.map((task) => (
                            <Box
                                key={task.assignment_id}
                                sx={{
                                    p: 3,
                                    borderRadius: 4,
                                    backgroundColor: "#ffffff",
                                    border: "1px solid",
                                    borderColor: "divider",
                                    display: "flex",
                                    justifyContent: "space-between",
                                    alignItems: "center",
                                    transition: "all 0.2s ease",
                                    "&:hover": {
                                        borderColor: "#6366f1",
                                        transform: "translateY(-2px)",
                                        boxShadow: "0 12px 24px rgba(99, 102, 241, 0.06)",
                                    },
                                }}
                            >
                                <Box>
                                    <Box
                                        sx={{
                                            display: "flex",
                                            alignItems: "center",
                                            gap: 1.5,
                                            mb: 1,
                                        }}
                                    >
                                        <Chip
                                            label={`Unit ${(task as any).house_block}-${(task as any).house_unit}`}
                                            size="small"
                                            sx={{
                                                backgroundColor: alpha("#6366f1", 0.08),
                                                color: "#6366f1",
                                                fontWeight: 800,
                                                fontSize: "0.7rem",
                                            }}
                                        />
                                        <Typography
                                            variant="caption"
                                            sx={{color: "text.secondary", fontWeight: 600}}
                                        >
                                            ID #{task.assignment_id}
                                        </Typography>
                                    </Box>
                                    <Typography variant="h6" sx={{fontWeight: 800, mb: 1}}>
                                        {(task as any).resident_name}
                                    </Typography>
                                    <Box sx={{display: "flex", alignItems: "center", gap: 3}}>
                                        <Box
                                            sx={{
                                                display: "flex",
                                                alignItems: "center",
                                                gap: 0.5,
                                                color: "#64748b",
                                            }}
                                        >
                                            <Clock size={14}/>
                                            <Typography variant="caption" sx={{fontWeight: 600}}>
                                                Slot {task.slot}
                                            </Typography>
                                        </Box>
                                        <Box
                                            sx={{
                                                display: "flex",
                                                alignItems: "center",
                                                gap: 0.5,
                                                color: "#64748b",
                                            }}
                                        >
                                            <MapPin size={14}/>
                                            <Typography variant="caption" sx={{fontWeight: 600}}>
                                                Ground Support
                                            </Typography>
                                        </Box>
                                    </Box>
                                </Box>
                                <Button
                                    variant="contained"
                                    startIcon={<CheckCircle2 size={18}/>}
                                    onClick={() => updateStatus(task.assignment_id!, "Resolved")}
                                >
                                    Resolve Task
                                </Button>
                            </Box>
                        ))
                    )}
                </Stack>
            ) : (
                <HistoryManager tasks={tasks}/>
            )}
        </Box>
    );
};

export default TechnicianDashboard;
