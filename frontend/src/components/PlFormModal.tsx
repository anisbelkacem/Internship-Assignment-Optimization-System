import React from "react";
import { Course } from "../services/plService";

export interface SchoolOption {
  id: number;
  name: string;
}

export interface PlFormValues {
  teacherId?: number;
  firstName: string;
  lastName: string;
  email: string;
  mainSubject: Course | "";
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

const COURSE_OPTIONS: { value: Course; label: string }[] = [
  { value: Course.COMPUTER_SCIENCE, label: "Informatik" },
  { value: Course.ENGINEERING, label: "Ingenieurwesen" },
  { value: Course.BUSINESS, label: "Wirtschaft" },
  { value: Course.MEDICINE, label: "Medizin" },
  { value: Course.LAW, label: "Recht" },
  { value: Course.ARTS, label: "Kunst" },
  { value: Course.SCIENCES, label: "Naturwissenschaften" },
  { value: Course.OTHER, label: "Sonstiges" },
];

const PlFormModal: React.FC<PlFormModalProps> = ({
  isOpen,
  mode,
  form,
  schools,
  onChange,
  onClose,
  onSubmit,
}) => {
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
              <label className="form-label required" htmlFor="mainSubject">
                Hauptfach
              </label>
              <select
                id="mainSubject"
                name="mainSubject"
                className="form-input"
                value={form.mainSubject}
                onChange={onChange}
                required
              >
                <option value="">Bitte wählen</option>
                {COURSE_OPTIONS.map((c) => (
                  <option key={c.value} value={c.value}>
                    {c.label}
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
            <button
              type="button"
              className="btn-cancel"
              onClick={onClose}
            >
              Abbrechen
            </button>
            <button type="submit" className="btn-primary">
              {mode === "create" ? "Hinzufügen" : "Aktualisieren"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default PlFormModal;
