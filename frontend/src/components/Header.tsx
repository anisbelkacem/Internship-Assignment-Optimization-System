import type { FC } from "react";
import { useMemo } from "react";
import { useLocation } from "react-router-dom";

const Header: FC = () => {
  const location = useLocation();

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
            {/* simple inline SVG avatar as a placeholder person picture (person filled grey) */}
            <img
              src={`data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='120' height='120' viewBox='0 0 24 24'><rect width='24' height='24' rx='12' fill='%23cfd8dc'/><path d='M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4z' fill='%236b6b6b'/><path d='M4 20c0-2.21 3.58-4 8-4s8 1.79 8 4v1H4v-1z' fill='%236b6b6b'/></svg>`}
              alt="Konto"
            />
          </div>

          <div className="notif">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden>
              <path d="M15 17h5l-1.405-1.405A2.032 2.032 0 0 1 18.6 14.6V11c0-3.07-1.64-5.64-4.5-6.32V4a1.5 1.5 0 0 0-3 0v.68C7.64 5.36 6 7.92 6 11v3.6c0 .538-.214 1.055-.595 1.445L4 17h11z" stroke="currentColor" strokeWidth="0.6mm" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
            <span className="notif-dot" />
          </div>

          <div className="logout">Abmelden</div>
        </div>
  </div>
  </div>
    </header>
  );
};

export default Header;
