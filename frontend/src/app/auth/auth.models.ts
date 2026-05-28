export type UserRole = 'ADMIN' | 'SKAUT' | 'STATISTICAR' | 'STRUCNI_STAB' | 'SPORTSKI_DIREKTOR';

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
