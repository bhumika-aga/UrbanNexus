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
    Alert,
    Avatar,
    Box,
    Button,
    Card,
    CardContent,
    CircularProgress,
    Divider,
    Snackbar,
    Switch,
    TextField,
    Typography,
} from "@mui/material";
import Grid from "@mui/material/Grid2";
import {BadgeCheck, Save, X} from "lucide-react";
import React, {useEffect, useState} from "react";
import api from "../api/axiosClient";
import {useAuth} from "../context/AuthContext";

const ProfileView: React.FC = () => {
    const {user} = useAuth();
    const [profileData, setProfileData] = useState<any>(null);
    const [loading, setLoading] = useState(true);
    const [editing, setEditing] = useState(false);
    const [formData, setFormData] = useState<any>({});
    const [saving, setSaving] = useState(false);
    const [snackbar, setSnackbar] = useState({
        open: false,
        message: "",
        severity: "success" as "success" | "error",
    });

    useEffect(() => {
        fetchProfile();
    }, []);

    const fetchProfile = async () => {
        try {
            setLoading(true);
            const response = await api.get("/profile/me");
            setProfileData(response.data);
            setFormData(response.data);
        } catch (error) {
            console.error("Failed to fetch profile:", error);
            showSnackbar("Failed to load profile data", "error");
        } finally {
            setLoading(false);
        }
    };

    const showSnackbar = (message: string, severity: "success" | "error") => {
        setSnackbar({open: true, message, severity});
    };

    const handleUpdate = async () => {
        try {
            setSaving(true);
            await api.put("/profile/update", formData);
            setProfileData(formData);
            setEditing(false);
            showSnackbar("Profile updated successfully!", "success");
        } catch (error) {
            console.error("Update failed:", error);
            showSnackbar("Failed to update profile", "error");
        } finally {
            setSaving(false);
        }
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData({...formData, [e.target.name]: e.target.value});
    };

    if (loading) {
        return (
            <Box
                sx={{
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                    height: "400px",
                }}
            >
                <CircularProgress/>
            </Box>
        );
    }

    const isResident = user?.role === "Resident";
    const isTechnician = user?.role === "Technician";
    const isAdmin = user?.role === "SuperAdmin";

    return (
        <Box sx={{maxWidth: 800, mx: "auto"}}>
            <Box sx={{mb: 4, display: "flex", alignItems: "center", gap: 2}}>
                <Avatar
                    sx={{
                        width: 80,
                        height: 80,
                        bgcolor: "primary.main",
                        fontSize: "2rem",
                        boxShadow: "0 4px 12px rgba(0,0,0,0.1)",
                    }}
                >
                    {profileData?.name?.[0] || user?.username[0].toUpperCase()}
                </Avatar>
                <Box>
                    <Typography variant="h4" sx={{fontWeight: 800}}>
                        {profileData?.name || profileData?.username || "My Profile"}
                    </Typography>
                    <Box sx={{display: "flex", alignItems: "center", gap: 1, mt: 0.5}}>
                        <BadgeCheck size={16} color="#6366f1"/>
                        <Typography
                            variant="body2"
                            color="text.secondary"
                            sx={{fontWeight: 600, textTransform: "uppercase"}}
                        >
                            Verified {user?.role} Account
                        </Typography>
                    </Box>
                </Box>
            </Box>

            <Card
                sx={{
                    borderRadius: 4,
                    border: "1px solid rgba(0,0,0,0.05)",
                    boxShadow: "0 8px 32px rgba(0,0,0,0.04)",
                    overflow: "visible",
                }}
            >
                <CardContent sx={{p: 4}}>
                    <Box
                        sx={{
                            display: "flex",
                            justifyContent: "space-between",
                            alignItems: "center",
                            mb: 3,
                        }}
                    >
                        <Typography variant="h6" sx={{fontWeight: 700}}>
                            Personal Information
                        </Typography>
                        {!editing ? (
                            <Button
                                variant="outlined"
                                size="small"
                                onClick={() => setEditing(true)}
                                sx={{borderRadius: "8px", fontWeight: 700}}
                            >
                                Edit Profile
                            </Button>
                        ) : (
                            <Box sx={{display: "flex", gap: 1}}>
                                <Button
                                    variant="text"
                                    size="small"
                                    onClick={() => {
                                        setEditing(false);
                                        setFormData(profileData);
                                    }}
                                    startIcon={<X size={16}/>}
                                    sx={{color: "text.secondary", fontWeight: 700}}
                                >
                                    Cancel
                                </Button>
                                <Button
                                    variant="contained"
                                    size="small"
                                    onClick={handleUpdate}
                                    disabled={saving}
                                    startIcon={
                                        saving ? <CircularProgress size={16}/> : <Save size={16}/>
                                    }
                                    sx={{borderRadius: "8px", fontWeight: 700}}
                                >
                                    Save Changes
                                </Button>
                            </Box>
                        )}
                    </Box>

                    <Divider sx={{mb: 4}}/>

                    <Grid container spacing={3}>
                        {/* Common Fields */}
                        {(isResident || isTechnician) && (
                            <Grid size={{xs: 12, md: 6}}>
                                <TextField
                                    fullWidth
                                    label="Full Name"
                                    name="name"
                                    value={editing ? formData.name : profileData.name}
                                    onChange={handleChange}
                                    variant={editing ? "outlined" : "filled"}
                                    disabled={!editing}
                                    slotProps={{input: {readOnly: !editing}}}
                                    sx={{
                                        "& .MuiFilledInput-root": {
                                            backgroundColor: "transparent",
                                        },
                                    }}
                                />
                            </Grid>
                        )}

                        {isAdmin && (
                            <Grid size={{xs: 12, md: 6}}>
                                <TextField
                                    fullWidth
                                    label="Username"
                                    name="username"
                                    value={editing ? formData.username : profileData.username}
                                    onChange={handleChange}
                                    variant={editing ? "outlined" : "filled"}
                                    disabled={!editing}
                                    slotProps={{input: {readOnly: !editing}}}
                                />
                            </Grid>
                        )}

                        {(isResident || isTechnician) && (
                            <Grid size={{xs: 12, md: 6}}>
                                <TextField
                                    fullWidth
                                    label="Contact Phone"
                                    name="contact"
                                    value={editing ? formData.contact : profileData.contact}
                                    onChange={handleChange}
                                    variant={editing ? "outlined" : "filled"}
                                    disabled={!editing}
                                    slotProps={{input: {readOnly: !editing}}}
                                />
                            </Grid>
                        )}

                        {/* Resident Specific */}
                        {isResident && (
                            <>
                                <Grid size={{xs: 12, md: 4}}>
                                    <TextField
                                        fullWidth
                                        label="House Block"
                                        value={profileData.houseBlock}
                                        variant="filled"
                                        disabled
                                        helperText="Admin only edit"
                                    />
                                </Grid>
                                <Grid size={{xs: 12, md: 4}}>
                                    <TextField
                                        fullWidth
                                        label="House Floor"
                                        value={profileData.houseFloor}
                                        variant="filled"
                                        disabled
                                        helperText="Admin only edit"
                                    />
                                </Grid>
                                <Grid size={{xs: 12, md: 4}}>
                                    <TextField
                                        fullWidth
                                        label="House Unit"
                                        value={profileData.houseUnit}
                                        variant="filled"
                                        disabled
                                        helperText="Admin only edit"
                                    />
                                </Grid>
                                <Grid size={{xs: 12, md: 6}}>
                                    <TextField
                                        fullWidth
                                        label="Ownership Status"
                                        value={profileData.ownershipStatus}
                                        variant="filled"
                                        disabled
                                    />
                                </Grid>
                                <Grid size={{xs: 12, md: 6}}>
                                    <TextField
                                        fullWidth
                                        label="Family Members"
                                        name="noOfMembers"
                                        type="number"
                                        value={
                                            editing ? formData.noOfMembers : profileData.noOfMembers
                                        }
                                        onChange={handleChange}
                                        variant={editing ? "outlined" : "filled"}
                                        disabled={!editing}
                                    />
                                </Grid>
                            </>
                        )}

                        {/* Technician Specific */}
                        {isTechnician && (
                            <>
                                <Grid size={{xs: 12, md: 6}}>
                                    <TextField
                                        fullWidth
                                        label="Specialization / Skill"
                                        name="skill"
                                        value={editing ? formData.skill : profileData.skill}
                                        onChange={handleChange}
                                        variant={editing ? "outlined" : "filled"}
                                        disabled={!editing}
                                    />
                                </Grid>
                                <Grid size={{xs: 12, md: 6}}>
                                    <Box
                                        sx={{
                                            p: 2,
                                            borderRadius: 2,
                                            bgcolor: "rgba(0,0,0,0.02)",
                                            display: "flex",
                                            alignItems: "center",
                                            justifyContent: "space-between",
                                        }}
                                    >
                                        <Typography sx={{fontWeight: 600}}>
                                            Active Availability
                                        </Typography>
                                        <Switch
                                            checked={
                                                editing ? !!formData.available : !!profileData.available
                                            }
                                            onChange={(e) =>
                                                editing &&
                                                setFormData({
                                                    ...formData,
                                                    available: e.target.checked,
                                                })
                                            }
                                            disabled={!editing}
                                        />
                                    </Box>
                                </Grid>
                            </>
                        )}
                    </Grid>
                </CardContent>
            </Card>

            <Snackbar
                open={snackbar.open}
                autoHideDuration={4000}
                onClose={() => setSnackbar({...snackbar, open: false})}
            >
                <Alert
                    onClose={() => setSnackbar({...snackbar, open: false})}
                    severity={snackbar.severity}
                    sx={{width: "100%", borderRadius: "12px", fontWeight: 600}}
                >
                    {snackbar.message}
                </Alert>
            </Snackbar>
        </Box>
    );
};

export default ProfileView;
