import apiService from './api';
import type { CompletedInternship } from './studentService';

class CompletedInternshipsService {
    // Fetch all internships
    async getAll(): Promise<CompletedInternship[]> {
        return apiService.get<CompletedInternship[]>('/api/completed-internships');
    }

    // Fetch internships for a specific student
    async getByStudentId(studentId: number): Promise<CompletedInternship[]> {
        return apiService.get<CompletedInternship[]>(`/api/completed-internships/student/${studentId}`);
    }

    // Fetch internships for a specific teacher
    async getByTeacherId(teacherId: number): Promise<CompletedInternship[]> {
        return apiService.get<CompletedInternship[]>(`/api/completed-internships/teacher/${teacherId}`);
    }

    // Fetch internships for a specific school
    async getBySchoolId(schoolId: number): Promise<CompletedInternship[]> {
        return apiService.get<CompletedInternship[]>(`/api/completed-internships/school/${schoolId}`);
    }

    // Fetch single internship by ID
    async getById(id: number): Promise<CompletedInternship> {
        return apiService.get<CompletedInternship>(`/api/completed-internships/${id}`);
    }

    // Create a new internship
    async create(dto: CompletedInternship): Promise<CompletedInternship> {
        return apiService.post<CompletedInternship>('/api/completed-internships', dto);
    }

    // Update an existing internship
    async update(id: number, dto: Partial<CompletedInternship>): Promise<CompletedInternship> {
        return apiService.put<CompletedInternship>(`/api/completed-internships/${id}`, dto);
    }

    // Delete an internship
    async delete(id: number): Promise<void> {
        return apiService.delete<void>(`/api/completed-internships/${id}`);
    }
}

export default new CompletedInternshipsService();
