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

                    {/* Only show School Type when editing */}
                    {config && (
                        <div className="student-config-field">
                            <label>School Type</label>
                            <select
                                className="student-config-input"
                                value={form.schoolType}
                                onChange={e => handleChange("schoolType", e.target.value as typeof form.schoolType)}
                            >
                                <option value="GS">GS</option>
                                <option value="HS">MS</option>
                            </select>
                        </div>
                    )}

                    {/* Checkboxes */}
                    <div className="student-config-field" style={{marginTop: '20px'}}>
                        <label style={{marginBottom: '12px', display: 'block'}}>Internship Types</label>
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
                {config && (
                    <div className="student-config-right-section">
                        {[
                            ["mainCourse", "Main Course"],
                            ["prefCourse1", "Preferred Course 1"],
                            ["prefCourse2", "Preferred Course 2"],
                            ["prefCourse3", "Preferred Course 3"]
                        ].map(([key, label]) => (
                            <div key={key} className="student-config-field">
                                <label>{label}</label>
                                <select
                                    className="student-config-input"
                                    value={form[key as keyof StudentConfigDto] as string}
                                    onChange={e => handleChange(key as keyof StudentConfigDto, e.target.value as any)}
                                >
                                    <option value="COMPUTER_SCIENCE">COMPUTER_SCIENCE</option>
                                    <option value="ENGINEERING">ENGINEERING</option>
                                    <option value="BUSINESS">BUSINESS</option>
                                    <option value="MEDICINE">MEDICINE</option>
                                    <option value="LAW">LAW</option>
                                    <option value="ARTS">ARTS</option>
                                    <option value="SCIENCES">SCIENCES</option>
                                    <option value="OTHER">OTHER</option>
                                </select>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            <div className="student-config-form-actions">
                <button className="student-config-btn" onClick={handleSubmit}>
                    {config ? "Update" : "Create"}
                </button>
                <button
                    className="student-config-btn student-config-btn-cancel"
                    onClick={onClose}
                >
                    Cancel
                </button>
            </div>
        </div>
    );
}
