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
  CardContent,
  Grid,
  Tab,
  Tabs,
  Typography,
} from "@mui/material";
import { Activity, IndianRupee, Users, Wrench } from "lucide-react";
import React, { useState } from "react";
import ResidentManager from "./admin/ResidentManager";
import TechnicianManager from "./admin/TechnicianManager";
import FinanceManager from "./admin/FinanceManager";

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div role="tabpanel" hidden={value !== index} {...other}>
      {value === index && <Box sx={{ py: 3 }}>{children}</Box>}
    </div>
  );
}

const AdminDashboard: React.FC = () => {
  const [tabValue, setTabValue] = useState(0);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  return (
    <Box>
      <Box sx={{ mb: 6 }}>
        <Typography variant="h3" sx={{ fontWeight: 800, mb: 1 }}>
          Administrator Hub
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Monitor community metrics and manage critical infrastructure.
        </Typography>
      </Box>

      <Grid container spacing={3} sx={{ mb: 6 }}>
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Box
                sx={{ display: "flex", justifyContent: "space-between", mb: 2 }}
              >
                <Users size={20} color="#666" />
                <Typography
                  variant="caption"
                  sx={{ fontWeight: 700, color: "success.main" }}
                >
                  +5%
                </Typography>
              </Box>
              <Typography variant="h4" sx={{ fontWeight: 800 }}>
                1,280
              </Typography>
              <Typography
                variant="caption"
                color="text.secondary"
                sx={{ fontWeight: 600, textTransform: "uppercase" }}
              >
                Total Residents
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Box
                sx={{ display: "flex", justifyContent: "space-between", mb: 2 }}
              >
                <Activity size={20} color="#666" />
                <Typography
                  variant="caption"
                  sx={{ fontWeight: 700, color: "success.main" }}
                >
                  Live
                </Typography>
              </Box>
              <Typography variant="h4" sx={{ fontWeight: 800 }}>
                98.4%
              </Typography>
              <Typography
                variant="caption"
                color="text.secondary"
                sx={{ fontWeight: 600, textTransform: "uppercase" }}
              >
                Grid Uptime
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Box
                sx={{ display: "flex", justifyContent: "space-between", mb: 2 }}
              >
                <IndianRupee size={20} color="#666" />
                <Typography
                  variant="caption"
                  sx={{ fontWeight: 700, color: "warning.main" }}
                >
                  PENDING
                </Typography>
              </Box>
              <Typography variant="h4" sx={{ fontWeight: 800 }}>
                ₹42.5K
              </Typography>
              <Typography
                variant="caption"
                color="text.secondary"
                sx={{ fontWeight: 600, textTransform: "uppercase" }}
              >
                Unpaid Ledger
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Box
                sx={{ display: "flex", justifyContent: "space-between", mb: 2 }}
              >
                <Wrench size={20} color="#666" />
                <Typography
                  variant="caption"
                  sx={{ fontWeight: 700, color: "#666" }}
                >
                  ACTIVE
                </Typography>
              </Box>
              <Typography variant="h4" sx={{ fontWeight: 800 }}>
                12
              </Typography>
              <Typography
                variant="caption"
                color="text.secondary"
                sx={{ fontWeight: 600, textTransform: "uppercase" }}
              >
                Pending Tickets
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Box sx={{ borderBottom: 1, borderColor: "divider" }}>
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
              "&.Mui-selected": { color: "#000" },
            },
            "& .MuiTabs-indicator": { backgroundColor: "#000" },
          }}
        >
          <Tab label="Resident Directory" />
          <Tab label="Technical Crew" />
          <Tab label="Financial Ledger" />
        </Tabs>
      </Box>

      <TabPanel value={tabValue} index={0}>
        <ResidentManager />
      </TabPanel>
      <TabPanel value={tabValue} index={1}>
        <TechnicianManager />
      </TabPanel>
      <TabPanel value={tabValue} index={2}>
        <FinanceManager />
      </TabPanel>
    </Box>
  );
};

export default AdminDashboard;
