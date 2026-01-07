import { API_ENDPOINTS } from '../config/api';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  roles: string[];
  permissions: string[];
  profileImage?: string;
}

class AuthService {
  async login(email: string, password: string): Promise<LoginResponse> {
    const response = await fetch(API_ENDPOINTS.LOGIN, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password }),
    });

    if (!response.ok) {
      throw new Error('Invalid credentials');
    }

    const data: LoginResponse = await response.json();
    
    sessionStorage.setItem('token', data.token);
    
    // Decode JWT to get user info
    const userInfo = this.decodeToken(data.token);
    sessionStorage.setItem('user', JSON.stringify(userInfo));    
    return data;
  }

  logout(): void {
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('user');
  }

  getToken(): string | null {
    return sessionStorage.getItem('token');
  }

  getCurrentUser(): User | null {
    const userStr = sessionStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  }

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }

  private decodeToken(token: string): User {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );

      const payload = JSON.parse(jsonPayload);
      
      console.log('JWT Payload:', payload);
      
      let permissions: string[] = [];
      
      if (payload.authorities && Array.isArray(payload.authorities)) {
        permissions = payload.authorities;
      }
      
      console.log('Extracted permissions:', permissions);
      
      return {
        id: payload.sub || 0,
        email: payload.sub || '',
        firstName: payload.firstName || '',
        lastName: payload.lastName || '',
        roles: payload.roles || [],
        permissions: permissions,
      };
    } catch (error) {
      console.error('Error decoding token:', error);
      return {
        id: 0,
        email: '',
        firstName: '',
        lastName: '',
        roles: [],
        permissions: [],
      };
    }
  }
}

export default new AuthService();