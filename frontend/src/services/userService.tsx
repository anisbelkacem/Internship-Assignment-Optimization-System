import apiService from './api';

export enum UserRole {
  ADMIN = 'ADMIN',
  USER = 'USER'
}

export enum Permission {
  VIEW = 'VIEW',
  EDIT = 'EDIT',
  MANAGE_USERS = 'MANAGE_USERS',
}

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  roles: UserRole[];
  permissions: Permission[];
}

export interface UserCreate {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  roles: UserRole[];
  permissions: Permission[];
}

class UserService {
  async getAllUsers(): Promise<User[]> {
    return apiService.get<User[]>('/api/admin/users');
  }

  async createUser(user: UserCreate): Promise<User> {
    return apiService.post<User>('/api/admin/users', user);
  }

  async updateUser(id: number, user: Partial<UserCreate>): Promise<User> {
    return apiService.put<User>(`/api/admin/users/${id}`, user);
  }

  async deleteUser(id: number): Promise<void> {
    return apiService.delete<void>(`/api/admin/users/${id}`);
  }
}

export default new UserService();