import apiService from './api';

export interface Course {
  id: number;
  name: string;
 active: boolean;
}

export interface CourseCreate {
  name: string;
  active: boolean;

}

class CourseService {
  async getAllCourses(): Promise<Course[]> {
    return apiService.get<Course[]>('/api/courses');
  }

  async getCourseById(id: number): Promise<Course> {
    return apiService.get<Course>(`/api/courses/${id}`);
  }

  async createCourse(course: CourseCreate): Promise<Course> {
    return apiService.post<Course>('/api/courses', course);
  }

  async updateCourse(id: number, course: CourseCreate): Promise<Course> {
    return apiService.put<Course>(`/api/courses/${id}`, course);
  }

  async deactivateCourse(id: number): Promise<void> {
    return apiService.delete<void>(`/api/courses/${id}`);
  }
}

export default new CourseService();