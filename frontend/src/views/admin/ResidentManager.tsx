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
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  InputAdornment,
  MenuItem,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from "@mui/material";
import Grid from "@mui/material/Grid2";
import {
  Home,
  Phone,
  Search,
  Trash2,
  User as UserIcon,
  UserPlus,
  Users,
} from "lucide-react";
import React, { useEffect, useState } from "react";
import api from "../../api/axiosClient";
import { Resident } from "../../types";

const ResidentManager: React.FC = () => {
  const [residents, setResidents] = useState<Resident[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [loading, setLoading] = useState(false);
  const [openModal, setOpenModal] = useState(false);
  const [formData, setFormData] = useState({
    name: "",
    houseBlock: "",
    houseFloor: "",
    houseUnit: "",
    ownershipStatus: "Owner",
    contact: "",
    noOfMembers: 1,
  });

  const fetchResidents = async () => {
    setLoading(true);
    try {
      const res = await api.get(`/admin/residents/search?name=${searchTerm}`);
      setResidents(res.data || []);
    } catch (err) {
      console.error("Resident lookup failed");
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = async () => {
    try {
      await api.post("/residents", formData);
      setOpenModal(false);
      fetchResidents();
    } catch (err) {
      alert("Failed to add resident");
    }
  };

  const handleDelete = async (id: number, name: string) => {
    if (
      window.confirm(
        `Are you sure you want to remove ${name} from the community?`,
      )
    ) {
      try {
        await api.delete(`/residents/${id}`);
        fetchResidents();
      } catch (err) {
        alert("Delete failed");
      }
    }
  };

  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      fetchResidents();
    }, 300);
    return () => clearTimeout(delayDebounceFn);
  }, [searchTerm]);

  return (
    <Box>
      <Box
        sx={{
          mb: 4,
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <TextField
          size="small"
          placeholder="Search residents..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          sx={{ width: 400 }}
          slotProps={{
            input: {
              startAdornment: (
                <InputAdornment position="start">
                  <Search size={16} />
                </InputAdornment>
              ),
            },
          }}
        />
        <Button
          variant="contained"
          startIcon={<UserPlus size={18} />}
          onClick={() => setOpenModal(true)}
        >
          Sign New Resident
        </Button>
      </Box>

      <TableContainer component={Paper} elevation={0}>
        <Table sx={{ minWidth: 650 }}>
          <TableHead sx={{ backgroundColor: "#fafafa" }}>
            <TableRow>
              <TableCell
                sx={{
                  fontWeight: 700,
                  textTransform: "uppercase",
                  fontSize: "0.65rem",
                  color: "#666",
                }}
              >
                Name
              </TableCell>
              <TableCell
                sx={{
                  fontWeight: 700,
                  textTransform: "uppercase",
                  fontSize: "0.65rem",
                  color: "#666",
                }}
              >
                Unit
              </TableCell>
              <TableCell
                sx={{
                  fontWeight: 700,
                  textTransform: "uppercase",
                  fontSize: "0.65rem",
                  color: "#666",
                }}
              >
                Status
              </TableCell>
              <TableCell
                sx={{
                  fontWeight: 700,
                  textTransform: "uppercase",
                  fontSize: "0.65rem",
                  color: "#666",
                }}
              >
                Contact
              </TableCell>
              <TableCell
                sx={{
                  fontWeight: 700,
                  textTransform: "uppercase",
                  fontSize: "0.65rem",
                  color: "#666",
                }}
                align="right"
              >
                Actions
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={5} align="center" sx={{ py: 4 }}>
                  <CircularProgress size={24} color="inherit" />
                </TableCell>
              </TableRow>
            ) : residents.length === 0 ? (
              <TableRow>
                <TableCell
                  colSpan={5}
                  align="center"
                  sx={{ py: 4, color: "#999", fontStyle: "italic" }}
                >
                  No residents found in the records.
                </TableCell>
              </TableRow>
            ) : (
              residents.map((res) => (
                <TableRow key={res.residentId} hover>
                  <TableCell sx={{ fontWeight: 600 }}>{res.name}</TableCell>
                  <TableCell sx={{ fontFamily: "monospace" }}>
                    {res.houseBlock}-{res.houseUnit}
                  </TableCell>
                  <TableCell>
                    <Typography
                      variant="caption"
                      sx={{
                        px: 1,
                        py: 0.5,
                        borderRadius: 1,
                        backgroundColor:
                          res.ownershipStatus === "Owner"
                            ? "#f0fdf4"
                            : "#eff6ff",
                        color:
                          res.ownershipStatus === "Owner"
                            ? "#166534"
                            : "#1e40af",
                        fontWeight: 700,
                        fontSize: "0.6rem",
                        textTransform: "uppercase",
                      }}
                    >
                      {res.ownershipStatus}
                    </Typography>
                  </TableCell>
                  <TableCell sx={{ fontFamily: "monospace", color: "#666" }}>
                    {res.contact}
                  </TableCell>
                  <TableCell align="right">
                    <Tooltip title="Delete resident">
                      <IconButton
                        size="small"
                        color="error"
                        onClick={() => handleDelete(res.residentId, res.name)}
                      >
                        <Trash2 size={16} />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog
        open={openModal}
        onClose={() => setOpenModal(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle sx={{ fontWeight: 800 }}>
          Register New Resident
        </DialogTitle>
        <DialogContent dividers>
          <Grid container spacing={3} sx={{ mt: 1 }}>
            <Grid size={12}>
              <TextField
                fullWidth
                label="Full Name"
                value={formData.name}
                onChange={(e) =>
                  setFormData({ ...formData, name: e.target.value })
                }
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <UserIcon size={16} />
                      </InputAdornment>
                    ),
                  },
                }}
              />
            </Grid>
            <Grid size={4}>
              <TextField
                fullWidth
                label="Block"
                value={formData.houseBlock}
                onChange={(e) =>
                  setFormData({ ...formData, houseBlock: e.target.value })
                }
              />
            </Grid>
            <Grid size={4}>
              <TextField
                fullWidth
                label="Floor"
                value={formData.houseFloor}
                onChange={(e) =>
                  setFormData({ ...formData, houseFloor: e.target.value })
                }
              />
            </Grid>
            <Grid size={4}>
              <TextField
                fullWidth
                label="Unit"
                value={formData.houseUnit}
                onChange={(e) =>
                  setFormData({ ...formData, houseUnit: e.target.value })
                }
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <Home size={16} />
                      </InputAdornment>
                    ),
                  },
                }}
              />
            </Grid>
            <Grid size={12}>
              <TextField
                fullWidth
                label="Contact Number"
                value={formData.contact}
                onChange={(e) =>
                  setFormData({ ...formData, contact: e.target.value })
                }
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <Phone size={16} />
                      </InputAdornment>
                    ),
                  },
                }}
              />
            </Grid>
            <Grid size={6}>
              <TextField
                fullWidth
                select
                label="Status"
                value={formData.ownershipStatus}
                onChange={(e) =>
                  setFormData({ ...formData, ownershipStatus: e.target.value })
                }
              >
                <MenuItem value="Owner">Owner</MenuItem>
                <MenuItem value="Tenant">Tenant</MenuItem>
              </TextField>
            </Grid>
            <Grid size={6}>
              <TextField
                fullWidth
                type="number"
                label="Members"
                value={formData.noOfMembers}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    noOfMembers: parseInt(e.target.value),
                  })
                }
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <Users size={16} />
                      </InputAdornment>
                    ),
                  },
                }}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <Button onClick={() => setOpenModal(false)} variant="outlined">
            Cancel
          </Button>
          <Button
            onClick={handleAdd}
            variant="contained"
            disabled={!formData.name || !formData.contact}
          >
            Add to Community
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ResidentManager;
