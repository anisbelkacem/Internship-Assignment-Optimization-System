import { useState } from "react";
import plService from "../../services/plService";
import { PraktikumType } from "../../services/plService";
import type { TeacherPlConfigRequest, TeacherPlConfigDto, TeacherDto } from "../../services/plService";
import "../../styles/InternshipsAssignment/InternshipAssignmentStudentForm.css";

interface Props {
  teacher: TeacherDto;
  config?: TeacherPlConfigDto;
  selectedYear?: string;
  onClose: () => void;
  onSave: () => void;
}

export default function TeacherConfigForm({ teacher, config, selectedYear, onClose, onSave }: Props) {
  const [form, setForm] = useState<TeacherPlConfigRequest>({
    schoolYear: config?.schoolYear || selectedYear || "",
    maxPraktikaPerYear: config?.maxPraktikaPerYear || 0,
    totalHoursCredit: config?.totalHoursCredit || 0,
    subjectSpecializations: config?.subjectSpecializations || [],
    internshipPreferences: config?.internshipPreferences || [],
  });

  const [subjectInput, setSubjectInput] = useState("");

  const handleChange = <K extends keyof TeacherPlConfigRequest>(
    key: K,
    value: TeacherPlConfigRequest[K]
  ) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const handleAddSubject = () => {
    if (subjectInput.trim() && !form.subjectSpecializations.includes(subjectInput.trim())) {
      handleChange("subjectSpecializations", [...form.subjectSpecializations, subjectInput.trim()]);
      setSubjectInput("");
    }
  };

  const handleRemoveSubject = (subject: string) => {
    handleChange(
      "subjectSpecializations",
      form.subjectSpecializations.filter((s) => s !== subject)
    );
  };

  const handleToggleInternshipPref = (pref: PraktikumType) => {
    const current = form.internshipPreferences;
    if (current.includes(pref)) {
      handleChange(
        "internshipPreferences",
        current.filter((p) => p !== pref)
      );
    } else {
      handleChange("internshipPreferences", [...current, pref]);
    }
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

  return (
    <div className="student-config-form-container">
      <h3 className="student-config-form-title">
        {config ? "Edit Teacher Configuration" : "Create Teacher Configuration"}
      </h3>
      <p className="teacher-name">
        Teacher: {teacher.firstName} {teacher.lastName}
      </p>

      <div className="student-config-split-layout">
        {/* Left Side: Basic Configuration */}
        <div className="student-config-left-section">
          <div className="student-config-field">
            <label htmlFor="schoolYear">School Year</label>
            <input
              id="schoolYear"
              className="student-config-input"
              type="text"
              placeholder="z.B. 2024"
              value={form.schoolYear}
              onChange={(e) => handleChange("schoolYear", e.target.value)}
            />
          </div>

          <div className="student-config-field">
            <label htmlFor="maxPraktika">Max Praktika Per Year</label>
            <input
              id="maxPraktika"
              className="student-config-input"
              type="number"
              min="0"
              value={form.maxPraktikaPerYear}
              onChange={(e) => handleChange("maxPraktikaPerYear", Number(e.target.value))}
            />
          </div>

          <div className="student-config-field">
            <label htmlFor="totalHours">Total Hours Credit</label>
            <input
              id="totalHours"
              className="student-config-input"
              type="number"
              min="0"
              value={form.totalHoursCredit}
              onChange={(e) => handleChange("totalHoursCredit", Number(e.target.value))}
            />
          </div>
        </div>

        {/* Right Side: Subjects & Internship Preferences */}
        <div className="student-config-right-section">
          <div className="student-config-field">
            <label htmlFor="subjectInput">Subject Specializations</label>
            <div className="subject-input-row">
              <input
                id="subjectInput"
                className="student-config-input"
                type="text"
                placeholder="Enter subject"
                value={subjectInput}
                onChange={(e) => setSubjectInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") {
                    e.preventDefault();
                    handleAddSubject();
                  }
                }}
              />
              <button
                type="button"
                className="student-config-btn btn-add-subject"
                onClick={handleAddSubject}
              >
                Add
              </button>
            </div>
            <div className="subject-tags">
              {form.subjectSpecializations.map((subject) => (
                <span key={subject} className="subject-tag">
                  {subject}
                  <button
                    type="button"
                    className="remove-tag"
                    onClick={() => handleRemoveSubject(subject)}
                  >
                    ×
                  </button>
                </span>
              ))}
            </div>
          </div>

          <div className="student-config-field" style={{marginTop: '20px'}}>
            <label style={{marginBottom: '12px', display: 'block'}}>Internship Preferences</label>
            <div className="checkbox-group">
              {Object.values(PraktikumType).map((pref) => (
                <label key={pref} className="checkbox-label">
                  <input
                    type="checkbox"
                    checked={form.internshipPreferences.includes(pref)}
                    onChange={() => handleToggleInternshipPref(pref)}
                  />
                  <span>{pref.replace("_", "-")}</span>
                </label>
              ))}
            </div>
          </div>
        </div>
      </div>

      <div className="student-config-form-actions">
        <button className="student-config-btn" onClick={handleSubmit}>
          {config ? "Update" : "Create"}
        </button>
        <button className="student-config-btn student-config-btn-cancel" onClick={onClose}>
          Cancel
        </button>
      </div>
    </div>
  );
}
