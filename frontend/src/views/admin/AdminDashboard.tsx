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

import {Box, Card, CardContent, CircularProgress, Grid, Tab, Tabs, Typography,} from "@mui/material";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faChartLine, faIndianRupeeSign, faUsers, faWrench,} from "@fortawesome/free-solid-svg-icons";
import React, {useEffect, useState} from "react";
import api from "../../api/axiosClient";
import AssignmentManager from "./AssignmentManager";
import AuditLog from "./AuditLog";
import FacilityBookings from "./FacilityBookings";
import FinanceManager from "./FinanceManager";
import ResidentManager from "./ResidentManager";
import ServiceHistory from "./ServiceHistory";
import TechnicianManager from "./TechnicianManager";

interface DashboardStats {
    totalResidents: number;
    unpaidLedger: number;
    pendingTickets: number;
    gridUptime: string;
}

interface TabPanelProps {
    children?: React.ReactNode;
    index: number;
    value: number;
}

function TabPanel(props: TabPanelProps) {
    const {children, value, index, ...other} = props;
    return (
        <div role="tabpanel" hidden={value !== index} {...other}>
            {value === index && <Box sx={{py: 3}}>{children}</Box>}
        </div>
    );
}

const AdminDashboard: React.FC = () => {
    const [tabValue, setTabValue] = useState(0);
    const [stats, setStats] = useState<DashboardStats | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const response = await api.get("/admin/stats");
                setStats(response.data);
            } catch (error) {
                console.error("Failed to fetch dashboard stats:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchStats();
    }, []);

    const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
        setTabValue(newValue);
    };

    const formatCurrency = (amount: number) => {
        if (amount >= 1000) return `₹${(amount / 1000).toFixed(1)}K`;
        return `₹${amount}`;
    };

    return (
        <Box>
            <Box sx={{mb: 6}}>
                <Typography variant="h3" sx={{fontWeight: 800, mb: 1}}>
                    Administrator Hub
                </Typography>
                <Typography variant="body1" color="text.secondary">
                    Monitor community metrics and manage critical infrastructure.
                </Typography>
            </Box>

            <Grid container spacing={3} sx={{mb: 6}}>
                <Grid size={{xs: 12, md: 3}}>
                    <Card>
                        <CardContent>
                            <Box
                                sx={{display: "flex", justifyContent: "space-between", mb: 2}}
                            >
                                <FontAwesomeIcon
                                    icon={faUsers}
                                    style={{fontSize: 20, color: "#666"}}
                                />
                                <Typography
                                    variant="caption"
                                    sx={{fontWeight: 700, color: "success.main"}}
                                >
                                    LIVE
                                </Typography>
                            </Box>
                            <Typography variant="h4" sx={{fontWeight: 800}}>
                                {loading ? (
                                    <CircularProgress size={24}/>
                                ) : (
                                    stats?.totalResidents.toLocaleString()
                                )}
                            </Typography>
                            <Typography
                                variant="caption"
                                color="text.secondary"
                                sx={{fontWeight: 600, textTransform: "uppercase"}}
                            >
                                Total Residents
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>
                <Grid size={{xs: 12, md: 3}}>
                    <Card>
                        <CardContent>
                            <Box
                                sx={{display: "flex", justifyContent: "space-between", mb: 2}}
                            >
                                <FontAwesomeIcon
                                    icon={faChartLine}
                                    style={{fontSize: 20, color: "#666"}}
                                />
                                <Typography
                                    variant="caption"
                                    sx={{fontWeight: 700, color: "success.main"}}
                                >
                                    SYSTEM
                                </Typography>
                            </Box>
                            <Typography variant="h4" sx={{fontWeight: 800}}>
                                {loading ? <CircularProgress size={24}/> : stats?.gridUptime}
                            </Typography>
                            <Typography
                                variant="caption"
                                color="text.secondary"
                                sx={{fontWeight: 600, textTransform: "uppercase"}}
                            >
                                Infrastructure Health
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>
                <Grid size={{xs: 12, md: 3}}>
                    <Card>
                        <CardContent>
                            <Box
                                sx={{display: "flex", justifyContent: "space-between", mb: 2}}
                            >
                                <FontAwesomeIcon
                                    icon={faIndianRupeeSign}
                                    style={{fontSize: 20, color: "#666"}}
                                />
                                <Typography
                                    variant="caption"
                                    sx={{fontWeight: 700, color: "warning.main"}}
                                >
                                    LEDGER
                                </Typography>
                            </Box>
                            <Typography variant="h4" sx={{fontWeight: 800}}>
                                {loading ? (
                                    <CircularProgress size={24}/>
                                ) : (
                                    formatCurrency(stats?.unpaidLedger || 0)
                                )}
                            </Typography>
                            <Typography
                                variant="caption"
                                color="text.secondary"
                                sx={{fontWeight: 600, textTransform: "uppercase"}}
                            >
                                Unpaid Total
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>
                <Grid size={{xs: 12, md: 3}}>
                    <Card>
                        <CardContent>
                            <Box
                                sx={{display: "flex", justifyContent: "space-between", mb: 2}}
                            >
                                <FontAwesomeIcon
                                    icon={faWrench}
                                    style={{fontSize: 20, color: "#666"}}
                                />
                                <Typography
                                    variant="caption"
                                    sx={{fontWeight: 700, color: "error.main"}}
                                >
                                    PENDING
                                </Typography>
                            </Box>
                            <Typography variant="h4" sx={{fontWeight: 800}}>
                                {loading ? (
                                    <CircularProgress size={24}/>
                                ) : (
                                    stats?.pendingTickets
                                )}
                            </Typography>
                            <Typography
                                variant="caption"
                                color="text.secondary"
                                sx={{fontWeight: 600, textTransform: "uppercase"}}
                            >
                                Open Assignments
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

            <Box sx={{borderBottom: 1, borderColor: "divider"}}>
                <Tabs
                    value={tabValue}
                    onChange={handleTabChange}
                    sx={{
                        "& .MuiTab-root": {
                            fontWeight: 700,
                            fontSize: "0.85rem",
                            mr: 2,
                            minWidth: 0,
                            px: 1,
                            "&.Mui-selected": {color: "primary.main"},
                        },
                        "& .MuiTabs-indicator": {backgroundColor: "primary.main"},
                    }}
                >
                    <Tab label="Resident Directory"/>
                    <Tab label="Technical Crew"/>
                    <Tab label="Financial Ledger"/>
                    <Tab label="Open Assignments"/>
                    <Tab label="Facility Bookings"/>
                    <Tab label="Service History"/>
                    <Tab label="Audit Logs"/>
                </Tabs>
            </Box>

            <TabPanel value={tabValue} index={0}>
                <ResidentManager/>
            </TabPanel>
            <TabPanel value={tabValue} index={1}>
                <TechnicianManager/>
            </TabPanel>
            <TabPanel value={tabValue} index={2}>
                <FinanceManager/>
            </TabPanel>
            <TabPanel value={tabValue} index={3}>
                <AssignmentManager/>
            </TabPanel>
            <TabPanel value={tabValue} index={4}>
                <FacilityBookings/>
            </TabPanel>
            <TabPanel value={tabValue} index={5}>
                <ServiceHistory/>
            </TabPanel>
            <TabPanel value={tabValue} index={6}>
                <AuditLog/>
            </TabPanel>
        </Box>
    );
};

export default AdminDashboard;
