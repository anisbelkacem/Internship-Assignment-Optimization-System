import apiService from './api';

export interface DashboardStats {
  totalStudents: number;
  totalTeachers: number;
  totalSchools: number;
  totalPls: number;
}

const dashboardService = {
  async getStats(schoolYear?: string): Promise<DashboardStats> {
    const params = schoolYear ? `?schoolYear=${schoolYear}` : '';
    return apiService.get<DashboardStats>(`/api/dashboard/stats${params}`);
  }
};

export default dashboardService;
