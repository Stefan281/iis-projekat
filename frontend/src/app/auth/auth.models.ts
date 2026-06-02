export type UserRole = 'CUSTOMER' | 'MANAGER' | 'ADMIN' | 'STATISTICAR' | 'STRUCNI_STAB';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  country: string;
  city: string;
  street: string;
  streetNumber: string;
  postalCode: string;
  phoneNumber: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export interface LoginResponse {
  token: string;
  id: number;
  username: string;
  email: string | null;
  firstName: string;
  lastName: string;
  role: UserRole;
}
