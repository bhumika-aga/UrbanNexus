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

import {createTheme} from '@mui/material/styles';

const vercelTheme = createTheme({
    palette: {
        mode: 'light',
        primary: {
            main: '#6366f1', // Royal Indigo
            dark: '#4f46e5',
            light: '#818cf8',
        },
        secondary: {
            main: '#1e293b', // Slate
        },
        success: {
            main: '#10b981', // Emerald
        },
        background: {
            default: '#f8fafc',
            paper: '#ffffff',
        },
        text: {
            primary: '#0f172a',
            secondary: '#64748b',
        },
        divider: 'rgba(0, 0, 0, 0.05)',
    },
    typography: {
        fontFamily: [
            'Inter',
            '-apple-system',
            'BlinkMacSystemFont',
            '"Segoe UI"',
            'Roboto',
            '"Helvetica Neue"',
            'Arial',
            'sans-serif',
        ].join(','),
        h1: {fontWeight: 800, letterSpacing: '-0.04em', color: '#0f172a'},
        h2: {fontWeight: 800, letterSpacing: '-0.03em', color: '#0f172a'},
        h3: {fontWeight: 800, letterSpacing: '-0.02em', color: '#0f172a'},
        h4: {fontWeight: 700, letterSpacing: '-0.02em'},
        h5: {fontWeight: 700, letterSpacing: '-0.01em'},
        h6: {fontWeight: 700},
        button: {textTransform: 'none', fontWeight: 600, letterSpacing: '0.01em'},
        caption: {letterSpacing: '0.05em'},
    },
    shape: {
        borderRadius: 12,
    },
    components: {
        MuiButton: {
            styleOverrides: {
                root: {
                    borderRadius: 10,
                    padding: '10px 20px',
                    boxShadow: 'none',
                    transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
                    '&:hover': {
                        boxShadow: '0 4px 12px rgba(99, 102, 241, 0.2)',
                        transform: 'translateY(-1px)',
                    },
                    '&.Mui-disabled': {
                        backgroundColor: 'rgba(0, 0, 0, 0.04)',
                        color: 'rgba(0, 0, 0, 0.25)',
                        borderColor: 'transparent',
                    },
                },
            },
            variants: [
                {
                    props: {variant: 'contained', color: 'primary'},
                    style: {
                        background: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)',
                        color: '#ffffff',
                        border: '1px solid rgba(255, 255, 255, 0.1)',
                    },
                },
                {
                    props: {variant: 'outlined', color: 'primary'},
                    style: {
                        borderColor: 'rgba(99, 102, 241, 0.2)',
                        backgroundColor: 'rgba(99, 102, 241, 0.02)',
                        '&:hover': {
                            backgroundColor: 'rgba(99, 102, 241, 0.06)',
                            borderColor: '#6366f1',
                        },
                    },
                },
            ],
        },
        MuiPaper: {
            styleOverrides: {
                root: {
                    border: '1px solid rgba(0, 0, 0, 0.05)',
                    boxShadow: '0 1px 3px rgba(0,0,0,0.02), 0 4px 12px rgba(0,0,0,0.03)',
                    borderRadius: 16,
                    backgroundColor: 'rgba(255, 255, 255, 0.9)',
                    backdropFilter: 'blur(8px)',
                },
            },
        },
        MuiCard: {
            styleOverrides: {
                root: {
                    border: '1px solid rgba(0, 0, 0, 0.05)',
                    boxShadow: '0 1px 3px rgba(0,0,0,0.02), 0 4px 12px rgba(0,0,0,0.03)',
                    borderRadius: 16,
                    transition: 'all 0.3s ease',
                    '&:hover': {
                        borderColor: 'rgba(99, 102, 241, 0.3)',
                        boxShadow: '0 12px 24px rgba(99, 102, 241, 0.08)',
                        transform: 'translateY(-2px)',
                    },
                },
            },
        },
        MuiAppBar: {
            styleOverrides: {
                root: {
                    backgroundColor: 'rgba(255, 255, 255, 0.7)',
                    backdropFilter: 'blur(16px)',
                    borderBottom: '1px solid rgba(0, 0, 0, 0.05)',
                    color: '#0f172a',
                    boxShadow: 'none',
                },
            },
        },
        MuiTab: {
            styleOverrides: {
                root: {
                    fontWeight: 700,
                    fontSize: '0.875rem',
                    textTransform: 'none',
                    minHeight: 48,
                    borderRadius: 8,
                    margin: '0 4px',
                    transition: 'all 0.2s ease',
                    '&.Mui-selected': {
                        color: '#6366f1',
                        backgroundColor: 'rgba(99, 102, 241, 0.05)',
                    },
                },
            },
        },
        MuiTextField: {
            styleOverrides: {
                root: {
                    '& .MuiOutlinedInput-root': {
                        borderRadius: 10,
                        transition: 'all 0.2s ease',
                        backgroundColor: '#ffffff',
                        '& fieldset': {
                            borderColor: 'rgba(0, 0, 0, 0.08)',
                        },
                        '&:hover fieldset': {
                            borderColor: 'rgba(99, 102, 241, 0.4)',
                        },
                        '&.Mui-focused fieldset': {
                            borderColor: '#6366f1',
                            borderWidth: '2px',
                        },
                    },
                },
            },
        },
    },
});

export default vercelTheme;
