import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { authService } from '../services/authService';
import { readSession, writeSession } from '../services/http';
const AuthContext = createContext(null);
export function AuthProvider({children}) {
  const [session,setSession] = useState(readSession);
  const setAuth = (value) => {writeSession(value); setSession(value);};
  useEffect(()=>{
    const onInvalid = () => setAuth(null);
    window.addEventListener('skyport-auth-invalid',onInvalid);
    return () => window.removeEventListener('skyport-auth-invalid',onInvalid);
  },[]);
  const value = useMemo(()=>({
    session, isAuthenticated: !!session?.accessToken, isAdmin: session?.role === 'ADMIN',
    login: async (email,password) => {const data = await authService.login(email,password); setAuth(data); return data;},
    register: async (email,password,phone) => {const data = await authService.register(email,password,phone); setAuth(data); return data;},
    logout: () => setAuth(null)
  }),[session]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
export const useAuth = () => useContext(AuthContext);
