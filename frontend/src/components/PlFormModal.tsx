import React from "react";
import { useEffect, useState } from "react";
import courseService from "../services/courseService";
import type { Course } from "../services/courseService";

export interface SchoolOption {
  id: number;
  name: string;
}

export interface PlFormValues {
  teacherId?: number;
  firstName: string;
  lastName: string;
  email: string;
  mainSubjectId: number | "";
  schoolId: number | "";
}

export interface PlFormModalProps {
  isOpen: boolean;
  mode: "create" | "edit";
  form: PlFormValues;
  schools: SchoolOption[];
  onChange: (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => void;
  onClose: () => void;
  onSubmit: (e: React.FormEvent) => void;
}

const PlFormModal: React.FC<PlFormModalProps> = ({
  isOpen,
  mode,
  form,
  schools,
  onChange,
  onClose,
  onSubmit,
}) => {
    const [courses, setCourses] = useState<Course[]>([]);

  useEffect(() => {
    courseService.getAllCourses().then(data => {
      setCourses(data.filter(c => c.active));
    });
  }, []);
  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal-content"
        onClick={(e) => e.stopPropagation()}
        style={{ maxWidth: "600px" }}
      >
        <div className="modal-header">
          <h2>
            {mode === "create" ? "Neuen PL hinzufügen" : "PL bearbeiten"}
          </h2>
        </div>
        <form onSubmit={onSubmit}>
          <div className="modal-body">
            <div className="form-group">
              <label className="form-label required" htmlFor="firstName">
                Vorname
              </label>
              <input
                type="text"
                id="firstName"
                name="firstName"
                className="form-input"
                value={form.firstName}
                onChange={onChange}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label required" htmlFor="lastName">
                Nachname
              </label>
              <input
                type="text"
                id="lastName"
                name="lastName"
                className="form-input"
                value={form.lastName}
                onChange={onChange}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label required" htmlFor="email">
                E-Mail
              </label>
              <input
                type="email"
                id="email"
                name="email"
                className="form-input"
                value={form.email}
                onChange={onChange}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label required" htmlFor="mainSubjectId">
                Hauptfach
              </label>
              <select
                id="mainSubject"
                name="mainSubjectId"
                className="form-input"
                value={form.mainSubjectId}
                onChange={onChange}
                required
              >
                <option value="">Bitte wählen</option>
                {courses.map(c => (
                  <option key={c.id} value={c.id}>
                    {c.name.charAt(0).toUpperCase() + c.name.slice(1)}
                  </option>
                ))}
              </select>


            </div>

            <div className="form-group">
              <label className="form-label required" htmlFor="schoolId">
                Schule
              </label>
              <select
                id="schoolId"
                name="schoolId"
                className="form-input"
                value={form.schoolId}
                onChange={onChange}
                required
              >
                <option value="">Bitte wählen</option>
                {schools.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="modal-footer">
            <button type="submit" className="btn btn-primary">
              {mode === "create" ? "Hinzufügen" : "Aktualisieren"}
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={onClose}
            >
              Abbrechen
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default PlFormModal;
