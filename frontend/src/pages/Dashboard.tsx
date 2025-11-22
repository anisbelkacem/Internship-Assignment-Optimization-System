import React from "react";
import StatCard from "../components/StatCard";
import PlanningCard from "../components/PlanningCard";
import PLTable from "../components/PLTable";
import RightPanel from "../components/RightPanel";
import "../styles/global.css";
const Dashboard: React.FC = () => {
  return (
    <div className="dashboard-root">
      <div className="dashboard-top">
        <div className="stats-row">
          <StatCard title="Regelpraktikumsfälle" value="≈ 1,000" small="pro Schuljahr" />
          <StatCard title="GS-PLs" value="169" small="Ermäßigungsstunden verteilt" />
          <StatCard title="MS-PLs" value="41" small="Ermäßigungsstunden verteilt" />
          <StatCard title="Anrechnungsstunden Budget (heute)" value="210" small="gesamt" />
        </div>
      </div>
      <div className="dashboard-grid">
        <main className="main-column">
          <section className="planning-board">
            <div className="planning-board-header">
              <h2>Planungsboard</h2>
              <div className="planning-actions">
                <button className="btn btn-ghost">Wintersemester (WiSe)</button>
                <button className="btn btn-primary">Zuweisen</button>
              </div>
            </div>
            <div className="planning-cards">
              <PlanningCard title="PDP I (Herbst)" items={[{ label: "Deutsch", needed: "12 PLs", assigned: "8" }, { label: "Englisch", needed: "10 PLs", assigned: "6" }]} />
              <PlanningCard title="PDP II (Frühjahr)" items={[{ label: "Mathematik", needed: "8 PLs", assigned: "5" }]} />
              <PlanningCard title="SFP (SoSe)" items={[{ label: "Deutsch", needed: "12 PLs", assigned: "9" }]} />
              <PlanningCard title="ZSP (WiSe)" items={[{ label: "Englisch", needed: "6 PLs", assigned: "4" }]} />
            </div>
          </section>
          <section className="pls-table-section">
            <div className="pls-header">
              <h2>Praktikumslehrkräfte — Übersicht</h2>
              <div className="pls-actions">
                <button className="btn-ghost">Neu hinzufügen</button>
                <button className="btn-primary">Zuweisen</button>
              </div>
            </div>
            <PLTable />
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
