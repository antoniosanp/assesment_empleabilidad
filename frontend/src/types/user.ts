export type UserRole = 'ADMIN' | 'MEMBER';

export interface User {
  id: string;
  email: string;
  fullName: string;
  jobTitle?: string;
  role: UserRole;
  isActive: boolean;
  createdAt: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: User;
}
