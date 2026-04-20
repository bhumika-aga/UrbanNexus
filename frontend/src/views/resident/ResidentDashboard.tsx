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
  CardActionArea,
  CardContent,
  Typography,
} from "@mui/material";
import Grid from "@mui/material/Grid2";
import { Calendar, CreditCard, History, Wrench } from "lucide-react";
import React, { useState } from "react";
import AmenityBooker from "./AmenityBooker";
import DuesManager from "./DuesManager";
import HistoryManager from "./HistoryManager";
import TechBooker from "./TechBooker";

const ResidentDashboard: React.FC = () => {
  const [activeView, setActiveView] = useState<
    "menu" | "dues" | "amenities" | "technicians" | "history"
  >("menu");

  const renderView = () => {
    switch (activeView) {
      case "dues":
        return <DuesManager onBack={() => setActiveView("menu")} />;
      case "history":
        return <HistoryManager onBack={() => setActiveView("menu")} />;
      case "amenities":
        return <AmenityBooker onBack={() => setActiveView("menu")} />;
      case "technicians":
        return <TechBooker onBack={() => setActiveView("menu")} />;
      default:
        return (
          <Box>
            <Box sx={{ mb: 6 }}>
              <Typography variant="h3" sx={{ fontWeight: 800, mb: 1 }}>
                Resident Portal
              </Typography>
              <Typography variant="body1" color="text.secondary">
                Securely manage your home services and financial grid status.
              </Typography>
            </Box>

            <Grid container spacing={4}>
              <Grid size={{ xs: 12, md: 6, lg: 3 }}>
                <Card sx={{ height: "100%" }}>
                  <CardActionArea
                    onClick={() => setActiveView("dues")}
                    sx={{ height: "100%", p: 2 }}
                  >
                    <CardContent>
                      <Box
                        sx={{
                          width: 48,
                          height: 48,
                          borderRadius: "12px",
                          backgroundColor: "#fef2f2",
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          mb: 3,
                        }}
                      >
                        <CreditCard color="#991b1b" />
                      </Box>
                      <Typography variant="h5" sx={{ fontWeight: 800, mb: 1 }}>
                        My Dues
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Pay pending invoices and view financial history.
                      </Typography>
                    </CardContent>
                  </CardActionArea>
                </Card>
              </Grid>

              <Grid size={{ xs: 12, md: 6, lg: 3 }}>
                <Card sx={{ height: "100%" }}>
                  <CardActionArea
                    onClick={() => setActiveView("amenities")}
                    sx={{ height: "100%", p: 2 }}
                  >
                    <CardContent>
                      <Box
                        sx={{
                          width: 48,
                          height: 48,
                          borderRadius: "12px",
                          backgroundColor: "#f0fdf4",
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          mb: 3,
                        }}
                      >
                        <Calendar color="#166534" />
                      </Box>
                      <Typography variant="h5" sx={{ fontWeight: 800, mb: 1 }}>
                        Facilities
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Reserve common amenities and track capacity.
                      </Typography>
                    </CardContent>
                  </CardActionArea>
                </Card>
              </Grid>

              <Grid size={{ xs: 12, md: 6, lg: 3 }}>
                <Card sx={{ height: "100%" }}>
                  <CardActionArea
                    onClick={() => setActiveView("technicians")}
                    sx={{ height: "100%", p: 2 }}
                  >
                    <CardContent>
                      <Box
                        sx={{
                          width: 48,
                          height: 48,
                          borderRadius: "12px",
                          backgroundColor: "primary.light",
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          mb: 3,
                        }}
                      >
                        <Wrench color="primary" />
                      </Box>
                      <Typography variant="h5" sx={{ fontWeight: 800, mb: 1 }}>
                        Staff Support
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Book on-call technicians for home repairs.
                      </Typography>
                    </CardContent>
                  </CardActionArea>
                </Card>
              </Grid>

              <Grid size={{ xs: 12, md: 6, lg: 3 }}>
                <Card sx={{ height: "100%" }}>
                  <CardActionArea
                    onClick={() => setActiveView("history")}
                    sx={{ height: "100%", p: 2 }}
                  >
                    <CardContent>
                      <Box
                        sx={{
                          width: 48,
                          height: 48,
                          borderRadius: "12px",
                          backgroundColor: "primary.light",
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          mb: 3,
                        }}
                      >
                        <History color="primary" />
                      </Box>
                      <Typography variant="h5" sx={{ fontWeight: 800, mb: 1 }}>
                        My Activity
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Audit your past bookings and service requests.
                      </Typography>
                    </CardContent>
                  </CardActionArea>
                </Card>
              </Grid>
            </Grid>
          </Box>
        );
    }
  };

  return <Box>{renderView()}</Box>;
};

export default ResidentDashboard;
