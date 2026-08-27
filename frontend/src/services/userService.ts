import { apiClient } from './apiClient';
import { User } from '../types/user';

export const userService = {
  async getAllUsers(): Promise<User[]> {
    return apiClient<User[]>('/users');
  },
};
