import type { FC } from "react";
import { useMemo } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

const Header: FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { logout } = useAuth();

  const { title, subtitle } = useMemo(() => {
    const map: Record<string, { title: string; subtitle?: string }> = {
      "/": { title: "Übersicht", subtitle: "Planungsübersicht" },
      "/dashboard": { title: "Übersicht", subtitle: "Planungsübersicht" },
      "/students": { title: "Studierende" },
      "/pls": { title: "PLs (Betreuer)" },
      "/schools": { title: "Schulen" },
      "/assignments": { title: "Praktikumszuweisungen" },
      "/reports": { title: "Berichte" },
      "/settings": { title: "Einstellungen" },
    };
    return map[location.pathname] ?? { title: location.pathname.replace("/", "") || "" };
  }, [location.pathname]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="app-header topbar">
      <div className="app-header__content">
        <div className="app-header__inner topbar-inner">
          <button
            className="sidebar-toggle"
            onClick={() => window.dispatchEvent(new CustomEvent('toggleSidebar'))}
            aria-label="Toggle navigation">
            ☰
          </button>
          <div className="header-title">
            <div className="page-title">{title}</div>
            {subtitle ? <div className="page-subtitle">{subtitle}</div> : null}
          </div>

          <div className="topbar-right">
            <div className="search-box">
              <svg className="search-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden>
                <path d="M21 21l-4.35-4.35" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round" />
                <circle cx="11" cy="11" r="6" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
              <input className="topbar-search" placeholder="PL, Schule, Fach suchen..." />
            </div>

            <button className="btn btn-primary">Export PDF</button>

            <div className="avatar">
              <svg viewBox="0 0 24 24" width="32" height="32" fill="#424242" xmlns="http://www.w3.org/2000/svg" aria-hidden>
                <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4z" fill="#424242"/>
                <path d="M4 20c0-2.21 3.58-4 8-4s8 1.79 8 4v1H4v-1z" fill="#424242"/>
              </svg>
            </div>

            <div className="notif">
              <svg viewBox="0 0 24 24" width="36" height="36" fill="#424242" xmlns="http://www.w3.org/2000/svg" aria-hidden>
                <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z" fill="#424242"/>
              </svg>
            </div>

            <button 
              className="logout" 
              onClick={handleLogout}
              style={{ 
                cursor: 'pointer', 
                background: 'none', 
                border: 'none',
                padding: 0,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}
              aria-label="Abmelden"
            >
              <svg viewBox="0 0 24 24" width="28" height="28" fill="#424242" xmlns="http://www.w3.org/2000/svg">
                <path d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z" fill="#424242"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;