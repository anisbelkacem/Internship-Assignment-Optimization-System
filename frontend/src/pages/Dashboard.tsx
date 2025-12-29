import React from "react";
import RightPanel from "../components/RightPanel";
import "../styles/global.css";

const Dashboard: React.FC = () => {
  return (
    <div className="dashboard-root">
      {/* Current Semester Status */}
      <div className="dashboard-top">
        <div className="semester-header">
          <h1>Wintersemester 2024/2025 — Mission Control</h1>
          <button className="btn btn-ghost">Semester wechseln</button>
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
                <button className="btn btn-ghost btn-sm">Anzeigen</button>
              </div>

              <div className="status-card info">
                <div className="card-header-row">
                  <span className="card-icon">📅</span>
                  <div className="card-content">
                    <h3>Nächste Deadline</h3>
                    <p>PDP I Planung: 15. Jan 2025</p>
                  </div>
                </div>
                <button className="btn btn-ghost btn-sm">Details</button>
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
    </div>
  );
};

export default Dashboard;
