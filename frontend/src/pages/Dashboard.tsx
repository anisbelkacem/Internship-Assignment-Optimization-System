import React, { useState } from "react";
import RightPanel from "../components/RightPanel";
import "../styles/global.css";

const Dashboard: React.FC = () => {
  const [showSemesterMenu, setShowSemesterMenu] = useState(false);
  const [selectedSemester, setSelectedSemester] = useState("WiSe 24/25");
  const [showOverbookedModal, setShowOverbookedModal] = useState(false);
  const [showDeadlineModal, setShowDeadlineModal] = useState(false);
  const [showCalendarModal, setShowCalendarModal] = useState(false);

  const semesters = [
    "WiSe 24/25",
    "SoSe 25",
    "WiSe 25/26",
    "SoSe 26",
    "WiSe 26/27",
  ];

  const handleSemesterSelect = (semester: string) => {
    setSelectedSemester(semester);
    setShowSemesterMenu(false);
  };

  return (
    <div className="dashboard-root">
      {/* Current Semester Status */}
      <div className="dashboard-top">
        <div className="semester-header">
          <h1>{selectedSemester} — Mission Control</h1>
          <div className="semester-menu-container">
            <button 
              className="btn btn-ghost"
              onClick={() => setShowSemesterMenu(!showSemesterMenu)}
            >
              Semester wechseln
            </button>
            {showSemesterMenu && (
              <div className="semester-dropdown">
                {semesters.map((sem) => (
                  <button
                    key={sem}
                    className={`semester-option ${selectedSemester === sem ? "active" : ""}`}
                    onClick={() => handleSemesterSelect(sem)}
                  >
                    {sem}
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>
        <div className="card-row">
          <div className="status-card info">
            <div className="card-content">
              <p>Regelpraktikumsfälle</p>
              <h3>≈ 1,000</h3>
              <p className="card-subtext">pro Schuljahr</p>
            </div>
          </div>
          <div className="status-card success">
            <div className="card-content">
              <p>GS-PLs</p>
              <h3>169</h3>
              <p className="card-subtext">Ermäßigungsstunden verteilt</p>
            </div>
          </div>
          <div className="status-card success">
            <div className="card-content">
              <p>MS-PLs</p>
              <h3>41</h3>
              <p className="card-subtext">Ermäßigungsstunden verteilt</p>
            </div>
          </div>
          <div className="status-card primary">
            <div className="card-content">
              <p>Anrechnungsstunden Budget</p>
              <h3>210</h3>
              <p className="card-subtext">von 233 verteilt</p>
            </div>
          </div>
        </div>
      </div>

      <div className="dashboard-grid">
        <main className="main-column">
          {/* Action Items Section */}
          <section className="section-container">
            <div className="section-header">
              <h2>🎯 Aktionsbedarf</h2>
            </div>
            <div className="card-row">
              <div className="status-card urgent">
                <div className="card-header-row">
                  <span className="card-icon">⚠️</span>
                  <div className="card-content">
                    <h3>Nicht zugewiesen</h3>
                    <p>24 Praktika brauchen noch PLs</p>
                  </div>
                </div>
                <button className="btn btn-primary btn-sm">Jetzt zuweisen</button>
              </div>

              <div className="status-card warning">
                <div className="card-header-row">
                  <span className="card-icon">⚡</span>
                  <div className="card-content">
                    <h3>Überbucht</h3>
                    <p>3 PLs mit 3+ Praktika</p>
                  </div>
                </div>
                <button className="btn btn-ghost btn-sm" onClick={() => setShowOverbookedModal(true)}>Anzeigen</button>
              </div>

              <div className="status-card info">
                <div className="card-header-row">
                  <span className="card-icon">📅</span>
                  <div className="card-content">
                    <h3>Nächste Deadline</h3>
                    <p>PDP I Planung: 15. Jan 2025</p>
                  </div>
                </div>
                <button className="btn btn-ghost btn-sm" onClick={() => setShowDeadlineModal(true)}>Details</button>
              </div>
            </div>
          </section>

          {/* Kapazitätsauslastung */}
          <section className="section-container">
            <div className="section-header">
              <h2>📊 Kapazitätsauslastung</h2>
            </div>
            <div className="capacity-bars">
              <div className="capacity-item">
                <div className="capacity-row">
                  <span className="capacity-label">PDP I (Herbst)</span>
                  <span className="capacity-meta">8 / 12 PLs</span>
                </div>
                <div className="progress-bar">
                  <div className="progress-fill warn" style={{ width: "67%" }} />
                </div>
              </div>

              <div className="capacity-item">
                <div className="capacity-row">
                  <span className="capacity-label">PDP II (Frühjahr)</span>
                  <span className="capacity-meta">5 / 8 PLs</span>
                </div>
                <div className="progress-bar">
                  <div className="progress-fill ok" style={{ width: "62%" }} />
                </div>
              </div>

              <div className="capacity-item">
                <div className="capacity-row">
                  <span className="capacity-label">SFP (SoSe)</span>
                  <span className="capacity-meta">9 / 12 PLs</span>
                </div>
                <div className="progress-bar">
                  <div className="progress-fill ok" style={{ width: "75%" }} />
                </div>
              </div>

              <div className="capacity-item">
                <div className="capacity-row">
                  <span className="capacity-label">ZSP (WiSe)</span>
                  <span className="capacity-meta">4 / 6 PLs</span>
                </div>
                <div className="progress-bar">
                  <div className="progress-fill ok" style={{ width: "67%" }} />
                </div>
              </div>
            </div>
          </section>

          {/* Letzte Aktivitäten */}
          <section className="section-container">
            <div className="section-header">
              <h2>📝 Letzte Aktivitäten</h2>
            </div>
            <ul className="activity-list">
              <li className="activity-item">
                <div className="activity-icon">✅</div>
                <div className="activity-content">
                  <h3>Lisa Müller zugewiesen</h3>
                  <p>GS St. Nikola • PDP I Deutsch</p>
                </div>
                <span className="badge primary">vor 2 Std</span>
              </li>

              <li className="activity-item">
                <div className="activity-icon">📥</div>
                <div className="activity-content">
                  <h3>3 Studierende importiert</h3>
                  <p>CSV Upload • Wintersemester</p>
                </div>
                <span className="badge">heute</span>
              </li>

              <li className="activity-item">
                <div className="activity-icon">⚡</div>
                <div className="activity-content">
                  <h3>Überbuchung markiert</h3>
                  <p>PL Hohenau • 3 Praktika</p>
                </div>
                <span className="badge danger">vor 5 Std</span>
              </li>
            </ul>
          </section>

          {/* Zonen-Auslastung */}
          <section className="section-container">
            <div className="section-header">
              <h2>🗺️ Zonen-Auslastung</h2>
            </div>
            <div className="card-row">
              <div className="status-card info">
                <div className="card-content">
                  <h3>Zone 1 (Passau)</h3>
                  <p>45 / 60 PLs</p>
                  <div className="progress-bar small">
                    <div className="progress-fill ok" style={{ width: "75%" }} />
                  </div>
                </div>
              </div>

              <div className="status-card warning">
                <div className="card-content">
                  <h3>Zone 2 (Mittel)</h3>
                  <p>32 / 50 PLs</p>
                  <div className="progress-bar small">
                    <div className="progress-fill warn" style={{ width: "64%" }} />
                  </div>
                </div>
              </div>

              <div className="status-card info">
                <div className="card-content">
                  <h3>Zone 3 (Entfernt)</h3>
                  <p>89 / 100 PLs</p>
                  <div className="progress-bar small">
                    <div className="progress-fill ok" style={{ width: "89%" }} />
                  </div>
                </div>
              </div>
            </div>
          </section>
        </main>

        <aside className="right-column">
          <RightPanel />
        </aside>
      </div>

      {/* Overbooked Modal */}
      {showOverbookedModal && (
        <div className="modal-overlay" onClick={() => setShowOverbookedModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="student-form-container">
              <h3 className="student-form-title">Überbuchte Praktikumsleiter</h3>

              <div className="overbooked-list">
                <div className="overbooked-item">
                  <div className="item-header">
                    <h4>Dr. Robert Müller</h4>
                    <span className="badge danger">4 Praktika</span>
                  </div>
                  <p className="item-detail">Schultyp: GS • Zone: 1 (Passau)</p>
                  <p className="item-detail">Empfehlte Kapazität: 3 Praktika</p>
                </div>

                <div className="overbooked-item">
                  <div className="item-header">
                    <h4>Prof. Anna Schmidt</h4>
                    <span className="badge danger">5 Praktika</span>
                  </div>
                  <p className="item-detail">Schultyp: MS • Zone: 3 (Entfernt)</p>
                  <p className="item-detail">Empfehlte Kapazität: 4 Praktika</p>
                </div>

                <div className="overbooked-item">
                  <div className="item-header">
                    <h4>Hans Weber</h4>
                    <span className="badge danger">3 Praktika</span>
                  </div>
                  <p className="item-detail">Schultyp: GS • Zone: 2 (Mittel)</p>
                  <p className="item-detail">Empfehlte Kapazität: 2 Praktika</p>
                </div>
              </div>

              <div className="student-form-actions">
                <button 
                  className="btn btn-ghost" 
                  onClick={() => setShowOverbookedModal(false)}
                >
                  Schließen
                </button>
                <button className="btn btn-primary">Ausgleichen</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Deadline Modal */}
      {showDeadlineModal && (
        <div className="modal-overlay" onClick={() => setShowDeadlineModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="student-form-container">
              <h3 className="student-form-title">Anstehende Deadlines</h3>

              <div className="overbooked-list">
                <div className="overbooked-item" style={{borderLeftColor: '#ef4444'}}>
                  <div className="item-header">
                    <h4>PDP I Planung</h4>
                    <span className="badge danger">Dringend</span>
                  </div>
                  <p className="item-detail">📅 Deadline: 15. Januar 2025</p>
                  <p className="item-detail">Status: 18 von 24 Zuweisungen abgeschlossen</p>
                  <p className="item-detail">Verantwortlich: Praktikumsamt Team</p>
                </div>

                <div className="overbooked-item" style={{borderLeftColor: '#f59e0b'}}>
                  <div className="item-header">
                    <h4>SFP Dokumentation</h4>
                    <span className="badge warning">Bald</span>
                  </div>
                  <p className="item-detail">📅 Deadline: 20. Januar 2025</p>
                  <p className="item-detail">Status: Berichte ausstehend</p>
                  <p className="item-detail">Verantwortlich: PLs & Studierende</p>
                </div>

                <div className="overbooked-item" style={{borderLeftColor: '#3b82f6'}}>
                  <div className="item-header">
                    <h4>PDP II Vorbereitung</h4>
                    <span className="badge primary">Normal</span>
                  </div>
                  <p className="item-detail">📅 Deadline: 01. Februar 2025</p>
                  <p className="item-detail">Status: Planung läuft</p>
                  <p className="item-detail">Verantwortlich: Koordinatoren</p>
                </div>
              </div>

              <div className="student-form-actions">
                <button 
                  className="btn btn-ghost" 
                  onClick={() => setShowDeadlineModal(false)}
                >
                  Schließen
                </button>
                <button className="btn btn-primary" onClick={() => setShowCalendarModal(true)}>Kalender öffnen</button>
              </div>
            </div>

      {/* Calendar Modal */}
      {showCalendarModal && (
        <div className="modal-overlay" onClick={() => setShowCalendarModal(false)}>
          <div className="modal-content modal-calendar" onClick={(e) => e.stopPropagation()}>
            <div className="student-form-container">
              <h3 className="student-form-title">📅 Kalender - Januar 2025</h3>

              <div className="calendar-grid">
                <div className="calendar-header">Mo</div>
                <div className="calendar-header">Di</div>
                <div className="calendar-header">Mi</div>
                <div className="calendar-header">Do</div>
                <div className="calendar-header">Fr</div>
                <div className="calendar-header">Sa</div>
                <div className="calendar-header">So</div>

                {/* Empty cells for days before month starts */}
                <div className="calendar-day empty"></div>
                <div className="calendar-day empty"></div>

                {/* January days */}
                {[1,2,3,4,5,6,7,8,9,10,11,12,13,14].map(day => (
                  <div key={day} className="calendar-day">{day}</div>
                ))}
                <div className="calendar-day deadline-day">15</div>
                {[16,17,18,19].map(day => (
                  <div key={day} className="calendar-day">{day}</div>
                ))}
                <div className="calendar-day deadline-day">20</div>
                {[21,22,23,24,25,26,27,28,29,30,31].map(day => (
                  <div key={day} className="calendar-day">{day}</div>
                ))}
              </div>

              <div className="calendar-legend">
                <div className="legend-item">
                  <span className="legend-color danger"></span>
                  <span>Dringend</span>
                </div>
                <div className="legend-item">
                  <span className="legend-color warning"></span>
                  <span>Bald</span>
                </div>
                <div className="legend-item">
                  <span className="legend-color primary"></span>
                  <span>Normal</span>
                </div>
              </div>

              <div className="student-form-actions">
                <button 
                  className="btn btn-ghost" 
                  onClick={() => setShowCalendarModal(false)}
                >
                  Schließen
                </button>
                <button 
                  className="btn btn-primary" 
                  onClick={() => {
                    setShowCalendarModal(false);
                    setShowDeadlineModal(true);
                  }}
                >
                  Zurück
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
