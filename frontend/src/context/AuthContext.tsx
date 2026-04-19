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

import { jwtDecode } from "jwt-decode";
import React, { createContext, useContext, useEffect, useState } from "react";
import { AuthState, DecodedToken, Role, User } from "../types";

interface AuthContextType extends AuthState {
  login: (token: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [state, setState] = useState<AuthState>({
    user: null,
    token: null,
    isLoading: true,
  });

  const decodeTokenAndSetUser = (token: string) => {
    try {
      const decoded = jwtDecode<DecodedToken>(token);
      const user: User = {
        id: (decoded.id || decoded.userId || decoded.adminId) as number,
        username: (decoded.sub || decoded.username) as string,
        role: decoded.role as Role,
        residentId: decoded.residentId,
        techId: decoded.techId,
      };
      setState({ user, token, isLoading: false });
    } catch (error) {
      console.error("Token decoding failed:", error);
      logout();
    }
  };

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      decodeTokenAndSetUser(token);
    } else {
      setState({ user: null, token: null, isLoading: false });
    }
  }, []);

  const login = (token: string) => {
    localStorage.setItem("token", token);
    decodeTokenAndSetUser(token);
  };

  const logout = () => {
    localStorage.removeItem("token");
    setState({ user: null, token: null, isLoading: false });
  };

  return (
    <AuthContext.Provider value={{ ...state, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
