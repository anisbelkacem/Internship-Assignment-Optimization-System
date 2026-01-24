import type { FC } from "react";
import { useState } from "react";
import StudentForm from "./Student/StudentForm";
import type { Student } from "../services/studentService";

const RightPanel: FC = () => {
  const [showStudentModal, setShowStudentModal] = useState(false);
  const [showReportModal, setShowReportModal] = useState(false);

  const handleStudentSave = (student: Student) => {
    console.log("Student saved:", student);
    setShowStudentModal(false);
    // Optionally refresh student list or show success message
  };

  return (
  <div className="right-panel">
    {/* Map Section */}
    <div className="section-container">
      <div className="section-header">
        <h2>🗺️ Schulen & Zonen</h2>
        <span className="badge">100 Schulen</span>
      </div>

      <div className="school-list">
        <div className="school-item">
          <div className="card-content">
            <h3>GS St. Nikola</h3>
            <p>Zone 1 • ÖPNV: Yes</p>
          </div>
          <span className="badge primary">5 PLs</span>
        </div>

        <div className="school-item">
          <div className="card-content">
            <h3>MS Hohenau</h3>
            <p>Zone 3 • ÖPNV: No</p>
          </div>
          <span className="badge primary">2 PLs</span>
        </div>
      </div>

      <button className="btn btn-primary btn-full">Kartenansicht öffnen</button>
    </div>

    {/* Add Student Modal */}
    {showStudentModal && (
      <div className="modal-overlay" onClick={() => setShowStudentModal(false)}>
        <div className="modal-content" onClick={(e) => e.stopPropagation()}>
          <StudentForm 
            student={null}
            onClose={() => setShowStudentModal(false)}
            onSave={handleStudentSave}
          />
        </div>
      </div>
    )}

    {/* Generate Report Modal */}
    {showReportModal && (
      <div className="modal-overlay" onClick={() => setShowReportModal(false)}>
        <div className="modal-content" onClick={(e) => e.stopPropagation()}>
          <div className="student-form-container">
            <h3 className="student-form-title">Bericht erstellen</h3>

            <div className="student-field">
              <label className="required" htmlFor="reportType">Berichtstyp</label>
              <select id="reportType" className="student-input">
                <option>Planungsbriefe</option>
                <option>Zuweisungsübersicht</option>
                <option>Kapazitätsauslastung</option>
                <option>Studierende nach Zone</option>
              </select>
            </div>

            <div className="student-field">
              <label className="required" htmlFor="semester">Semester</label>
              <select id="semester" className="student-input">
                <option>WiSe 24/25</option>
                <option>SoSe 25</option>
                <option>WiSe 25/26</option>
              </select>
            </div>

            <div className="student-field">
              <label className="required" htmlFor="format">Format</label>
              <select id="format" className="student-input">
                <option>PDF</option>
                <option>Excel</option>
                <option>CSV</option>
              </select>
            </div>

            <div className="student-form-actions">
              <button 
                className="btn btn-ghost" 
                onClick={() => setShowReportModal(false)}
              >
                Abbrechen
              </button>
              <button className="btn btn-primary">Generieren</button>
            </div>
          </div>
        </div>
      </div>
    )}
  </div>
);
};

export default RightPanel;
