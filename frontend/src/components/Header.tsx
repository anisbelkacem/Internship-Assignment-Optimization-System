import type { FC } from "react";
import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import internshipAssignmentService from "../services/internshipAssignmentService";
import plService from "../services/plService";
import studentConfigService from "../services/studentConfigService";

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
  const [notificationOpen, setNotificationOpen] = useState(false);
  const userChipRef = useRef<HTMLDivElement | null>(null);
  const notificationRef = useRef<HTMLDivElement | null>(null);

  // Notification data
  const [unassignedCount, setUnassignedCount] = useState(0);
  const [zoneViolationsCount, setZoneViolationsCount] = useState(0);
  const [overbookedCount, setOverbookedCount] = useState(0);
  const [forbiddenCombinationsCount, setForbiddenCombinationsCount] = useState(0);

  useEffect(() => {
    const onDocClick = (e: MouseEvent) => {
      if (userChipRef.current && !userChipRef.current.contains(e.target as Node)) {
        setMenuOpen(false);
      }
      if (notificationRef.current && !notificationRef.current.contains(e.target as Node)) {
        setNotificationOpen(false);
      }
    };
    document.addEventListener('mousedown', onDocClick);
    return () => document.removeEventListener('mousedown', onDocClick);
  }, []);

  // Fetch current year and violations
  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        const years = await studentConfigService.getAllYears();
        if (years.length > 0) {
          const latestYear = years.sort((a, b) => parseInt(b) - parseInt(a))[0];
          await fetchViolations(latestYear);
        }
      } catch (err) {
        console.error('Failed to load notifications:', err);
      }
    };

    fetchNotifications();
    const interval = setInterval(fetchNotifications, 120000);
    return () => clearInterval(interval);
  }, []);

  const fetchViolations = async (year: string) => {
    try {
      const [plannedInternships, teachers] = await Promise.all([
        internshipAssignmentService.getPlannedInternships(year),
        plService.getAllPls()
      ]);

      // Unassigned PLs
      const assignedTeacherIds = new Set<string>();
      plannedInternships.forEach(internship => {
        if (internship.teacherId) {
          assignedTeacherIds.add(internship.teacherId);
        }
      });
      const activeTeachers = teachers.filter(t => t.active);
      const unassigned = activeTeachers.filter(t => !assignedTeacherIds.has(t.teacherId.toString()));
      setUnassignedCount(unassigned.length);

      // Overbooked teachers
      const teacherInternshipMap = new Map<string, number>();
      plannedInternships.forEach(internship => {
        if (internship.teacherId) {
          teacherInternshipMap.set(
            internship.teacherId,
            (teacherInternshipMap.get(internship.teacherId) || 0) + 1
          );
        }
      });
      const overbooked = Array.from(teacherInternshipMap.values()).filter(count => count > 2).length;
      setOverbookedCount(overbooked);

      // Forbidden combinations
      const VALID_COMBINATIONS = [
        ['PDP_I', 'PDP_II'],
        ['PDP_I', 'SFP'],
        ['PDP_I', 'ZSP'],
        ['PDP_II', 'SFP'],
        ['PDP_II', 'ZSP'],
        ['SFP', 'ZSP']
      ];

      const teacherPraktikumsMap = new Map<string, Set<string>>();
      plannedInternships.forEach(internship => {
        if (internship.teacherId) {
          if (!teacherPraktikumsMap.has(internship.teacherId)) {
            teacherPraktikumsMap.set(internship.teacherId, new Set());
          }
          teacherPraktikumsMap.get(internship.teacherId)!.add(internship.praktikumType);
        }
      });

      let forbiddenCount = 0;
      teacherPraktikumsMap.forEach((praktikums) => {
        if (praktikums.size === 2) {
          const combo = Array.from(praktikums).sort();
          const isValid = VALID_COMBINATIONS.some(validCombo => 
            validCombo.length === combo.length && validCombo.every((val, idx) => val === combo[idx])
          );
          if (!isValid) {
            forbiddenCount++;
          }
        }
      });
      setForbiddenCombinationsCount(forbiddenCount);

      // Zone violations - Wednesday praktika (SFP, ZSP) should not be in Zone 3
      const WEDNESDAY_PRAKTIKA = ['SFP', 'ZSP'];
      const FORBIDDEN_ZONES_FOR_WEDNESDAY = ['Zone 3', '3'];
      
      let zoneViolationCount = 0;
      plannedInternships.forEach(internship => {
        if (internship.schoolZone && WEDNESDAY_PRAKTIKA.includes(internship.praktikumType)) {
          if (FORBIDDEN_ZONES_FOR_WEDNESDAY.some(forbiddenZone => internship.schoolZone?.includes(forbiddenZone))) {
            zoneViolationCount++;
          }
        }
      });
      setZoneViolationsCount(zoneViolationCount);

      console.log('Notification counts:', {
        unassigned: unassigned.length,
        overbooked,
        forbiddenCombinations: forbiddenCount,
        zoneViolations: zoneViolationCount,
        total: unassigned.length + overbooked + forbiddenCount + zoneViolationCount
      });

    } catch (err) {
      console.error('Failed to fetch violations:', err);
    }
  };

  // Count number of notification categories with issues (not total violations)
  const notificationCategories = [
    unassignedCount > 0,
    overbookedCount > 0,
    forbiddenCombinationsCount > 0,
    zoneViolationsCount > 0
  ].filter(Boolean).length;

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

          <div className="topbar-right" style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            {/* Notification Bell */}
            <div ref={notificationRef} style={{ position: 'relative' }}>
              <button
                onClick={() => setNotificationOpen(v => !v)}
                style={{
                  background: 'transparent',
                  border: 'none',
                  cursor: 'pointer',
                  position: 'relative',
                  padding: '8px',
                  borderRadius: '50%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  transition: 'background-color 0.2s',
                  outline: 'none'
                }}
                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f3f4f6'}
                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                aria-label="Notifications"
              >
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z" fill="#6b7280"/>
                </svg>
                {notificationCategories > 0 && (
                  <span style={{
                    position: 'absolute',
                    top: '4px',
                    right: '4px',
                    backgroundColor: '#ef4444',
                    color: 'white',
                    borderRadius: '10px',
                    padding: '2px 6px',
                    fontSize: '11px',
                    fontWeight: '600',
                    minWidth: '18px',
                    textAlign: 'center'
                  }}>
                    {notificationCategories}
                  </span>
                )}
              </button>

              {notificationOpen && (
                <div style={{
                  position: 'absolute',
                  top: 'calc(100% + 8px)',
                  right: '0',
                  backgroundColor: 'white',
                  borderRadius: '8px',
                  boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
                  minWidth: '320px',
                  zIndex: 1000,
                  border: '1px solid #e5e7eb'
                }}>
                  <div style={{
                    padding: '12px 16px',
                    borderBottom: '1px solid #e5e7eb',
                    fontWeight: '600',
                    fontSize: '14px',
                    color: '#111827'
                  }}>
                    Benachrichtigungen
                  </div>
                  
                  {notificationCategories === 0 ? (
                    <div style={{
                      padding: '24px 16px',
                      textAlign: 'center',
                      color: '#6b7280',
                      fontSize: '14px'
                    }}>
                      ✅ Keine offenen Probleme
                    </div>
                  ) : (
                    <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
                      {unassignedCount > 0 && (
                        <button
                          onClick={() => {
                            setNotificationOpen(false);
                            navigate('/dashboard');
                            setTimeout(() => {
                              document.getElementById('action-items')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
                            }, 100);
                          }}
                          style={{
                            width: '100%',
                            padding: '12px 16px',
                            textAlign: 'left',
                            background: 'transparent',
                            border: 'none',
                            borderBottom: '1px solid #f3f4f6',
                            cursor: 'pointer',
                            transition: 'background-color 0.2s'
                          }}
                          onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f9fafb'}
                          onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                        >
                          <div style={{ display: 'flex', alignItems: 'flex-start', gap: '12px' }}>
                            <span style={{ fontSize: '20px', marginTop: '2px' }}>⚠️</span>
                            <div style={{ flex: 1 }}>
                              <div style={{ fontWeight: '600', fontSize: '13px', color: '#111827', marginBottom: '2px' }}>
                                Nicht zugewiesen
                              </div>
                              <div style={{ fontSize: '12px', color: '#6b7280' }}>
                                {unassignedCount} {unassignedCount === 1 ? 'PL braucht' : 'PLs brauchen'} noch Zuweisung
                              </div>
                            </div>
                            <span style={{
                              backgroundColor: '#fef3c7',
                              color: '#92400e',
                              padding: '2px 8px',
                              borderRadius: '12px',
                              fontSize: '12px',
                              fontWeight: '600'
                            }}>
                              {unassignedCount}
                            </span>
                          </div>
                        </button>
                      )}

                      {overbookedCount > 0 && (
                        <button
                          onClick={() => {
                            setNotificationOpen(false);
                            navigate('/dashboard');
                            setTimeout(() => {
                              document.getElementById('action-items')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
                            }, 100);
                          }}
                          style={{
                            width: '100%',
                            padding: '12px 16px',
                            textAlign: 'left',
                            background: 'transparent',
                            border: 'none',
                            borderBottom: '1px solid #f3f4f6',
                            cursor: 'pointer',
                            transition: 'background-color 0.2s'
                          }}
                          onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f9fafb'}
                          onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                        >
                          <div style={{ display: 'flex', alignItems: 'flex-start', gap: '12px' }}>
                            <span style={{ fontSize: '20px', marginTop: '2px' }}>⚡</span>
                            <div style={{ flex: 1 }}>
                              <div style={{ fontWeight: '600', fontSize: '13px', color: '#111827', marginBottom: '2px' }}>
                                Überbucht
                              </div>
                              <div style={{ fontSize: '12px', color: '#6b7280' }}>
                                {overbookedCount} {overbookedCount === 1 ? 'PL ist' : 'PLs sind'} überbucht
                              </div>
                            </div>
                            <span style={{
                              backgroundColor: '#fee2e2',
                              color: '#991b1b',
                              padding: '2px 8px',
                              borderRadius: '12px',
                              fontSize: '12px',
                              fontWeight: '600'
                            }}>
                              {overbookedCount}
                            </span>
                          </div>
                        </button>
                      )}

                      {forbiddenCombinationsCount > 0 && (
                        <button
                          onClick={() => {
                            setNotificationOpen(false);
                            navigate('/dashboard');
                            setTimeout(() => {
                              document.getElementById('constraint-violations')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
                            }, 100);
                          }}
                          style={{
                            width: '100%',
                            padding: '12px 16px',
                            textAlign: 'left',
                            background: 'transparent',
                            border: 'none',
                            borderBottom: '1px solid #f3f4f6',
                            cursor: 'pointer',
                            transition: 'background-color 0.2s'
                          }}
                          onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f9fafb'}
                          onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                        >
                          <div style={{ display: 'flex', alignItems: 'flex-start', gap: '12px' }}>
                            <span style={{ fontSize: '20px', marginTop: '2px' }}>❌</span>
                            <div style={{ flex: 1 }}>
                              <div style={{ fontWeight: '600', fontSize: '13px', color: '#111827', marginBottom: '2px' }}>
                                Ungültige Kombinationen
                              </div>
                              <div style={{ fontSize: '12px', color: '#6b7280' }}>
                                {forbiddenCombinationsCount} {forbiddenCombinationsCount === 1 ? 'PL hat' : 'PLs haben'} ungültige Kombinationen
                              </div>
                            </div>
                            <span style={{
                              backgroundColor: '#fee2e2',
                              color: '#991b1b',
                              padding: '2px 8px',
                              borderRadius: '12px',
                              fontSize: '12px',
                              fontWeight: '600'
                            }}>
                              {forbiddenCombinationsCount}
                            </span>
                          </div>
                        </button>
                      )}

                      {zoneViolationsCount > 0 && (
                        <button
                          onClick={() => {
                            setNotificationOpen(false);
                            navigate('/dashboard');
                            setTimeout(() => {
                              document.getElementById('constraint-violations')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
                            }, 100);
                          }}
                          style={{
                            width: '100%',
                            padding: '12px 16px',
                            textAlign: 'left',
                            background: 'transparent',
                            border: 'none',
                            cursor: 'pointer',
                            transition: 'background-color 0.2s'
                          }}
                          onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f9fafb'}
                          onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                        >
                          <div style={{ display: 'flex', alignItems: 'flex-start', gap: '12px' }}>
                            <span style={{ fontSize: '20px', marginTop: '2px' }}>🚫</span>
                            <div style={{ flex: 1 }}>
                              <div style={{ fontWeight: '600', fontSize: '13px', color: '#111827', marginBottom: '2px' }}>
                                Zonen-Verstöße
                              </div>
                              <div style={{ fontSize: '12px', color: '#6b7280' }}>
                                {zoneViolationsCount} {zoneViolationsCount === 1 ? 'Praktikum' : 'Praktika'} in falscher Zone
                              </div>
                            </div>
                            <span style={{
                              backgroundColor: '#fee2e2',
                              color: '#991b1b',
                              padding: '2px 8px',
                              borderRadius: '12px',
                              fontSize: '12px',
                              fontWeight: '600'
                            }}>
                              {zoneViolationsCount}
                            </span>
                          </div>
                        </button>
                      )}
                    </div>
                  )}
                </div>
              )}
            </div>

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

                  <button className="user-menu__link" role="menuitem" onClick={() => { navigate('/settings'); setMenuOpen(false); }} style={{ outline: 'none' }}>View Account</button>

                  <button className="user-menu__item" onClick={handleLogout} role="menuitem" style={{ outline: 'none' }}>Logout</button>
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