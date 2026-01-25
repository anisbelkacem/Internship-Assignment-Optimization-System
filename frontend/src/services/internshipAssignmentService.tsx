import apiService from "./api";

export interface PlannedInternshipDto {
  id: number | null;
  praktikumType: string;
  schoolType: string;
  course: string | null;
  maxCapacity: number;
  teacherId: string | null;
  teacherName: string | null;
  schoolId: number | null;
  schoolName: string | null;
  schoolZone: string | null;
}

export interface TeacherAssignmentResult {
  schoolYear: string;
  totalPlannedInternships: number;
  assignedCount: number;
  unassignedCount: number;
  score: string;
  plannedInternships: PlannedInternshipDto[];
}

export interface AssignmentDto {
  id: number;
  studentName: string;
  praktikumType: string;
  course: string | null;
  teacherId?: number | null;
  teacherName: string | null;
  schoolId?: number | null;
  schoolName: string | null;
  status: string;
}

export interface StudentAssignmentResult {
  schoolYear: string;
  totalStudents: number;
  assignedStudents: number;
  unassignedStudents: number;
  score: string;
  assignments: AssignmentDto[];
}

class InternshipAssignmentService {
  /**
   * Trigger Phase 1 optimization: assigns teachers and schools to planned internships
   * @param schoolYear - Academic year in format "YYYY/YYYY" (e.g., "2024/2025")
   * @returns TeacherAssignmentResult with optimization score and assignments
   */
  async optimizePhase1(schoolYear: string, budget: number): Promise<TeacherAssignmentResult> {
    return apiService.post<TeacherAssignmentResult>(
      `/api/internships/phase1/optimize?schoolYear=${encodeURIComponent(schoolYear)}&budget=${budget}`,
      {}
    );
  }

  /**
   * Get saved planned internships from database for a specific school year
   * @param schoolYear - Academic year (e.g., "2024/2025")
   * @returns List of saved planned internships
   */
  async getPlannedInternships(schoolYear: string): Promise<PlannedInternshipDto[]> {
    return apiService.get<PlannedInternshipDto[]>(
      `/api/planned-internships?schoolYear=${encodeURIComponent(schoolYear)}`
    );
  }

  /**
   * Update a planned internship (change teacher or school assignment)
   * @param id - Planned internship ID
   * @param teacherId - New teacher ID (or null to unassign)
   * @param schoolId - New school ID (or null to unassign)
   */
  async updatePlannedInternship(
  id: number,
  teacherId: number | null,
  schoolId: number | null,
  options?: { force?: boolean }
) {
  const force = options?.force === true;
  const qs = force ? `?force=true` : "";
  return apiService.put(`/api/planned-internships/${id}${qs}`, { teacherId, schoolId });
}


  /**
   * Delete a single planned internship
   */
  async deletePlannedInternship(id: number): Promise<void> {
    return apiService.delete<void>(`/api/planned-internships/${id}`);
  }

  /**
   * Delete all planned internships for a school year
   */
  async deleteAllPlannedInternships(schoolYear: string): Promise<void> {
    return apiService.delete<void>(
      `/api/planned-internships?schoolYear=${encodeURIComponent(schoolYear)}`
    );
  }

  /**
   * Trigger Phase 2 optimization: assigns students to planned internships
   * @param schoolYear - Academic year in format "YYYY/YYYY" (e.g., "2024/2025")
   * @returns StudentAssignmentResult with optimization score and assignments
   */
  async optimizePhase2(schoolYear: string): Promise<StudentAssignmentResult> {
    console.log('Calling Phase 2 optimization for year:', schoolYear);
    console.log('Token present:', !!sessionStorage.getItem('token'));
    return apiService.post<StudentAssignmentResult>(
      `/api/internships/phase2/optimize?schoolYear=${encodeURIComponent(schoolYear)}`,
      {}
    );
  }

  /**
   * Get student assignments from database for a specific school year
   * @param schoolYear - Academic year (e.g., "2024/2025")
   * @returns List of student assignments
   */
  async getStudentAssignments(schoolYear: string): Promise<AssignmentDto[]> {
    return apiService.get<AssignmentDto[]>(
      `/api/internship-assignments?schoolYear=${encodeURIComponent(schoolYear)}`
    );
  }

  /**
   * Delete all student assignments for a school year
   */
  async deleteAllStudentAssignments(schoolYear: string): Promise<void> {
    return apiService.delete<void>(
      `/api/internship-assignments?schoolYear=${encodeURIComponent(schoolYear)}`
    );
  }

  /**
   * Update student assignment status only
   */
  async updateAssignmentStatus(id: number, status: string): Promise<AssignmentDto> {
    return apiService.patch<AssignmentDto>(
      `/api/internship-assignments/${id}/status?status=${status}`,
      {}
    );
  }

  /**
   * Update full student assignment (teacher, school, status, etc.)
   */
  async updateStudentAssignment(
  id: number,
  data: { teacherId?: number | null; schoolId?: number | null; status?: string },
  options?: { force?: boolean }
) {
  const force = options?.force === true;
  const qs = force ? `?force=true` : "";
  return apiService.put(`/api/internship-assignments/${id}${qs}`, data);
}
}

export default new InternshipAssignmentService();
