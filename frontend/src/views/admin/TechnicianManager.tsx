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
  Grid,
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
  Typography,
} from "@mui/material";
import { Hash, Phone, UserPlus, Wrench } from "lucide-react";
import React, { useEffect, useState } from "react";
import api from "../../api/axiosClient";
import { Technician } from "../../types";

const TechnicianManager: React.FC = () => {
  const [techs, setTechs] = useState<Technician[]>([]);
  const [loading, setLoading] = useState(false);
  const [openModal, setOpenModal] = useState(false);
  const [formData, setFormData] = useState({
    techId: "",
    name: "",
    contact: "",
    skill: "Plumber",
  });

  const fetchTechs = async () => {
    setLoading(true);
    try {
      const res = await api.get("/admin/technicians");
      setTechs(res.data);
    } catch (err: unknown) {
      const errorMsg =
        err && typeof err === "object" && "response" in err
          ? (err as { response?: { data?: { error?: string } } }).response?.data
              ?.error
          : "Processing failed";
      alert(errorMsg || "Processing failed");
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = async () => {
    try {
      await api.post("/technicians", formData);
      setOpenModal(false);
      setFormData({ techId: "", name: "", contact: "", skill: "Plumber" });
      fetchTechs();
    } catch (err: unknown) {
      const errorMsg =
        err && typeof err === "object" && "response" in err
          ? (err as any).response?.data?.error
          : "Action failed.";
      alert("Failed to sign crew: " + (errorMsg || "Unknown error"));
    }
  };

  useEffect(() => {
    fetchTechs();
  }, []);

  return (
    <Box>
      <Box
        sx={{
          mb: 4,
          display: "flex",
          justifyContent: "flex-end",
          alignItems: "center",
        }}
      >
        <Button
          variant="contained"
          color="primary"
          startIcon={<UserPlus size={18} />}
          onClick={() => setOpenModal(true)}
          sx={{
            backgroundColor: "#000",
            "&:hover": { backgroundColor: "#333" },
          }}
        >
          Recruit Crew
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
                ID
              </TableCell>
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
                Skill
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
            ) : techs.length === 0 ? (
              <TableRow>
                <TableCell
                  colSpan={5}
                  align="center"
                  sx={{ py: 4, color: "#999", fontStyle: "italic" }}
                >
                  No technical crew registered.
                </TableCell>
              </TableRow>
            ) : (
              techs.map((tech) => (
                <TableRow key={tech.techId} hover>
                  <TableCell sx={{ fontFamily: "monospace" }}>
                    {tech.techId}
                  </TableCell>
                  <TableCell sx={{ fontWeight: 600 }}>{tech.name}</TableCell>
                  <TableCell>
                    <Typography
                      variant="caption"
                      sx={{
                        px: 1,
                        py: 0.5,
                        borderRadius: 1,
                        backgroundColor: "#f8fafc",
                        color: "#475569",
                        fontWeight: 700,
                        fontSize: "0.6rem",
                        textTransform: "uppercase",
                        border: "1px solid #e2e8f0",
                      }}
                    >
                      {tech.skill}
                    </Typography>
                  </TableCell>
                  <TableCell sx={{ fontFamily: "monospace", color: "#666" }}>
                    {tech.contact}
                  </TableCell>
                  <TableCell align="right">
                    <Typography variant="caption" color="text.secondary">
                      Ready for assignment
                    </Typography>
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
        maxWidth="xs"
        fullWidth
      >
        <DialogTitle sx={{ fontWeight: 800 }}>
          Sign New Technical Staff
        </DialogTitle>
        <DialogContent dividers>
          <Grid container spacing={3} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Staff ID"
                placeholder="e.g. 101"
                value={formData.techId}
                onChange={(e) =>
                  setFormData({ ...formData, techId: e.target.value })
                }
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <Hash size={16} />
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Full Name"
                value={formData.name}
                onChange={(e) =>
                  setFormData({ ...formData, name: e.target.value })
                }
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Contact"
                value={formData.contact}
                onChange={(e) =>
                  setFormData({ ...formData, contact: e.target.value })
                }
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <Phone size={16} />
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                select
                label="Primary Skill"
                value={formData.skill}
                onChange={(e) =>
                  setFormData({ ...formData, skill: e.target.value })
                }
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <Wrench size={16} />
                    </InputAdornment>
                  ),
                }}
              >
                <MenuItem value="Plumber">Plumber</MenuItem>
                <MenuItem value="Electrician">Electrician</MenuItem>
                <MenuItem value="Maintenance">Maintenance</MenuItem>
                <MenuItem value="Carpenter">Carpenter</MenuItem>
              </TextField>
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
            disabled={!formData.name || !formData.techId}
          >
            Deploy Staff
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default TechnicianManager;
