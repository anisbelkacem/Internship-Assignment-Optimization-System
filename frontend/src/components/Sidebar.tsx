import type { FC } from "react";
import { useState, useEffect } from "react";
import { NavLink } from "react-router-dom";
import logo from "../assets/Uni.png";

const Sidebar: FC = () => {
  const [open, setOpen] = useState(false);

  useEffect(() => {
    const listener = () => setOpen((v) => !v);
    window.addEventListener("toggleSidebar", listener as EventListener);
    return () => window.removeEventListener("toggleSidebar", listener as EventListener);
  }, []);

  const close = () => setOpen(false);

  return (
    <aside className={`app-sidebar ${open ? "open" : ""}`}>
      <div className="sidebar-top">
        <img src={logo} alt="Universitätslogo" className="sidebar-logo" />
        <div className="sidebar-brand">
          Praktikumsamt
          <span className="small">Universität Passau</span>
        </div>
      </div>
      <div className="aside-divider" />
      <nav>
        <ul>
          <li>
            <NavLink to="/dashboard" className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
              <span className="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden>
                  <path d="M3 11.5L12 4l9 7.5" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M5 21h14a1 1 0 0 0 1-1V11" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </span>
              Übersicht
            </NavLink>
          </li>
          <li>
            <NavLink to="/students" className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
              <span className="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden>
                  <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4z" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M6 20v-1a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v1" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </span>
              Studierende
            </NavLink>
          </li>
          <li>
            <NavLink to="/pls" className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
              <span className="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden>
                  <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4z" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M4 21v-2a4 4 0 0 1 4-4h8a4 4 0 0 1 4 4v2" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </span>
              PLs (Betreuer)
            </NavLink>
          </li>
          <li>
            <NavLink to="/schools" className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
              <span className="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden>
                  <path d="M3 11.5L12 4l9 7.5" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M9 21V12h6v9" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </span>
              Schulen
            </NavLink>
          </li>
                    {/* NEW: Courses Navigation Item */}
          <li>
            <NavLink to="/courses" className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
              <span className="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden>
                  <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M8 7h8M8 11h8M8 15h5" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </span>
              Kurse
            </NavLink>
          </li>
          <li></li>
          <li>
            <NavLink to="/assignments" className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
              <span className="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden>
                  <rect x="3" y="7" width="18" height="14" rx="2" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M16 3v4" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </span>
              Praktikumsplanung
            </NavLink>
          </li>
          <li>
            <NavLink to="/reports" className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
              <span className="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden>
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M14 2v6h6" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </span>
              Berichte
            </NavLink>
          </li>
          <li>
            <NavLink to="/settings" className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
              <span className="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden>
                  <path d="M12 15.5a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7z" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06A2 2 0 1 1 2.28 16.2l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09c.66 0 1.24-.4 1.51-1a1.65 1.65 0 0 0-.33-1.82l-.06-.06A2 2 0 1 1 7.12 2.28l.06.06c.5.5 1.2.68 1.82.33.6-.33 1.28-.33 1.82 0 .62.35 1.32.17 1.82-.33l.06-.06A2 2 0 1 1 16.88 4.88l-.06.06c-.5.5-.68 1.2-.33 1.82.33.6.33 1.28 0 1.82-.35.62-.17 1.32.33 1.82l.06.06a2 2 0 0 1 .33 2.83z" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </span>
              Einstellungen
            </NavLink>
          </li>
        </ul>
      </nav>

      {/* backdrop for small screens */}
      <div className="sidebar-backdrop" style={{ display: open ? "block" : "none" }} onClick={close} />
    </aside>
  );
};

export default Sidebar;
