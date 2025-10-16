import axios from 'axios';
import * as SecureStore from 'expo-secure-store';

// -------------------------------------------------------------------------
// ATENÇÃO: PASSO CRÍTICO!
// Substitua 'localhost' pelo endereço IP da sua máquina na rede local.
// O emulador/celular não consegue acessar 'localhost'.
// Para descobrir seu IP:
// - Windows: no terminal, digite `ipconfig` e procure por "Endereço IPv4".
// - Mac/Linux: no terminal, digite `ifconfig` ou `ip a` (ex: 192.168.1.XX).
// -------------------------------------------------------------------------
const BASE_URL = 'http://192.168.136.13:8080'; // <-- MUDE ESTE VALOR

const apiClient = axios.create({
  baseURL: BASE_URL,
});

apiClient.interceptors.request.use(async (config) => {
  const token = await SecureStore.getItemAsync('userToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const isFirstUser = () => apiClient.get('/api/auth/is-first-user');
export const registerFirstUser = (data: any) => apiClient.post('/api/auth/register-first-user', data);
export const login = (data: any) => apiClient.post('/api/auth/login', data);

export const getRecentHemogramas = () => apiClient.get('/api/hemogramas/recentes');
