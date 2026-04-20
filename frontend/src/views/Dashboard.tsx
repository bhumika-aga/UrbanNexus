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
  AppBar,
  Avatar,
  Box,
  Button,
  Chip,
  Container,
  Toolbar,
  Tooltip,
  Typography,
} from "@mui/material";
import { LayoutDashboard, LogOut, User } from "lucide-react";
import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import AdminDashboard from "./admin/AdminDashboard";
import ResidentDashboard from "./resident/ResidentDashboard";
import TechnicianDashboard from "./technician/TechnicianDashboard";

const Dashboard: React.FC = () => {
  const { user, token, logout, isLoading } = useAuth();

  if (isLoading) {
    return null; // Or a loading spinner
  }

  if (!token || !user) {
    return <Navigate to="/login" replace />;
  }

  const renderDashboard = () => {
    switch (user.role) {
      case "SuperAdmin":
        return <AdminDashboard />;
      case "Resident":
        return <ResidentDashboard />;
      case "Technician":
        return <TechnicianDashboard />;
      default:
        return <Typography>Unknown Role</Typography>;
    }
  };

  return (
    <Box sx={{ minHeight: "100vh", backgroundColor: "#fafafa" }}>
      <AppBar position="sticky" elevation={0}>
        <Container maxWidth="lg">
          <Toolbar disableGutters sx={{ justifyContent: "space-between" }}>
            <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
              <Box
                sx={{
                  width: 32,
                  height: 32,
                  borderRadius: "6px",
                  backgroundColor: "primary.main",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                }}
              >
                <LayoutDashboard size={18} color="white" />
              </Box>
              <Typography
                variant="h6"
                noWrap
                sx={{
                  fontWeight: 800,
                  letterSpacing: "-0.02em",
                  textTransform: "uppercase",
                  fontSize: "1.1rem",
                }}
              >
                UrbanNexus
              </Typography>
            </Box>

            <Box sx={{ display: "flex", alignItems: "center", gap: 3 }}>
              <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                <Chip
                  label={user.role}
                  size="small"
                  sx={{
                    fontWeight: 700,
                    fontSize: "0.65rem",
                    textTransform: "uppercase",
                    letterSpacing: "0.05em",
                    backgroundColor: "#f0f0f0",
                    color: "#666",
                    height: 20,
                  }}
                />
                <Tooltip title={user.username}>
                  <Avatar
                    sx={{
                      width: 28,
                      height: 28,
                      fontSize: "0.8rem",
                      backgroundColor: "primary.main",
                      cursor: "pointer",
                    }}
                  >
                    <User size={14} />
                  </Avatar>
                </Tooltip>
              </Box>

              <Button
                size="small"
                onClick={logout}
                startIcon={<LogOut size={16} />}
                sx={{
                  color: "#666",
                  fontWeight: 600,
                  "&:hover": {
                    color: "primary.main",
                    backgroundColor: "transparent",
                  },
                }}
              >
                Logout
              </Button>
            </Box>
          </Toolbar>
        </Container>
      </AppBar>

      <Container maxWidth="lg" sx={{ py: 6 }}>
        {renderDashboard()}
      </Container>
    </Box>
  );
};

export default Dashboard;
