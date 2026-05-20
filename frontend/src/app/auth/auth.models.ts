export type UserRole = 'ADMIN' | 'STATISTICAR' | 'STRUCNI_STAB';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  role: UserRole;
}
