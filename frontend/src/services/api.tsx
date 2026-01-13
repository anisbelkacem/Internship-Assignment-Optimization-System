/* eslint-disable @typescript-eslint/no-explicit-any */
import API_BASE_URL from '../config/api';

interface ApiError {
  message: string;
  status: number;
}

class ApiService {
  private getToken(): string | null {
    const token = sessionStorage.getItem('token');
    return token;
  }

  private getHeaders(includeAuth = true): HeadersInit {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    };

    if (includeAuth) {
      const token = this.getToken();
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      } else {
        console.warn('No token found!'); 
      }
    }
    return headers;
  }

  async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${API_BASE_URL}${endpoint}`;
    const headers = this.getHeaders(true);
    const response = await fetch(url, {
      ...options,
      headers: {
        ...headers,
        ...options.headers,
      },
    });

    if (!response.ok) {
      if (!response.ok) {
  if (response.status === 401 || response.status === 403) {
    console.error('Unauthorized/Forbidden - Token might be invalid or missing');
  }

  // try to parse backend error response (ValidationResult JSON)
  const contentType = response.headers.get('content-type') || '';
  let errorBody: any = null;

  try {
    if (contentType.includes('application/json')) {
      errorBody = await response.json();
    } else {
      const text = await response.text();
      errorBody = text ? { message: text } : null;
    }
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  } catch (e) {
    // ignore parse errors
  }

  //  if backend returned structured validation (your ValidationResult)
  if (errorBody && typeof errorBody === 'object') {
    throw {
      ...errorBody,
      status: response.status, // keep status for UI
    };
  }

  // fallback
  const error: ApiError = {
    message: `HTTP error! status: ${response.status}`,
    status: response.status,
  };
  throw error;
}

    }

    if (response.status === 204 || response.headers.get('content-length') === '0') {
      return {} as T;
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return response.json();
    }

    return {} as T;
  }

  async get<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'GET' });
  }

  async post<T>(endpoint: string, data: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async put<T>(endpoint: string, data: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data),
    });
  }

  async patch<T>(endpoint: string, data: unknown): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PATCH',
      body: JSON.stringify(data),
    });
  }

  async delete<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, { method: 'DELETE' });
  }
}

export default new ApiService();