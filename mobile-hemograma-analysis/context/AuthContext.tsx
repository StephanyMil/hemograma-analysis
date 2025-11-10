import React, { createContext, useState, useEffect } from 'react';
import * as SecureStore from 'expo-secure-store';
import { login as apiLogin } from '../api/apiService';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [userToken, setUserToken] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const bootstrapAsync = async () => {
      let token;
      try {
        token = await SecureStore.getItemAsync('userToken');
      } catch (e) {
        console.error('Falha ao carregar o token', e);
      }
      setUserToken(token);
      setIsLoading(false);
    };

    bootstrapAsync();
  }, []);

  const authContext = {
    login: async (credentials) => {
      try {
        const response = await apiLogin(credentials);
        const token = response.data.token;
        setUserToken(token);
        await SecureStore.setItemAsync('userToken', token);
      } catch (error) {
        console.error("Falha no login", error);
        throw error;
      }
    },
    logout: async () => {
      setUserToken(null);
      await SecureStore.deleteItemAsync('userToken');
    },
    userToken,
    isLoading,
  };

  return (
    <AuthContext.Provider value={authContext}>
      {children}
    </AuthContext.Provider>
  );
};
