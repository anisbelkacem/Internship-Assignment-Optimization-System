import { useState, useEffect } from "react";
import type { PlannedInternshipDto } from "../../services/internshipAssignmentService";
import type { TeacherDto } from "../../services/plService";
import plService from "../../services/plService";
import schoolService from "../../services/schoolService";
import type { School } from "../../services/schoolService";

interface EditPlannedInternshipFormProps {
  internship: PlannedInternshipDto;
  onClose: () => void;
  onSave: () => void;
}

type ValidationViolation = {
  code: string;
  severity: "HARD" | "WARNING";
  message: string;
  fields: string[];
};

type ValidationResult = {
  hardValid: boolean;
  hardViolations: ValidationViolation[];
  warnings: ValidationViolation[];
};

export default function EditPlannedInternshipForm({
  internship,
  onClose,
  onSave,
}: EditPlannedInternshipFormProps) {
  const [selectedTeacherId, setSelectedTeacherId] = useState<number | null>(
    internship.teacherId ? parseInt(internship.teacherId) : null
  );
  const [selectedSchoolId, setSelectedSchoolId] = useState<number | null>(
    internship.schoolId ?? null
  );
  const [teachers, setTeachers] = useState<TeacherDto[]>([]);
  const [schools, setSchools] = useState<School[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [validation, setValidation] = useState<ValidationResult | null>(null);
  const [validating, setValidating] = useState(false);


  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
  validate(selectedTeacherId, selectedSchoolId);
  // eslint-disable-next-line react-hooks/exhaustive-deps
}, [selectedTeacherId, selectedSchoolId]);


  const hasFieldViolation = (field: string) => {
  if (!validation) return false;
  return validation.hardViolations.some(v => v.fields?.includes(field));
};
const validate = async (tId: number | null, sId: number | null) => {
  setValidating(true);
  try {
    const baseUrl = import.meta.env.VITE_API_URL || "http://localhost:8080";
    const res = await fetch(`${baseUrl}/api/validation/planned-internships/update`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${sessionStorage.getItem("token")}`,
      },
      body: JSON.stringify({
        internshipId: internship.id,
        teacherId: tId,
        schoolId: sId,
      }),
    });

    if (!res.ok) throw new Error("Validation call failed");
    const data = (await res.json()) as ValidationResult;
    setValidation(data);
  } finally {
    setValidating(false);
  }
};


  const loadData = async () => {
    try {
      const [teachersData, schoolsData] = await Promise.all([
        plService.getAllPls(),
        schoolService.getAllSchools(),
      ]);
      setTeachers(teachersData);
      setSchools(schoolsData);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load data");
    }
  };

  const handleSave = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetch(
        `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/api/planned-internships/${internship.id}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${sessionStorage.getItem("token")}`,
          },
          body: JSON.stringify({
            teacherId: selectedTeacherId,
            schoolId: selectedSchoolId,
          }),
        }
      );

      if (!response.ok) {
        throw new Error("Failed to update assignment");
      }

      onSave();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update assignment");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-form-container">
      <div className="modal-form-header">
        <h2>Praktikumszuweisung bearbeiten</h2>
      </div>

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}

      <div className="modal-form-body">
        <div className="form-section">
          <h3>Praktikum Details</h3>
          <div className="form-row">
            <div className="form-group">
              <label>Praktikumtyp</label>
              <input
                type="text"
                className="form-input"
                value={internship.praktikumType}
                disabled
                readOnly
              />
            </div>
            <div className="form-group">
              <label>Schultyp</label>
              <input
                type="text"
                className="form-input"
                value={internship.schoolType}
                disabled
                readOnly
              />
            </div>
          </div>

          {internship.course && (
            <div className="form-group">
              <label>Kurs</label>
              <input
                type="text"
                className="form-input"
                value={internship.course}
                disabled
                readOnly
              />
            </div>
          )}

          <div className="form-group">
            <label>Kapazität</label>
            <input
              type="text"
              className="form-input"
              value={internship.maxCapacity}
              disabled
              readOnly
            />
          </div>
        </div>

        <div className="form-section">
          <h3>Zuweisung</h3>
          
          <div className="form-group">
            <label htmlFor="teacher">Lehrkraft *</label>
            <select
              id="teacher"
                className={`form-select ${hasFieldViolation("teacherId") ? "invalid" : ""}`}
              value={selectedTeacherId ?? ""}
              onChange={(e) => setSelectedTeacherId(e.target.value ? parseInt(e.target.value) : null)}
            >
              <option value="">-- Keine Lehrkraft --</option>
              {teachers.map((teacher) => (
                <option key={teacher.teacherId} value={teacher.teacherId}>
                  {teacher.firstName} {teacher.lastName} ({teacher.mainSubject.name})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="school">Schule *</label>
            <select
              id="school"
              className={`form-select ${hasFieldViolation("schoolId") ? "invalid" : ""}`}
              value={selectedSchoolId ?? ""}
              onChange={(e) => setSelectedSchoolId(e.target.value ? parseInt(e.target.value) : null)}
            >
              <option value="">-- Keine Schule --</option>
              {schools.map((school) => (
                <option key={school.id} value={school.id}>
                  {school.name} (Zone {school.zone}, {school.type})
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>
      {validation && !validation.hardValid && (
  <div className="error-message">
    <strong>Nicht speicherbar:</strong>
    <ul>
      {validation.hardViolations.map((v, idx) => (
            <li key={idx}>{v.message}</li>
            ))}
            </ul>
          </div>
        )}

      <div className="modal-form-actions">
        <button
          type="button"
          className="btn-cancel"
          onClick={onClose}
          disabled={loading}
        >
          Abbrechen
        </button>
        <button
          type="button"
          className="btn-save"
          onClick={handleSave}
           disabled={loading || validating || (validation ? !validation.hardValid : false)}
>
  {loading ? "Speichern..." : validating ? "Prüfe..." : "Speichern"}
        </button>
      </div>
    </div>
  );
}
