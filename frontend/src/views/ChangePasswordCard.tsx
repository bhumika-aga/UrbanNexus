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
  faEye,
  faEyeSlash,
  faLock,
  faSave,
} from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Divider,
  Grid,
  IconButton,
  InputAdornment,
  TextField,
  Typography,
} from "@mui/material";
import axios from "axios";
import React, { useState } from "react";
import api from "../api/axiosClient";
import { useAuth } from "../context/AuthContext";
import { LoginResponse } from "../types";

type FieldKey = "currentPassword" | "newPassword" | "confirmPassword";

const FIELDS: { key: FieldKey; label: string }[] = [
  { key: "currentPassword", label: "Current Password" },
  { key: "newPassword", label: "New Password" },
  { key: "confirmPassword", label: "Confirm New Password" },
];

interface Props {
  title?: string;
  description?: React.ReactNode;
  onSuccess?: () => void;
}

const ChangePasswordCard: React.FC<Props> = ({
  title = "Change Password",
  description,
  onSuccess,
}) => {
  const { login } = useAuth();
  const [form, setForm] = useState<Record<FieldKey, string>>({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [visible, setVisible] = useState<Record<FieldKey, boolean>>({
    currentPassword: false,
    newPassword: false,
    confirmPassword: false,
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const mismatch =
    !!form.confirmPassword && form.newPassword !== form.confirmPassword;

  const handleSubmit = async () => {
    setError("");
    if (form.newPassword !== form.confirmPassword) {
      setError("New passwords do not match.");
      return;
    }
    if (form.newPassword.length < 6) {
      setError("New password must be at least 6 characters.");
      return;
    }
    try {
      setSubmitting(true);
      const res = await api.put<LoginResponse>("/profile/password", {
        currentPassword: form.currentPassword,
        newPassword: form.newPassword,
      });
      setForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
      // The change revokes every earlier session token, so switch to the one returned.
      login(res.data.token);
      onSuccess?.();
    } catch (err) {
      const msg = axios.isAxiosError(err) ? err.response?.data?.error : null;
      setError(msg || "Failed to update password.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Card
      sx={{
        borderRadius: 4,
        border: "1px solid rgba(0,0,0,0.05)",
        boxShadow: "0 8px 32px rgba(0,0,0,0.04)",
        overflow: "visible",
      }}
    >
      <CardContent sx={{ p: 4 }}>
        <Box sx={{ display: "flex", alignItems: "center", gap: 1.5, mb: 3 }}>
          <FontAwesomeIcon icon={faLock} style={{ fontSize: 18 }} />
          <Typography variant="h6" sx={{ fontWeight: 700 }}>
            {title}
          </Typography>
        </Box>
        {description && (
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            {description}
          </Typography>
        )}
        <Divider sx={{ mb: 4 }} />
        {error && (
          <Alert severity="error" sx={{ mb: 3, borderRadius: "12px" }}>
            {error}
          </Alert>
        )}
        <Grid container spacing={3}>
          {FIELDS.map(({ key, label }) => (
            <Grid key={key} size={{ xs: 12, md: 4 }}>
              <TextField
                fullWidth
                label={label}
                type={visible[key] ? "text" : "password"}
                value={form[key]}
                onChange={(e) => setForm({ ...form, [key]: e.target.value })}
                error={key === "confirmPassword" && mismatch}
                helperText={
                  key === "confirmPassword" && mismatch
                    ? "Passwords do not match"
                    : ""
                }
                slotProps={{
                  input: {
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          size="small"
                          onClick={() =>
                            setVisible({ ...visible, [key]: !visible[key] })
                          }
                        >
                          <FontAwesomeIcon
                            icon={visible[key] ? faEyeSlash : faEye}
                            style={{ fontSize: 16 }}
                          />
                        </IconButton>
                      </InputAdornment>
                    ),
                  },
                }}
              />
            </Grid>
          ))}
          <Grid size={12}>
            <Button
              variant="contained"
              onClick={handleSubmit}
              disabled={
                submitting ||
                !form.currentPassword ||
                !form.newPassword ||
                !form.confirmPassword
              }
              startIcon={
                submitting ? (
                  <CircularProgress size={16} />
                ) : (
                  <FontAwesomeIcon icon={faSave} style={{ fontSize: 16 }} />
                )
              }
              sx={{ borderRadius: "8px", fontWeight: 700 }}
            >
              Update Password
            </Button>
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};

export default ChangePasswordCard;
