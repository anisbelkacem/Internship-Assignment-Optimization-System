/* eslint-disable @typescript-eslint/no-explicit-any */
import { useState } from "react";
import StudentConfigService from "../../services/studentConfigService";
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
        mainCourse: config?.mainCourse ?? "COMPUTER_SCIENCE",
        prefCourse1: config?.prefCourse1 ?? "COMPUTER_SCIENCE",
        prefCourse2: config?.prefCourse2 ?? "COMPUTER_SCIENCE",
        prefCourse3: config?.prefCourse3 ?? "COMPUTER_SCIENCE",
        year: config?.year ?? year,
    });

    const handleChange = <K extends keyof StudentConfigDto>(key: K, value: StudentConfigDto[K]) => {
        setForm(prev => ({ ...prev, [key]: value }));
    };

    const handleSubmit = async () => {
        try {
            const saved = config?.id
                ? await StudentConfigService.updateConfig(form.id!, form)
                : await StudentConfigService.createConfig(form);

            onSave(saved);
        } catch (err) {
            console.error("Config save failed", err);
        }
    };

    return (
        <div className="student-config-form-container">
            <h3 className="student-config-form-title">
                {config?.id ? "Edit Student Configuration" : "Create Student Configuration"}
            </h3>

            <div className="student-config-split-layout">
                {/* Left Side: Student Info */}
                <div className="student-config-left-section">
                    <div className="student-config-field">
                        <label className="required">Student ID</label>
                        <input
                            className="student-config-input"
                            type="number"
                            value={form.studentId}
                            onChange={e => handleChange("studentId", Number(e.target.value))}
                            required
                        />
                    </div>

                    <div className="student-config-field">
                        <label className="required">Year</label>
                        <input
                            className="student-config-input"
                            type="text"
                            value={form.year}
                            onChange={e => handleChange("year", e.target.value)}
                            placeholder="e.g., 2024/2025"
                            required
                        />
                    </div>

                    {/* School Type (shown for both create and edit) */}
                    <div className="student-config-field">
                        <label className="required">School Type</label>
                        <select
                            className="student-config-input"
                            value={form.schoolType}
                            onChange={e => handleChange("schoolType", e.target.value as typeof form.schoolType)}
                            required
                        >
                            <option value="GS">GS</option>
                            <option value="HS">MS</option>
                        </select>
                    </div>
                </div>

                {/* Right Side: Courses (shown for both create and edit) */}
                <div className="student-config-right-section">
                    {[
                        ["mainCourse", "Main Course"],
                        ["prefCourse1", "Course 1"],
                        ["prefCourse2", "Course 2"],
                        ["prefCourse3", "Course 3"]
                    ].map(([key, label]) => (
                        <div key={key} className="student-config-field">
                            <label className={label === "Main Course" ? "required" : undefined}>{label}</label>
                            <select
                                className="student-config-input"
                                value={form[key as keyof StudentConfigDto] as string}
                                onChange={e => handleChange(key as keyof StudentConfigDto, e.target.value as any)}
                                required={label === "Main Course"}
                            >
                                <option value="COMPUTER_SCIENCE">Computer Science</option>
                                <option value="ENGINEERING">Engineering</option>
                                <option value="BUSINESS">Business</option>
                                <option value="MEDICINE">Medicine</option>
                                <option value="LAW">Law</option>
                                <option value="ARTS">Arts</option>
                                <option value="SCIENCES">Sciences</option>
                                <option value="OTHER">Other</option>
                            </select>
                        </div>
                    ))}

                    <div className="student-config-field checkbox-list">
                        <label>Internship Types</label>
                        <div className="checkbox-row">
                            {["pdpI", "pdpII", "zsp", "sfp"].map(field => (
                                <label key={field} className="checkbox-pill">
                                    <input
                                        type="checkbox"
                                        checked={form[field as keyof StudentConfigDto] as boolean}
                                        onChange={e => handleChange(field as keyof StudentConfigDto, e.target.checked as any)}
                                    />
                                    <span>{field.toUpperCase()}</span>
                                </label>
                            ))}
                        </div>
                    </div>
                </div>
            </div>

            <div className="student-config-form-actions">
                <button className="btn btn-primary" onClick={handleSubmit}>
                    {config ? "Update" : "Create"}
                </button>
                <button
                    className="btn btn-secondary"
                    onClick={onClose}
                >
                    Cancel
                </button>
            </div>
        </div>
    );
}
