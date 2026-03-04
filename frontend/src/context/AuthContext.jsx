import { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null);
  const [loading, setLoading] = useState(true);

  // On mount, try to restore session from stored token
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) { setLoading(false); return; }

    authApi.me()
      .then(setUser)
      .catch(() => localStorage.removeItem('token'))
      .finally(() => setLoading(false));
  }, []);

  async function login(username, password) {
    const data = await authApi.login({ username, password });
    localStorage.setItem('token', data.token);
    // Fetch full user profile (includes homeId)
    const me = await authApi.me();
    setUser(me);
    return me;
  }

  async function register(payload) {
    return authApi.register(payload);
  }

  function logout() {
    localStorage.removeItem('token');
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, register }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
