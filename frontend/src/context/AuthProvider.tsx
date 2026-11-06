import { jwtDecode } from "jwt-decode";
import React, { useCallback, useEffect, useState } from "react";
import { AuthState, DecodedToken, Role, User } from "../types";
import { AuthContext } from "./AuthContext";

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [state, setState] = useState<AuthState>({
    user: null,
    token: null,
    isLoading: true,
  });

  const logout = useCallback(() => {
    localStorage.removeItem("token");
    setState({ user: null, token: null, isLoading: false });
  }, []);

  const decodeTokenAndSetUser = useCallback(
    (token: string) => {
      try {
        const decoded = jwtDecode<DecodedToken>(token);
        // A stored token may have expired since it was saved; don't show a signed-in UI for it.
        if (decoded.exp !== undefined && decoded.exp * 1000 <= Date.now()) {
          logout();
          return;
        }
        const user: User = {
          id: (decoded.id || decoded.userId || decoded.adminId) as number,
          username: (decoded.username || decoded.sub) as string,
          role: decoded.role as Role,
          residentId: decoded.resident_id,
          techId: decoded.tech_id,
          mustChangePassword: decoded.pwd_change === true,
        };
        setState({ user, token, isLoading: false });
      } catch (error) {
        console.error("Token decoding failed:", error);
        logout();
      }
    },
    [logout],
  );

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (token) {
      decodeTokenAndSetUser(token);
    } else {
      setState({ user: null, token: null, isLoading: false });
    }
  }, [decodeTokenAndSetUser]);

  // Sign out when the token expires, rather than on the next failing request.
  useEffect(() => {
    if (!state.token) return;
    const { exp } = jwtDecode<DecodedToken>(state.token);
    if (exp === undefined) return;
    const timer = setTimeout(logout, Math.max(0, exp * 1000 - Date.now()));
    return () => clearTimeout(timer);
  }, [state.token, logout]);

  const login = useCallback(
    (token: string) => {
      localStorage.setItem("token", token);
      decodeTokenAndSetUser(token);
    },
    [decodeTokenAndSetUser],
  );

  return (
    <AuthContext.Provider value={{ ...state, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
