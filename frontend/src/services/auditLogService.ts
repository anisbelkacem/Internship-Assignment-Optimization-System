import apiService from './api';

export interface AuditLogEntry {
  id: number;
  entityType: string;
  entityId: number;
  action: string;
  createdBy: string;
  timestamp: string;
  description: string;
  previousValues: string;  // JSON string
  newValues: string;       // JSON string
}

export interface AuditLogPage {
  content: AuditLogEntry[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

const auditLogService = {
  async getAll(page: number = 0, size: number = 20): Promise<AuditLogPage> {
    return apiService.get(`/api/audit-logs?page=${page}&size=${size}`);
  },

  async getEntityHistory(entityType: string, entityId: number, page: number = 0, size: number = 20): Promise<AuditLogPage> {
    return apiService.get(`/api/audit-logs/${entityType}/${entityId}?page=${page}&size=${size}`);
  }
};

export default auditLogService;
