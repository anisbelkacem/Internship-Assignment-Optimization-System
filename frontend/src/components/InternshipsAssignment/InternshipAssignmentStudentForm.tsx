/* eslint-disable @typescript-eslint/no-explicit-any */
import { useState, useEffect } from "react";
import StudentConfigService from "../../services/studentConfigService";
import courseService, { type Course } from "../../services/courseService";
import "../../styles/InternshipsAssignment/InternshipAssignmentStudentForm.css";
import type { StudentConfigDto } from "../../services/studentConfigService";

interface Props {
  config?: StudentConfigDto;
  year: string;
  onClose: () => void;
  onSave: (savedConfig: StudentConfigDto) => void;
}

export default function StudentConfigForm({ config, year, onClose, onSave }: Props) {
  const [form, setForm] = useState<StudentConfigDto>({
    id: config?.id ?? 0,
    studentId: config?.studentId ?? 0,
    schoolType: config?.schoolType ?? "GS",
    pdpI: config?.pdpI ?? false,
    pdpII: config?.pdpII ?? false,
    zsp: config?.zsp ?? false,
    sfp: config?.sfp ?? false,
    mainCourse: config?.mainCourse ?? null,
    prefCourse1: config?.prefCourse1 ?? null,
    prefCourse2: config?.prefCourse2 ?? null,
    prefCourse3: config?.prefCourse3 ?? null,
    year: config?.year ?? year,
  });

  const [courses, setCourses] = useState<Course[]>([]);
  const [loadingCourses, setLoadingCourses] = useState(true);

  useEffect(() => {
    const loadCourses = async () => {
      try {
        setLoadingCourses(true);
        const allCourses = await courseService.getAllCourses();
        const activeCourses = allCourses.filter(c => c.active);
        setCourses(activeCourses);
      } catch (error) {
        console.error('Error loading courses:', error);
      } finally {
        setLoadingCourses(false);
      }
    };
    loadCourses();
  }, []);

  const handleChange = <K extends keyof StudentConfigDto>(key: K, value: StudentConfigDto[K]) => {
    setForm(prev => ({ ...prev, [key]: value }));
  };

  const handleCourseChange = (key: 'mainCourse' | 'prefCourse1' | 'prefCourse2' | 'prefCourse3', courseId: string) => {
    if (courseId === "") {
      handleChange(key, null as any);
    } else {
      const selectedCourse = courses.find(c => c.id === Number(courseId)) ?? null;
      handleChange(key, selectedCourse as any);
    }
  };

  const getCourseValue = (key: 'mainCourse' | 'prefCourse1' | 'prefCourse2' | 'prefCourse3'): string => {
    const course = form[key];
    return course?.id?.toString() ?? "";
  };

const handleSubmit = async () => {
  // Validate year
  if (!form.year || form.year.trim() === "") {
    alert('Please enter a year');
    return;
  }
  
  // Validate all courses are selected
  if (!form.mainCourse || !form.prefCourse1 || !form.prefCourse2 || !form.prefCourse3) {
    alert('Please select all courses');
    return;
  }
  
  // Validate no duplicate courses
  const courseIds = [
    form.mainCourse?.id,
    form.prefCourse1?.id,
    form.prefCourse2?.id,
    form.prefCourse3?.id
  ].filter(Boolean);
  
  const uniqueIds = new Set(courseIds);
  if (uniqueIds.size < courseIds.length) {
    alert('Cannot select the same course multiple times');
    return;
  }

  // Send full course objects matching backend DTO
  const payload = {
    id: form.id || null,
    studentId: form.studentId,
    schoolType: form.schoolType,
    pdpI: form.pdpI,
    pdpII: form.pdpII,
    zsp: form.zsp,
    sfp: form.sfp,
    year: form.year,
    mainCourse: form.mainCourse,
    prefCourse1: form.prefCourse1,
    prefCourse2: form.prefCourse2,
    prefCourse3: form.prefCourse3
  };

  console.log('Sending payload:', JSON.stringify(payload, null, 2));

  try {
    const saved = config?.id
      ? await StudentConfigService.updateConfig(form.id!, payload as any)
      : await StudentConfigService.createConfig(payload as any);
    onSave(saved);
  } catch (err) {
    console.error("Config save failed", err);
    alert('Error saving configuration');
  }
};

  return (
    <div className="student-config-form-container">
      <h3 className="student-config-form-title">
        {config?.id ? "Edit Configuration" : "Create Configuration"}
      </h3>

      <div className="student-config-split-layout">
        {/* Left Side: Student Info & Checkboxes */}
        <div className="student-config-left-section">
          <div className="student-config-field">
            <label>Student ID</label>
            <input
              className="student-config-input"
              type="number"
              value={form.studentId}
              onChange={e => handleChange("studentId", Number(e.target.value))}
            />
          </div>

          <div className="student-config-field">
            <label>Year</label>
            <input
              className="student-config-input"
              type="text"
              value={form.year}
              onChange={e => handleChange("year", e.target.value)}
              placeholder="e.g., 2024/2025"
            />
          </div>

          {config && (
            <div className="student-config-field">
              <label>School Type</label>
              <select
                className="student-config-input"
                value={form.schoolType}
                onChange={e => handleChange("schoolType", e.target.value as typeof form.schoolType)}
              >
                <option value="GS">GS</option>
                <option value="MS">MS</option>
              </select>
            </div>
          )}

          <div className="student-config-field" style={{ marginTop: "20px" }}>
            <label style={{ marginBottom: "12px", display: "block" }}>Internship Types</label>
            <div className="checkbox-group">
              {["pdpI", "pdpII", "zsp", "sfp"].map(field => (
                <div key={field} className="checkbox-label">
                  <input
                    type="checkbox"
                    checked={form[field as keyof StudentConfigDto] as boolean}
                    onChange={e => handleChange(field as keyof StudentConfigDto, e.target.checked as any)}
                  />
                  <span>{field.toUpperCase()}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right Side: Courses */}
        <div className="student-config-right-section">
          {loadingCourses ? (
            <div style={{ padding: '20px', textAlign: 'center', color: '#666' }}>
              Loading courses...
            </div>
          ) : courses.length === 0 ? (
            <div style={{ padding: '20px', textAlign: 'center', color: '#999' }}>
              No active courses available. Please add courses in the course management section.
            </div>
          ) : (
            <>
              {[
                { key: "mainCourse" as const, label: "Main Course" },
                { key: "prefCourse1" as const, label: "Preferred Course 1" },
                { key: "prefCourse2" as const, label: "Preferred Course 2" },
                { key: "prefCourse3" as const, label: "Preferred Course 3" }
              ].map(({ key, label }) => (
                <div key={key} className="student-config-field">
                  <label>{label}</label>
                  <select
                    className="student-config-input"
                    value={getCourseValue(key)}
                    onChange={e => handleCourseChange(key, e.target.value)}
                  >
                    <option value="">Select course</option>
                    {courses.map(c => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                </div>
              ))}
            </>
          )}
        </div>
      </div>

      <div className="student-config-form-actions">
        <button 
          className="student-config-btn" 
          onClick={handleSubmit}
          disabled={loadingCourses || courses.length === 0}
        >
          {config ? "Update" : "Create"}
        </button>
        <button className="student-config-btn student-config-btn-cancel" onClick={onClose}>
          Cancel
        </button>
      </div>
    </div>
  );
}
