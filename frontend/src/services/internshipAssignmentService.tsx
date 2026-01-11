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

class InternshipAssignmentService {
  /**
   * Trigger Phase 1 optimization: assigns teachers and schools to planned internships
   * @param schoolYear - Academic year in format "YYYY/YYYY" (e.g., "2024/2025")
   * @returns TeacherAssignmentResult with optimization score and assignments
   */
  async optimizePhase1(schoolYear: string): Promise<TeacherAssignmentResult> {
    return apiService.post<TeacherAssignmentResult>(
      `/api/internships/phase1/optimize?schoolYear=${encodeURIComponent(schoolYear)}`,
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
    schoolId: number | null
  ): Promise<PlannedInternshipDto> {
    return apiService.put<PlannedInternshipDto>(
      `/api/planned-internships/${id}`,
      { teacherId, schoolId }
    );
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
}

export default new InternshipAssignmentService();
