export interface AuditLogEntry {
  id: number;
  entityType: string;
  entityId: number;
  createdBy: {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
  };
  timestamp: string;
  action: 'CREATE' | 'UPDATE' | 'DELETE';
  previousValues?: Record<string, any>;
  newValues?: Record<string, any>;
  description: string;
  relatedStudentId?: number;
  relatedSchoolId?: number;
  schoolYear?: string;
  isReadOnly: boolean;
}

export interface AuditLogPage {
  content: AuditLogEntry[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

export interface AuditLogFilter {
  entityType?: string;
  studentId?: number;
  schoolId?: number;
  schoolYear?: string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
  sort?: string;
}

const auditLogService = {
  async getEntityHistory(
    entityType: string,
    entityId: number,
    page: number = 0,
    size: number = 20
  ): Promise<AuditLogPage> {
    const { default: apiService } = await import('./api');
    const url = `/api/audit-logs/${entityType}/${entityId}?page=${page}&size=${size}`;
    return apiService.get(url);
  },

  async getStudentHistory(
    studentId: number,
    page: number = 0,
    size: number = 20
  ): Promise<AuditLogPage> {
    const { default: apiService } = await import('./api');
    const url = `/api/audit-logs/student/${studentId}?page=${page}&size=${size}`;
    return apiService.get(url);
  },

  async getSchoolHistory(
    schoolId: number,
    page: number = 0,
    size: number = 20
  ): Promise<AuditLogPage> {
    const { default: apiService } = await import('./api');
    const url = `/api/audit-logs/school/${schoolId}?page=${page}&size=${size}`;
    return apiService.get(url);
  },

  async getSchoolYearHistory(
    schoolYear: string,
    page: number = 0,
    size: number = 20
  ): Promise<AuditLogPage> {
    const { default: apiService } = await import('./api');
    const url = `/api/audit-logs/school-year/${schoolYear}?page=${page}&size=${size}`;
    return apiService.get(url);
  },

  async searchAuditLogs(filters: AuditLogFilter): Promise<AuditLogPage> {
    const { default: apiService } = await import('./api');
    const params = new URLSearchParams();
    if (filters.entityType) params.append('entityType', filters.entityType);
    if (filters.studentId) params.append('studentId', filters.studentId.toString());
    if (filters.schoolId) params.append('schoolId', filters.schoolId.toString());
    if (filters.schoolYear) params.append('schoolYear', filters.schoolYear);
    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);
    params.append('page', (filters.page || 0).toString());
    params.append('size', (filters.size || 20).toString());
    if (filters.sort) params.append('sort', filters.sort);
    const url = `/api/audit-logs/search?${params.toString()}`;
    return apiService.get(url);
  }
};

export default auditLogService;
