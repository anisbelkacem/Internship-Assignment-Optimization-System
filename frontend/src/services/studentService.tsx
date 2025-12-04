import apiService from './api';

export type SchoolType = "GS" | "MS";


export type Course =
    "COMPUTER_SCIENCE" | "ENGINEERING" | "BUSINESS" |
    "MEDICINE" | "LAW" | "ARTS" | "SCIENCES" | "OTHER";

export type PraktikumType = "PDP_I" | "PDP_II" | "ZSP" | "SFP";


export interface Address {
  street?: string;
  city?: string;
  houseNbr?: string;
  postalCode?: string;
  country?: string;
}

export interface Student {
  matriculationNbr: number;
  firstName: string;
  lastName: string;
  email: string;
  schoolType: SchoolType;
  mainCourse: Course;
  prefCourse1: Course;
  prefCourse2: Course;
  prefCourse3: Course;
  registred: boolean;
  oriented: boolean;
  address?: Address;
  addressSemester?: Address;
  phone?: string;
  birthDate?: string; // use string for ISO date
  description?: string;
}

export interface CompletedInternship {
  id: number;
  studentId: number;
  teacherId?: number;
  schoolId?: number;
  type: PraktikumType;
  course: Course;
  startDate: string; // ISO date string
  endDate: string;   // ISO date string
  description?: string;
}


class StudentService {
    async getAllStudent(): Promise<Student[]> {
    return apiService.get<Student[]>('/api/students');
  }

    async createStudent(Student: Student): Promise<Student> {
    return apiService.post<Student>('/api/students', Student);
  }

    async updateStudent(id: number, Student: Partial<Student>): Promise<Student> {
    return apiService.put<Student>(`/api/students/${id}`, Student);
  }

  async deleteStudent(id: number): Promise<void> {
    return apiService.delete<void>(`/api/students/${id}`);
  }
}

export default new StudentService();