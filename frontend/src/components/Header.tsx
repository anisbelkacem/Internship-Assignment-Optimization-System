import type { FC } from "react";
import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

const Header: FC = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const fullName = [user?.firstName, user?.lastName].filter(Boolean).join(' ');
  const welcomeText = `Welcome Back${fullName ? ", " + fullName : ""} 👋🏻`;

  const [menuOpen, setMenuOpen] = useState(false);
  const userChipRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const onDocClick = (e: MouseEvent) => {
      if (userChipRef.current && !userChipRef.current.contains(e.target as Node)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', onDocClick);
    return () => document.removeEventListener('mousedown', onDocClick);
  }, []);

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
            <div className="page-title">{welcomeText}</div>
          </div>

          <div className="topbar-right">
            <div className="user-chip" ref={userChipRef}>
              <div className="user-chip__avatar">
                <div className="avatar">
                  {user?.profileImage ? (
                    <img src={user.profileImage} alt={fullName} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  ) : (
                    <span>{user?.firstName?.[0]?.toUpperCase()}{user?.lastName?.[0]?.toUpperCase()}</span>
                  )}
                </div>
                {fullName ? <span className="user-name">{fullName}</span> : null}
              </div>
              <button className="user-chip__dropdown" onClick={() => setMenuOpen(v => !v)} aria-haspopup="menu" aria-expanded={menuOpen}>
                <svg className="caret" viewBox="0 0 24 24" width="16" height="16" fill="#424242" xmlns="http://www.w3.org/2000/svg" aria-hidden>
                  <path d="M7 10l5 5 5-5z"/>
                </svg>
              </button>
              {menuOpen && (
                <div className="user-menu" role="menu">
                  <div className="user-menu__header">
                    <div className="org-name">Passau University</div>
                  </div>
                  
                  <div className="user-menu__profile">
                    <div className="user-menu__avatar">
                      {user?.profileImage ? (
                        <img src={user.profileImage} alt={fullName} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                      ) : (
                        <span>{user?.firstName?.[0]?.toUpperCase()}{user?.lastName?.[0]?.toUpperCase()}</span>
                      )}
                    </div>
                    <div className="user-menu__info">
                      <div className="user-menu__name">{fullName}</div>
                      <div className="user-menu__email">{user?.email || 'user@example.com'}</div>
                    </div>
                  </div>

                  <button className="user-menu__link" role="menuitem" onClick={() => { navigate('/settings'); setMenuOpen(false); }}>View Account</button>

                  <button className="user-menu__item" onClick={handleLogout} role="menuitem">Logout</button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </header>
  );
};

export default Header;