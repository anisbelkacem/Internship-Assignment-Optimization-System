import { useState } from "react";
import "../styles/Reports.css";
import API_BASE_URL from '../config/api';

type Status = "Assigned" | "Not Assigned" | "Completed";

interface TrendPoint {
  year: number;
  planned: number;
  completed: number;
  budgetUsed: number;
}

interface WorkloadEntry {
  label: string;
  value: number;
}

const trendData: TrendPoint[] = [
  { year: 2022, planned: 820, completed: 790, budgetUsed: 185 },
  { year: 2023, planned: 950, completed: 910, budgetUsed: 210 },
  { year: 2024, planned: 1000, completed: 940, budgetUsed: 225 },
];

const workloadData: WorkloadEntry[] = [
  { label: "GS-PLs", value: 72 },
  { label: "MS-PLs", value: 63 },
];

export default function Reports() {
  const [semester, setSemester] = useState<string>("");
  const [district, setDistrict] = useState<string>("");
  const [internshipType, setInternshipType] = useState<string>("");
  const [zone, setZone] = useState<string>("");
  const [status, setStatus] = useState<string>("");

  const handleExcelExport = async () => {
    try {
      const token = sessionStorage.getItem('token');
      const params = new URLSearchParams();
      if (semester) params.append("schoolYear", semester);
      
      const response = await fetch(
        `${API_BASE_URL}/api/internship-assignments/export?${params.toString()}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );
      if (!response.ok) throw new Error("Export failed");
      
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `assignments_${semester || "all"}.xlsx`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      console.error("Error exporting assignments:", error);
    }
  };

  const semesters = ["WiSe 24/25", "SoSe 24", "WiSe 23/24", "SoSe 23"];
  const districts = ["Passau", "Deggendorf", "Regen", "Freyung", "Landau"];
  const internshipTypes = ["PDP I", "PDP II", "SFP", "ZSP"];
  const zones = ["1", "2", "3"];
  const statuses: Status[] = ["Assigned", "Not Assigned", "Completed"];

  return (
    <div className="reports-root">      {/* Critical Alerts Summary */}
      <section className="alerts-summary">
        <div className="alert-pill urgent">
          <span className="alert-icon">⛔</span>
          <div>
            <span className="alert-label">Constraint Violations</span>
            <span className="alert-value">10</span>
          </div>
        </div>
        <div className="alert-pill warning">
          <span className="alert-icon">⚠️</span>
          <div>
            <span className="alert-label">Overbooked PLs</span>
            <span className="alert-value">3</span>
          </div>
        </div>
        <div className="alert-pill warning">
          <span className="alert-icon">📚</span>
          <div>
            <span className="alert-label">Subject Gaps</span>
            <span className="alert-value">2</span>
          </div>
        </div>
        <div className="alert-pill info">
          <span className="alert-icon">📊</span>
          <div>
            <span className="alert-label">Budget Used</span>
            <span className="alert-value">90%</span>
          </div>
        </div>
      </section>
      {/* Filters & Exports */}
      <section className="section-container">
        <div className="section-header">
          <h2>🔍 Filter & Export</h2>
          <div className="section-header-actions">
            <button className="btn btn-ghost" onClick={handleExcelExport}>📊 Excel</button>
            <button className="btn btn-ghost">📄 PDF</button>
            <button className="btn btn-primary">📬 Briefe</button>
            <button className="btn btn-secondary">📈 Historie</button>
          </div>
        </div>

        <div className="filters-grid">
          <div className="filter-field">
            <label>Semester</label>
            <select value={semester} onChange={(e) => setSemester(e.target.value)}>
              <option value="">Alle Semester</option>
              {semesters.map((s) => (
                <option key={s} value={s}>{s}</option>
              ))}
            </select>
          </div>
          <div className="filter-field">
            <label>Schulamt</label>
            <select value={district} onChange={(e) => setDistrict(e.target.value)}>
              <option value="">Alle Schulämter</option>
              {districts.map((d) => (
                <option key={d} value={d}>{d}</option>
              ))}
            </select>
          </div>
          <div className="filter-field">
            <label>Praktikumsart</label>
            <select value={internshipType} onChange={(e) => setInternshipType(e.target.value)}>
              <option value="">Alle Praktika</option>
              {internshipTypes.map((t) => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>
          </div>
          <div className="filter-field">
            <label>Zone</label>
            <select value={zone} onChange={(e) => setZone(e.target.value)}>
              <option value="">Alle Zonen</option>
              {zones.map((z) => (
                <option key={z} value={z}>{z}</option>
              ))}
            </select>
          </div>
          <div className="filter-field">
            <label>Status</label>
            <select value={status} onChange={(e) => setStatus(e.target.value)}>
              <option value="">Alle Status</option>
              {statuses.map((s) => (
                <option key={s} value={s}>{s}</option>
              ))}
            </select>
          </div>
        </div>
      </section>

      {/* Constraint Violations */}
      <section className="section-container">
        <div className="section-header">
          <h2>⛔ Constraint Violations</h2>
        </div>
        <div className="card-row">
          <div className="status-card urgent">
            <div className="card-header-row">
              <span className="card-icon">⛔</span>
              <div className="card-content">
                <h3>Overbooked PLs</h3>
                <p>3 PLs mit 3+ Praktika</p>
              </div>
            </div>
            <span className="badge danger">Müller, König, Wagner</span>
          </div>

          <div className="status-card warning">
            <div className="card-header-row">
              <span className="card-icon">❌</span>
              <div className="card-content">
                <h3>Forbidden Combinations</h3>
                <p>2 invalid PL-praktika pairs</p>
              </div>
            </div>
            <span className="badge danger">Review needed</span>
          </div>

          <div className="status-card warning">
            <div className="card-header-row">
              <span className="card-icon">🚫</span>
              <div className="card-content">
                <h3>Zone Violations</h3>
                <p>5 Wed-praktika in Zone 3</p>
              </div>
            </div>
            <span className="badge warning">Reassign</span>
          </div>
        </div>
      </section>

      {/* School District Summary */}
      <section className="section-container">
        <div className="section-header">
          <h2>🏛️ Schulamt Übersicht</h2>
        </div>
        <div className="district-grid">
          <div className="district-card">
            <div className="card-content">
              <h3>Passau Stadt</h3>
              <div className="district-metric">
                <span>Budget</span>
                <span className="badge primary">45/50 Std</span>
              </div>
              <div className="progress-bar small">
                <div className="progress-fill ok" style={{ width: "90%" }} />
              </div>
              <div className="district-metric">
                <span>PLs genutzt</span>
                <span className="badge primary">32/35</span>
              </div>
            </div>
          </div>

          <div className="district-card">
            <div className="card-content">
              <h3>Regen</h3>
              <div className="district-metric">
                <span>Budget</span>
                <span className="badge warning">12/15 Std</span>
              </div>
              <div className="progress-bar small">
                <div className="progress-fill warn" style={{ width: "80%" }} />
              </div>
              <div className="district-metric">
                <span>PLs genutzt</span>
                <span className="badge warning">18/20</span>
              </div>
            </div>
          </div>

          <div className="district-card">
            <div className="card-content">
              <h3>Deggendorf</h3>
              <div className="district-metric">
                <span>Budget</span>
                <span className="badge primary">38/45 Std</span>
              </div>
              <div className="progress-bar small">
                <div className="progress-fill ok" style={{ width: "84%" }} />
              </div>
              <div className="district-metric">
                <span>PLs genutzt</span>
                <span className="badge primary">28/32</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Subject Gap Analysis */}
      <section className="section-container">
        <div className="section-header">
          <h2>📚 Fächerbedarf Analyse</h2>
        </div>
        <div className="gap-analysis">
          <div className="gap-item">
            <div className="gap-header">
              <span>Deutsch GS</span>
              <span className="badge danger">Gap: -2</span>
            </div>
            <div className="gap-row">
              <span>Benötigt: 12</span>
              <div className="progress-bar small">
                <div className="progress-fill danger" style={{ width: "83%" }} />
              </div>
              <span>Geplant: 10</span>
            </div>
          </div>

          <div className="gap-item">
            <div className="gap-header">
              <span>Englisch GS</span>
              <span className="badge primary">OK</span>
            </div>
            <div className="gap-row">
              <span>Benötigt: 10</span>
              <div className="progress-bar small">
                <div className="progress-fill ok" style={{ width: "100%" }} />
              </div>
              <span>Geplant: 10</span>
            </div>
          </div>

          <div className="gap-item">
            <div className="gap-header">
              <span>Mathematik MS</span>
              <span className="badge primary">+1</span>
            </div>
            <div className="gap-row">
              <span>Benötigt: 8</span>
              <div className="progress-bar small">
                <div className="progress-fill ok" style={{ width: "112%" }} />
              </div>
              <span>Geplant: 9</span>
            </div>
          </div>
        </div>
      </section>

      {/* Historical Comparison */}
      <section className="section-container">
        <div className="section-header">
          <h2>📊 Semester Vergleich</h2>
        </div>
        <div className="comparison-grid">
          <div className="comparison-card">
            <div className="comparison-header">
              <h3>WiSe 24/25</h3>
              <span className="badge primary">Aktuell</span>
            </div>
            <div className="comparison-stat">
              <span>Zugewiesen</span>
              <h4>845</h4>
            </div>
            <div className="comparison-stat">
              <span>Budget genutzt</span>
              <h4>210 / 233 Std</h4>
            </div>
          </div>

          <div className="comparison-card">
            <div className="comparison-header">
              <h3>SoSe 24</h3>
              <span className="badge">Vergleich</span>
            </div>
            <div className="comparison-stat">
              <span>Zugewiesen</span>
              <h4>812</h4>
            </div>
            <div className="comparison-stat">
              <span>Budget genutzt</span>
              <h4>185 / 233 Std</h4>
            </div>
          </div>

          <div className="comparison-card diff">
            <div className="comparison-header">
              <h3>Differenz</h3>
              <span className="badge primary">Δ</span>
            </div>
            <div className="comparison-stat">
              <span>Zugewiesen</span>
              <h4 className="positive">+33 ↑</h4>
            </div>
            <div className="comparison-stat">
              <span>Budget</span>
              <h4 className="positive">+25 Std ↑</h4>
            </div>
          </div>
        </div>
      </section>

      <section className="section-container">
        <div className="section-header">
          <h2>📈 Mehrjahrestrends</h2>
        </div>
        <div className="trend-grid">
          {trendData.map((t) => (
            <div key={t.year} className="trend-card">
              <div className="card-content">
                <h3>{t.year}</h3>
                <p>Geplant: {t.planned}</p>
                <p>Abgeschlossen: {t.completed}</p>
                <p>Budget genutzt: {t.budgetUsed} Std</p>
                <div className="progress-bar small">
                  <div className="progress-fill" style={{ width: `${Math.round((t.completed / t.planned) * 100)}%` }} />
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Visualizations */}
      <section className="section-container">
        <div className="section-header">
          <h2>📊 Visualisierungen</h2>
        </div>
        <div className="viz-grid">
          <div className="viz-card">
            <h3>Budget-Auslastung</h3>
            <div className="placeholder-chart">Budget over time (placeholder)</div>
          </div>
          <div className="viz-card">
            <h3>Workload Distribution (GS/MS)</h3>
            <div className="placeholder-chart">
              {workloadData.map((w) => (
                <div key={w.label} className="progress-row">
                  <span>{w.label}</span>
                  <div className="progress-bar small">
                    <div className="progress-fill" style={{ width: `${w.value}%` }} />
                  </div>
                  <span className="progress-value">{w.value}%</span>
                </div>
              ))}
            </div>
          </div>
          <div className="viz-card">
            <h3>Zonen-Nutzung</h3>
            <div className="placeholder-chart">Zone usage patterns (placeholder)</div>
          </div>
          <div className="viz-card">
            <h3>Fächerbedarf</h3>
            <div className="placeholder-chart">Subject demand forecast (placeholder)</div>
          </div>
        </div>
      </section>
    </div>
  );
}
