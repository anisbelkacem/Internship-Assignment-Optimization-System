import type { SchoolType, Course } from "./enums";

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
  course: Course;
  startDate: string; // ISO date string
  endDate: string;   // ISO date string
  description?: string;
}
