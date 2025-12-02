import apiService from './api';
import type { SchoolType, Course } from './studentService';

export interface StudentConfigDto {
    id: number;
    studentId: number;
    year: string;
    schoolType: SchoolType;
    mainCourse: Course;
    prefCourse1: Course;
    prefCourse2: Course;
    prefCourse3: Course;
    pdpI: boolean;
    pdpII: boolean;
    zsp: boolean;
    sfp: boolean;
}

class StudentConfigService {

    // Get all configs
    async getAllConfigs(): Promise<StudentConfigDto[]> {
        return apiService.get<StudentConfigDto[]>('/api/student-configs');
    }

    // Get configs for a specific student
    async getConfigsByStudent(studentId: number): Promise<StudentConfigDto[]> {
        return apiService.get<StudentConfigDto[]>(`/api/student-configs/student/${studentId}`);
    }

    // Get a specific config by student and year
    async getConfig(studentId: number, year: string): Promise<StudentConfigDto> {
        return apiService.get<StudentConfigDto>(`/api/student-configs/student/${studentId}/year/${year}`);
    }
    
    // Get all configs for a specific year
    async getConfigsByYear(year: string): Promise<StudentConfigDto[]> {
        return apiService.get<StudentConfigDto[]>(`/api/student-configs/year/${year}`);
    }
    // Get all unique years
    async getAllYears(): Promise<string[]> {
        return apiService.get<string[]>('/api/student-configs/years');
    }

    // Create new student config
    async createConfig(dto: StudentConfigDto): Promise<StudentConfigDto> {
        return apiService.post<StudentConfigDto>('/api/student-configs', dto);
    }

    // Update existing config
    async updateConfig(id: number, dto: Partial<StudentConfigDto>): Promise<StudentConfigDto> {
        return apiService.put<StudentConfigDto>(`/api/student-configs/${id}`, dto);
    }

    // Delete a config
    async deleteConfig(id: number): Promise<void> {
        return apiService.delete<void>(`/api/student-configs/${id}`);
    }
}

export default new StudentConfigService();
