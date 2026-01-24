import { useState, useEffect } from "react";
import plService, { PraktikumType } from "../../services/plService";
import type {
  TeacherPlConfigRequest,
  TeacherPlConfigDto,
  TeacherDto,
} from "../../services/plService";
import courseService, { type Course } from "../../services/courseService";
import "../../styles/InternshipsAssignment/InternshipAssignmentStudentForm.css";

interface Props {
  teacher: TeacherDto;
  config?: TeacherPlConfigDto;
  selectedYear?: string;
  onClose: () => void;
  onSave: () => void;
}

export default function TeacherConfigForm({
  teacher,
  config,
  selectedYear,
  onClose,
  onSave,
}: Props) {
  /* ---------------- Courses ---------------- */
  const [courses, setCourses] = useState<Course[]>([]);
  const [loadingCourses, setLoadingCourses] = useState(true);
  
  // Initialize form with course IDs instead of course names
  const [form, setForm] = useState<TeacherPlConfigRequest>({
    schoolYear: config?.schoolYear || selectedYear || "",
    maxPraktikaPerYear: config?.maxPraktikaPerYear || 0,
    totalHoursCredit: config?.totalHoursCredit || 0,
    subjectSpecializations: config?.subjectSpecializations?.map(c => c.id) || [], // Store course IDs
    internshipPreferences: config?.internshipPreferences || [],
  });

  useEffect(() => {
    const loadCourses = async () => {
      try {
        setLoadingCourses(true);
        const allCourses = await courseService.getAllCourses();
        setCourses(allCourses.filter((c) => c.active));
      } catch (err) {
        console.error("Failed to load courses", err);
      } finally {
        setLoadingCourses(false);
      }
    };
    loadCourses();
  }, []);

  /* ---------------- Helpers ---------------- */
  const handleChange = <K extends keyof TeacherPlConfigRequest>(
    key: K,
    value: TeacherPlConfigRequest[K]
  ) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  /* Toggle course checkbox - now using course IDs */
  const handleToggleCourse = (courseId: number) => {
    const current = form.subjectSpecializations;
    handleChange(
      "subjectSpecializations",
      current.includes(courseId)
        ? current.filter((id) => id !== courseId)
        : [...current, courseId]
    );
  };

  /* Toggle internship type */
  const handleToggleInternshipPref = (pref: PraktikumType) => {
    const current = form.internshipPreferences;
    handleChange(
      "internshipPreferences",
      current.includes(pref)
        ? current.filter((p) => p !== pref)
        : [...current, pref]
    );
  };

  const handleSubmit = async () => {
    try {
      if (config) {
        await plService.updateConfig(teacher.teacherId, config.id, form);
      } else {
        await plService.createConfig(teacher.teacherId, form);
      }
      onSave();
    } catch (err) {
      console.error("Teacher config save failed", err);
    }
  };

  // Get the count of selected courses
  const selectedCount = form.subjectSpecializations.length;

  return (
    <div className="student-config-form-container">
      <h3 className="student-config-form-title">
        {config ? "Edit Teacher Configuration" : "Create Teacher Configuration"}
      </h3>

      <p className="teacher-name">
        Teacher: {teacher.firstName} {teacher.lastName}
        {teacher.isPartTime && (
          <span style={{ 
            marginLeft: "12px", 
            padding: "4px 12px", 
            backgroundColor: "#fef3c7",
            color: "#92400e",
            borderRadius: "4px",
            fontSize: "0.85rem",
            fontWeight: "bold"
          }}>
            Part-Time
          </span>
        )}
      </p>
      <p className="teacher-main-subject">
        Main Subject: {teacher.mainSubject?.name || "Not specified"}
      </p>

      <div className="student-config-split-layout">
        {/* LEFT SIDE */}
        <div className="student-config-left-section">
          <div className="student-config-field">
            <label>School Year</label>
            <input
              className="student-config-input"
              type="text"
              placeholder="z.B. 2024"
              value={form.schoolYear}
              onChange={(e) => handleChange("schoolYear", e.target.value)}
            />
          </div>

          <div className="student-config-field">
            <label>Max Praktika Per Year</label>
            <input
              className="student-config-input"
              type="number"
              min={0}
              value={form.maxPraktikaPerYear}
              onChange={(e) =>
                handleChange("maxPraktikaPerYear", Number(e.target.value))
              }
            />
          </div>

          <div className="student-config-field">
            <label>Total Hours Credit</label>
            <input
              className="student-config-input"
              type="number"
              min={0}
              value={form.totalHoursCredit}
              onChange={(e) =>
                handleChange("totalHoursCredit", Number(e.target.value))
              }
            />
          </div>
        </div>

        {/* RIGHT SIDE */}
        <div className="student-config-right-section">
          {/* SUBJECT SPECIALIZATIONS (CHECKBOXES) */}
          <div className="student-config-field">
            <label style={{ marginBottom: "12px", display: "block" }}>
              Subject Specializations
              {selectedCount > 0 && (
                <span style={{ marginLeft: "8px", fontSize: "0.9rem", color: "#64748b" }}>
                  ({selectedCount} selected)
                </span>
              )}
            </label>

            {loadingCourses ? (
              <div style={{ color: "#777" }}>Loading courses…</div>
            ) : courses.length === 0 ? (
              <div style={{ color: "#777", fontStyle: "italic" }}>
                No active courses available
              </div>
            ) : (
              <div className="checkbox-group vertical" style={{ maxHeight: "200px", overflowY: "auto" }}>
                {courses.map((course) => (
                  <label key={course.id} className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={form.subjectSpecializations.includes(course.id)}
                      onChange={() => handleToggleCourse(course.id)}
                    />
                    <span>{course.name}</span>
                    {course.id === teacher.mainSubject?.id && (
                      <span style={{ 
                        marginLeft: "8px", 
                        fontSize: "0.8rem", 
                        color: "#3b82f6",
                        fontWeight: "bold"
                      }}>
                        (Main Subject)
                      </span>
                    )}
                  </label>
                ))}
              </div>
            )}
          </div>

          {/* INTERNSHIP TYPES */}
          <div className="student-config-field" style={{ marginTop: "20px" }}>
            <label style={{ marginBottom: "12px", display: "block" }}>
              Internship Preferences
            </label>
            {teacher.isPartTime && (
              <div style={{
                padding: "8px 12px",
                backgroundColor: "#fef3c7",
                border: "1px solid #fbbf24",
                borderRadius: "4px",
                marginBottom: "12px",
                fontSize: "0.9rem",
                color: "#92400e"
              }}>
                ⚠️ Part-time teachers can only be assigned to ZSP or SFP internships.
              </div>
            )}

            <div className="checkbox-group horizontal">
              {Object.values(PraktikumType).map((pref) => {
                const isPdp = pref === PraktikumType.PDP_I || pref === PraktikumType.PDP_II;
                const isDisabled = teacher.isPartTime && isPdp;
                
                return (
                  <label 
                    key={pref} 
                    className="checkbox-label"
                    style={{
                      opacity: isDisabled ? 0.5 : 1,
                      cursor: isDisabled ? "not-allowed" : "pointer"
                    }}
                  >
                    <input
                      type="checkbox"
                      checked={form.internshipPreferences.includes(pref)}
                      onChange={() => handleToggleInternshipPref(pref)}
                      disabled={isDisabled}
                    />
                    <span>{pref.replace("_", "-")}</span>
                  </label>
                );
              })}
            </div>
          </div>
        </div>
      </div>

      {/* ACTION BUTTONS */}
      <div className="student-config-form-actions">
        <button
          className="btn btn-ghost"
          onClick={onClose}
        >
          Cancel
        </button>
        <button
          className="btn-primary-filled"
          onClick={handleSubmit}
          disabled={loadingCourses}
        >
          {config ? "Update" : "Create"}
        </button>
      </div>
    </div>
  );
}
