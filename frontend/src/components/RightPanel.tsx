import type { FC } from "react";

const RightPanel: FC = () => (
  <div className="right-panel">
    <div className="card zones-card">
      <div className="card-header">
        <h3>Schulen & Zonen</h3>
        <div className="card-count">100 Schulen</div>
      </div>

      <div className="zone-row">
        <div className="zone-info">
          <div className="zone-title">GS St. Nikola</div>
          <div className="zone-sub">Zone 1 • ÖPNV: Yes</div>
        </div>
        <div className="badge">5 PLs</div>
      </div>

      <div className="zone-row">
        <div className="zone-info">
          <div className="zone-title">MS Hohenau</div>
          <div className="zone-sub">Zone 3 • ÖPNV: No</div>
        </div>
        <div className="badge">2 PLs</div>
      </div>

      <button className="btn btn-primary full">Map View (placeholder)</button>
    </div>

    <div className="card quick-reports">
      <h3>Quick Reports</h3>
      <div className="report-row">
        <div className="report-inner">
          <div className="report-info">
            <div className="report-title">Vorjahr Einsatzplanung</div>
            <div className="report-sub">Historie mit PL-Zuweisungen</div>
          </div>
          <button className="btn report-download">Download</button>
        </div>
      </div>
      <div className="report-row">
        <div className="report-inner">
          <div className="report-info">
            <div className="report-title">Ermäßigungsstunden Übersicht</div>
            <div className="report-sub">Zuweisung pro Schulamt</div>
          </div>
          <button className="btn report-download">Download</button>
        </div>
      </div>
    </div>
  </div>
);

export default RightPanel;
