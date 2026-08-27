import { apiClient } from './apiClient';
import { LoginResponse, User } from '../types/user';

export const authService = {
  async login(email: string, password: string): Promise<LoginResponse> {
    const data = await apiClient<LoginResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });

    localStorage.setItem('riwi_access_token', data.accessToken);
    localStorage.setItem('riwi_refresh_token', data.refreshToken);
    localStorage.setItem('riwi_user', JSON.stringify(data.user));

    return data;
  },

  async register(email: string, password: string, fullName: string, jobTitle: string): Promise<LoginResponse> {
    const data = await apiClient<LoginResponse>('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ email, password, fullName, jobTitle }),
    });

    localStorage.setItem('riwi_access_token', data.accessToken);
    localStorage.setItem('riwi_refresh_token', data.refreshToken);
    localStorage.setItem('riwi_user', JSON.stringify(data.user));

    return data;
  },

  logout() {
    localStorage.removeItem('riwi_access_token');
    localStorage.removeItem('riwi_refresh_token');
    localStorage.removeItem('riwi_user');
    window.location.reload();
  },

  getStoredUser(): User | null {
    const userStr = localStorage.getItem('riwi_user');
    if (!userStr) return null;
    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  },

  getAccessToken(): string | null {
    return localStorage.getItem('riwi_access_token');
  },
};
