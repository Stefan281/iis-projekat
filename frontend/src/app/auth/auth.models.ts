export type UserRole = 'ORGANIZATOR' | 'GENERALNI_DIREKTOR' | 'IGRAC' | 'STATISTICAR' | 'STRUCNI_STAB' | 'ADMIN';

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
