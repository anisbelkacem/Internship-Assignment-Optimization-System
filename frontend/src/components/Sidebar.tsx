import type { FC } from "react";
import { useState } from "react";
import { NavLink, useLocation } from "react-router-dom";
import logo from "../assets/Uni.png";

const Sidebar: FC = () => {
  const [collapsed, setCollapsed] = useState(false);
  const [expandedSubmenu, setExpandedSubmenu] = useState<string | null>(null);
  const location = useLocation();

  const toggleSidebar = () => {
    setCollapsed(!collapsed);
    document.body.classList.toggle('sidebar-collapsed');
  };

  const toggleSubmenu = (menuId: string) => {
    setExpandedSubmenu(expandedSubmenu === menuId ? null : menuId);
  };

  const isAssignmentsActive = location.pathname === '/assignments';

  return (
    <>
      <button className="sidebar-expand-dots" onClick={toggleSidebar} aria-label="Sidebar anzeigen">
        <span>⋮</span>
      </button>
      <aside className={`app-sidebar ${collapsed ? 'collapsed' : ''}`}>
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
          <li className={`submenu-item ${isAssignmentsActive ? 'active' : ''}`}>
            <button 
              className="submenu-toggle"
              onClick={() => toggleSubmenu('assignments')}
              aria-expanded={expandedSubmenu === 'assignments'}
            >
              <span className="nav-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden>
                  <rect x="3" y="7" width="18" height="14" rx="2" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                  <path d="M16 3v4" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
              </span>
              Praktikumsplanung
              <span className={`chevron ${expandedSubmenu === 'assignments' ? 'expanded' : ''}`}>▼</span>
            </button>
            {expandedSubmenu === 'assignments' && (
              <ul className="submenu">
                <li>
                  <NavLink to="/assignments" className={({ isActive }) => (isActive ? "nav-link active" : "nav-link")}>
                    Zuweisungen
                  </NavLink>
                </li>
                <li>
                  <NavLink to="/assignments?tab=pl-config" className={location.pathname === '/assignments' && location.search === '?tab=pl-config' ? "nav-link active" : "nav-link"}>
                    PL-Config
                  </NavLink>
                </li>
                <li>
                  <NavLink to="/assignments?tab=student-config" className={location.pathname === '/assignments' && location.search === '?tab=student-config' ? "nav-link active" : "nav-link"}>
                    Studenten-Config
                  </NavLink>
                </li>
              </ul>
            )}
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
      <button className="sidebar-toggle-bottom" onClick={toggleSidebar} aria-label="Collapse sidebar">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="3" y="3" width="8" height="18" rx="1" stroke="currentColor" strokeWidth="1.5"/>
          <path d="M13 7h8M13 12h8M13 17h8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
        </svg>
      </button>
    </aside>
    </>
  );
};

export default Sidebar;
