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
  IconButton,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import Grid from "@mui/material/Grid2";
import { ChevronLeft, Info } from "lucide-react";
import React, { useEffect, useState } from "react";
import api from "../../api/axiosClient";
import { Amenity } from "../../types";

interface Props {
  onBack: () => void;
}

const AmenityBooker: React.FC<Props> = ({ onBack }) => {
  const [amenities, setAmenities] = useState<Amenity[]>([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    amenity_id: "",
    date: new Date().toISOString().split("T")[0],
    slot: 1,
    capacity_booked: 1,
  });

  const fetchAmenities = async () => {
    setLoading(true);
    try {
      const res = await api.get("/admin/amenities");
      setAmenities(res.data);
    } catch (err) {
      console.error("Failed to fetch amenities");
    } finally {
      setLoading(false);
    }
  };

  const handleBook = async () => {
    setSubmitting(true);
    try {
      await api.post("/bookings/amenity", formData);
      alert("Reservation confirmed! Please check History for details.");
      onBack();
    } catch (err: unknown) {
      const errorMsg =
        err && typeof err === "object" && "response" in err
          ? (err as any).response?.data?.error
          : "Booking failed. Capacity might be full.";
      alert(errorMsg || "Booking failed. Capacity might be full.");
    } finally {
      setSubmitting(false);
    }
  };

  useEffect(() => {
    fetchAmenities();
  }, []);

  return (
    <Box>
      <Box sx={{ mb: 4, display: "flex", alignItems: "center", gap: 2 }}>
        <IconButton onClick={onBack} size="small">
          <ChevronLeft size={20} />
        </IconButton>
        <Typography variant="h4" sx={{ fontWeight: 800 }}>
          Reserve Facility
        </Typography>
      </Box>

      {loading ? (
        <Box sx={{ display: "flex", justifyContent: "center", py: 10 }}>
          <CircularProgress />
        </Box>
      ) : (
        <Grid container spacing={4}>
          <Grid size={{ xs: 12, md: 7 }}>
            <Grid container spacing={2}>
              {amenities.map((a) => (
                <Grid size={{ xs: 12, sm: 6 }} key={a.amenityId}>
                  <Card
                    sx={{
                      cursor: "pointer",
                      borderColor:
                        formData.amenity_id === a.amenityId.toString()
                          ? "primary.main"
                          : "divider",
                      backgroundColor:
                        formData.amenity_id === a.amenityId.toString()
                          ? "action.hover"
                          : "background.paper",
                      "&:hover": { borderColor: "primary.light" },
                    }}
                    onClick={() =>
                      setFormData({
                        ...formData,
                        amenity_id: a.amenityId.toString(),
                      })
                    }
                  >
                    <CardContent>
                      <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
                        {a.name}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        Max Capacity: {a.capacity} per slot
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          </Grid>

          <Grid size={{ xs: 12, md: 5 }}>
            <Paper sx={{ p: 4 }}>
              <Typography variant="h6" sx={{ fontWeight: 800, mb: 3 }}>
                Booking Details
              </Typography>
              <Stack spacing={3}>
                <TextField
                  fullWidth
                  type="date"
                  label="Select Date"
                  value={formData.date}
                  onChange={(e) =>
                    setFormData({ ...formData, date: e.target.value })
                  }
                  InputLabelProps={{ shrink: true }}
                />
                <TextField
                  fullWidth
                  select
                  label="Time Slot"
                  value={formData.slot}
                  onChange={(e) =>
                    setFormData({ ...formData, slot: parseInt(e.target.value) })
                  }
                >
                  <MenuItem value={1}>09:00 AM - 12:00 PM</MenuItem>
                  <MenuItem value={2}>12:00 PM - 03:00 PM</MenuItem>
                  <MenuItem value={3}>03:00 PM - 06:00 PM</MenuItem>
                  <MenuItem value={4}>06:00 PM - 09:00 PM</MenuItem>
                </TextField>
                <TextField
                  fullWidth
                  type="number"
                  label="Members Joining"
                  value={formData.capacity_booked}
                  inputProps={{ min: 1 }}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      capacity_booked: parseInt(e.target.value),
                    })
                  }
                />
                <Box
                  sx={{
                    p: 2,
                    backgroundColor: "#f8fafc",
                    borderRadius: 2,
                    border: "1px solid #e2e8f0",
                    display: "flex",
                    gap: 1.5,
                  }}
                >
                  <Info size={18} color="#64748b" />
                  <Typography variant="caption" color="text.secondary">
                    Booking will automatically generate a convenience invoice
                    for the monthly ledger.
                  </Typography>
                </Box>
                <Button
                  fullWidth
                  variant="contained"
                  size="large"
                  disabled={!formData.amenity_id || submitting}
                  onClick={handleBook}
                >
                  {submitting ? (
                    <CircularProgress size={24} color="inherit" />
                  ) : (
                    "Confirm Reservation"
                  )}
                </Button>
              </Stack>
            </Paper>
          </Grid>
        </Grid>
      )}
    </Box>
  );
};

export default AmenityBooker;
