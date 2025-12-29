import type { FC } from "react";

const RightPanel: FC = () => (
  <div className="right-panel">
    {/* Quick Actions Panel */}
    <div className="section-container">
      <div className="section-header">
        <h2>⚡ Schnellaktionen</h2>
      </div>
      <div className="quick-actions-list">
        <button className="quick-action-btn">
          <span className="card-icon">➕</span>
          <span className="action-text">Neue Zuweisung starten</span>
        </button>
        <button className="quick-action-btn">
          <span className="card-icon">📥</span>
          <span className="action-text">Studierendenliste importieren</span>
        </button>
        <button className="quick-action-btn">
          <span className="card-icon">📄</span>
          <span className="action-text">Planungsbriefe generieren</span>
        </button>
      </div>
    </div>

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
  </div>
);

export default RightPanel;
