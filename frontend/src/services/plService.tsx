import apiService from "./api";
import API_BASE_URL from "../config/api";

// export enum Course {
//   COMPUTER_SCIENCE = "COMPUTER_SCIENCE",
//   ENGINEERING = "ENGINEERING",
//   BUSINESS = "BUSINESS",
//   MEDICINE = "MEDICINE",
//   LAW = "LAW",
//   ARTS = "ARTS",
//   SCIENCES = "SCIENCES",
//   OTHER = "OTHER",
// }
export interface Course {
  id: number;
  name: string;
  active?: boolean;
}

export enum PraktikumType {
  PDP_I = "PDP_I",
  PDP_II = "PDP_II",
  ZSP = "ZSP",
  SFP = "SFP",
}


export interface SchoolRef {
  id: number;
  name?: string;
  address?: string;
  zone?: string;
  oepnv?: boolean;
  type?: string;
}

export interface TeacherPlConfigDto {
  id: number;
  schoolYear: string;
  maxPraktikaPerYear: number;
  totalHoursCredit: number;
  subjectSpecializations: Course[];
  internshipPreferences: PraktikumType[];
}

export type TeacherDto = {
  teacherId: number;
  firstName: string;
  lastName: string;
  mainSubject: Course;
  email: string;
  schoolId: number | null;
  schoolName: string | null;
  schoolZone: string | null;
  plConfigs: TeacherPlConfigDto[];
};


export interface TeacherRequest {
  firstName: string;
  lastName: string;
  mainSubject: Course;
  schoolId: { id: number } | null;
  email: string;
}

export interface TeacherPlConfigRequest {
  schoolYear: string;
  maxPraktikaPerYear: number;
  totalHoursCredit: number;
  subjectSpecializations: number[];
  internshipPreferences: PraktikumType[];
}

// Excel import result (TeacherImportResult.java)
export interface TeacherImportResult {
  totalRows: number;
  importedCount: number;
  errorCount: number;
  errors: string[];
}

class PlService {
  private base = "/api/pls";


  async getAllPls(): Promise<TeacherDto[]> {
    return apiService.get<TeacherDto[]>(this.base);
  }

  async getPl(id: number): Promise<TeacherDto> {
    return apiService.get<TeacherDto>(`${this.base}/${id}`);
  }

  async createPl(payload: TeacherRequest): Promise<TeacherDto> {
    return apiService.post<TeacherDto>(this.base, payload);
  }

  async updatePl(id: number, payload: TeacherRequest): Promise<TeacherDto> {
    return apiService.put<TeacherDto>(`${this.base}/${id}`, payload);
  }

  async deletePl(id: number): Promise<void> {
    return apiService.delete<void>(`${this.base}/${id}`);
  }


  async getConfigsForTeacher(teacherId: number): Promise<TeacherPlConfigDto[]> {
    return apiService.get<TeacherPlConfigDto[]>(
      `${this.base}/${teacherId}/pl-configs`
    );
  }

  async createConfig(
    teacherId: number,
    payload: TeacherPlConfigRequest
  ): Promise<TeacherPlConfigDto> {
    return apiService.post<TeacherPlConfigDto>(
      `${this.base}/${teacherId}/pl-configs`,
      payload
    );
  }

  async updateConfig(
    teacherId: number,
    configId: number,
    payload: TeacherPlConfigRequest
  ): Promise<TeacherPlConfigDto> {
    return apiService.put<TeacherPlConfigDto>(
      `${this.base}/${teacherId}/pl-configs/${configId}`,
      payload
    );
  }

  async deleteConfig(teacherId: number, configId: number): Promise<void> {
    return apiService.delete<void>(
      `${this.base}/${teacherId}/pl-configs/${configId}`
    );
  }

  async importFromExcel(file: File): Promise<TeacherImportResult> {
    const formData = new FormData();
    formData.append("file", file);

    const token = sessionStorage.getItem("token");
    const headers: HeadersInit = {};
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}${this.base}/import`, {
      method: "POST",
      headers,
      body: formData,
    });

    if (!response.ok) {
      const message = `HTTP error during PL import: ${response.status}`;
      throw new Error(message);
    }

    return response.json();
  }
}

export default new PlService();
