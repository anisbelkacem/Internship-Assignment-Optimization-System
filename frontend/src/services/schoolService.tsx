import apiService from './api';

export enum SchoolType {
  GS = 'GS',
  MS = 'MS'
}

export interface School {
  id: number;
  name: string;
  address: string;
  zone: string;
  oepnv: boolean;
  type: SchoolType;
  active: boolean;
}

export interface SchoolCreate {
  name: string;
  address: string;
  zone: string;
  oepnv: boolean;
  type: SchoolType;
  active: boolean;
}

export interface SchoolImportResult {
  totalRows: number;
  importedCount: number;
  errorCount: number;
  errors: string[];
}

class SchoolService {
  async getAllSchools(): Promise<School[]> {
    return apiService.get<School[]>('/api/schools');
  }

  async getActiveSchools(): Promise<School[]> {
    return apiService.get<School[]>('/api/schools/active');
  }

  async getSchoolById(id: number): Promise<School> {
    return apiService.get<School>(`/api/schools/${id}`);
  }

  async createSchool(school: SchoolCreate): Promise<School> {
    return apiService.post<School>('/api/schools', school);
  }

  async updateSchool(id: number, school: SchoolCreate): Promise<School> {
    return apiService.put<School>(`/api/schools/${id}`, school);
  }

  async deleteSchool(id: number): Promise<void> {
    return apiService.delete<void>(`/api/schools/${id}`);
  }

  async importSchoolsFromExcel(file: File): Promise<SchoolImportResult> {
    const formData = new FormData();
    formData.append('file', file);

    const token = sessionStorage.getItem('token');
    const headers: HeadersInit = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    console.log('Sending import request with token:', token ? 'present' : 'missing');
    console.log('File:', file.name, file.type, file.size);

    const response = await fetch(`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/schools/import`, {
      method: 'POST',
      headers,
      body: formData,
    });

    console.log('Import response status:', response.status);

    if (!response.ok) {
      let errorMessage = `Import failed: ${response.status}`;
      
      try {
        const contentType = response.headers.get('content-type');
        console.log('Error response content-type:', contentType);
        
        if (contentType && contentType.includes('application/json')) {
          const errorJson = await response.json();
          console.log('Error JSON:', errorJson);
          if (errorJson.message) {
            errorMessage = errorJson.message;
          } else if (errorJson.error) {
            errorMessage = errorJson.error;
          }
        } else {
          const errorText = await response.text();
          console.log('Error text:', errorText);
          if (errorText) {
            errorMessage = errorText;
          }
        }
      } catch (parseError) {
        console.error('Failed to parse error response:', parseError);
      }
      
      if (response.status === 403) {
        errorMessage = 'Access denied. You need EDIT permission to import schools.';
      }
      
      throw new Error(errorMessage);
    }

    return response.json();
  }
}

export default new SchoolService();
