import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import {
  TOKEN_STORAGE_KEY,
  UNAUTHORIZED_EVENT,
  USER_STORAGE_KEY,
} from '../api/axiosClient';

const AuthContext = createContext(null);

function decodeJwtPayload(token) {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
    const json = atob(padded);
    return JSON.parse(json);
  } catch {
    return null;
  }
}

function getInitialAuthState() {
  const savedToken = localStorage.getItem(TOKEN_STORAGE_KEY);
  if (!savedToken) {
    return { token: null, user: null };
  }

  const payload = decodeJwtPayload(savedToken);
  const isExpired = !payload?.exp || payload.exp * 1000 <= Date.now();
  if (!payload || isExpired) {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    localStorage.removeItem(USER_STORAGE_KEY);
    return { token: null, user: null };
  }

  let savedUser = null;
  try {
    const rawUser = localStorage.getItem(USER_STORAGE_KEY);
    savedUser = rawUser ? JSON.parse(rawUser) : null;
  } catch {
    savedUser = null;
  }

  return {
    token: savedToken,
    user: {
      userId: payload.userId ?? savedUser?.userId ?? null,
      email: payload.sub ?? savedUser?.email ?? '',
      rol: payload.rol ?? savedUser?.rol ?? null,
      nombre: savedUser?.nombre ?? payload.sub ?? '',
    },
  };
}

export function AuthProvider({ children }) {
  const [authState, setAuthState] = useState(getInitialAuthState);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    localStorage.removeItem(USER_STORAGE_KEY);
    setAuthState({ token: null, user: null });
  }, []);

  const login = useCallback((authResponse) => {
    const { token, userId, nombre, email, rol } = authResponse;
    const payload = decodeJwtPayload(token);

    const user = {
      userId: payload?.userId ?? userId,
      email: payload?.sub ?? email,
      rol: payload?.rol ?? rol,
      nombre,
    };

    localStorage.setItem(TOKEN_STORAGE_KEY, token);
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
    setAuthState({ token, user });
  }, []);

  useEffect(() => {
    const handleUnauthorized = () => {
      setAuthState({ token: null, user: null });
    };
    window.addEventListener(UNAUTHORIZED_EVENT, handleUnauthorized);
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, handleUnauthorized);
  }, []);

  const value = useMemo(
    () => ({
      token: authState.token,
      user: authState.user,
      rol: authState.user?.rol ?? null,
      isAuthenticated: Boolean(authState.token && authState.user),
      login,
      logout,
    }),
    [authState, login, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe utilizarse dentro de un AuthProvider');
  }
  return context;
}
