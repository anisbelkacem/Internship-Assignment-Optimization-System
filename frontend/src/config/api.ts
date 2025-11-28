const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const API_ENDPOINTS = {
  LOGIN: `${API_BASE_URL}/auth/login`,
  USERS: `${API_BASE_URL}/api/admin/users`,
  SCHOOLS: `${API_BASE_URL}/api/schools`,
};

export default API_BASE_URL;