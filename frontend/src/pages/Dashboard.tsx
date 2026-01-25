import React, { useState, useEffect } from "react";
import RightPanel from "../components/RightPanel";
import studentConfigService from "../services/studentConfigService";
import plService from "../services/plService";
import schoolService, { SchoolType } from "../services/schoolService";
import internshipAssignmentService from "../services/internshipAssignmentService";
import type { TeacherDto } from "../services/plService";
import type { School } from "../services/schoolService";
import "../styles/global.css";

const Dashboard: React.FC = () => {
  const [showOverbookedModal, setShowOverbookedModal] = useState(false);
  const [showNewYearModal, setShowNewYearModal] = useState(false);
  const [newYearInput, setNewYearInput] = useState('');
  const [selectedYear, setSelectedYear] = useState<string>("");
  const [availableYears, setAvailableYears] = useState<string[]>([]);
  const [totalRegelpraktikum, setTotalRegelpraktikum] = useState(0);
  const [gsPlCount, setGsPlCount] = useState(0);
  const [msPlCount, setMsPlCount] = useState(0);
  const [capacityData, setCapacityData] = useState<{
    PDP_I: { assigned: number; total: number };
    PDP_II: { assigned: number; total: number };
    SFP: { assigned: number; total: number };
    ZSP: { assigned: number; total: number };
  }>({
    PDP_I: { assigned: 0, total: 0 },
    PDP_II: { assigned: 0, total: 0 },
    SFP: { assigned: 0, total: 0 },
    ZSP: { assigned: 0, total: 0 },
  });
  const [zoneStats, setZoneStats] = useState<Map<string, number>>(new Map());
  const [unassignedCount, setUnassignedCount] = useState(0);
  const [loadingActionItems, setLoadingActionItems] = useState(false);
  const [overbookedCount, setOverbookedCount] = useState(0);
  const [overbookedTeachers, setOverbookedTeachers] = useState<Array<{
    teacherId: string;
    teacherName: string;
    internshipCount: number;
    internships: Array<{ praktikumType: string; schoolName: string | null }>;
  }>>([]);
  const [assignedStudents, setAssignedStudents] = useState(0);
  const [totalStudents, setTotalStudents] = useState(0);

  useEffect(() => {
    fetchAvailableYears();
    fetchTeacherStats();
    fetchZoneStats();
  }, []);

  useEffect(() => {
    if (selectedYear) {
      fetchStudentConfigStats();
      fetchCapacityStats();
      fetchActionItems();
    }
  }, [selectedYear]);

  const fetchAvailableYears = async () => {
    try {
      const yearsData = await studentConfigService.getAllYears();
      setAvailableYears(yearsData);
      if (yearsData.length > 0) {
        setSelectedYear(yearsData[0]);
      }
    } catch (err) {
      console.error('Failed to load years:', err);
    }
  };

  const fetchTeacherStats = async () => {
    try {
      const [teachers, schools]: [TeacherDto[], School[]] = await Promise.all([
        plService.getAllPls(),
        schoolService.getAllSchools()
      ]);
      
      // Create a map of schoolId to school type
      const schoolTypeMap = new Map<number, SchoolType>();
      schools.forEach(school => {
        schoolTypeMap.set(school.id, school.type);
      });
      
      // Count GS-PLs (teachers whose school type is GS)
      const gsTeachers = teachers.filter(t => 
        t.active && t.schoolId && schoolTypeMap.get(t.schoolId) === SchoolType.GS
      );
      setGsPlCount(gsTeachers.length);
      
      // Count MS-PLs (teachers whose school type is MS)
      const msTeachers = teachers.filter(t => 
        t.active && t.schoolId && schoolTypeMap.get(t.schoolId) === SchoolType.MS
      );
      setMsPlCount(msTeachers.length);
    } catch (err) {
      console.error('Failed to load teacher stats:', err);
    }
  };

  const fetchZoneStats = async () => {
    try {
      const [teachers, schools]: [TeacherDto[], School[]] = await Promise.all([
        plService.getAllPls(),
        schoolService.getAllSchools()
      ]);
      
      // Create a map of schoolId to zone
      const schoolZoneMap = new Map<number, string>();
      schools.forEach(school => {
        if (school.zone) {
          schoolZoneMap.set(school.id, school.zone);
        }
      });
      
      // Count PLs per zone
      const zoneCounts = new Map<string, number>();
      teachers.forEach(teacher => {
        if (teacher.active && teacher.schoolId) {
          const zone = schoolZoneMap.get(teacher.schoolId);
          if (zone) {
            zoneCounts.set(zone, (zoneCounts.get(zone) || 0) + 1);
          }
        }
      });
      
      setZoneStats(zoneCounts);
    } catch (err) {
      console.error('Failed to load zone stats:', err);
    }
  };

  const fetchStudentConfigStats = async () => {
    try {
      const configs = await studentConfigService.getConfigsByYear(selectedYear);
      // Count unique students (not total configs, since one student can have multiple configs)
      const uniqueStudentIds = new Set(configs.map(config => config.studentId));
      setTotalRegelpraktikum(uniqueStudentIds.size);
    } catch (err) {
      console.error('Failed to load student config stats:', err);
    }
  };

  const fetchCapacityStats = async () => {
    try {
      // Fetch all planned internships for the selected year
      const plannedInternships = await internshipAssignmentService.getPlannedInternships(selectedYear);
      
      // Fetch all active teachers to calculate total capacity
      const teachers = await plService.getAllPls();
      const totalActivePls = teachers.filter(t => t.active).length;
      
      // Group by praktikum type and count unique teachers assigned
      const capacityByType = {
        PDP_I: new Set<string>(),
        PDP_II: new Set<string>(),
        SFP: new Set<string>(),
        ZSP: new Set<string>(),
      };
      
      plannedInternships.forEach(internship => {
        if (internship.teacherId && internship.praktikumType in capacityByType) {
          capacityByType[internship.praktikumType as keyof typeof capacityByType].add(internship.teacherId);
        }
      });
      
      setCapacityData({
        PDP_I: { assigned: capacityByType.PDP_I.size, total: totalActivePls },
        PDP_II: { assigned: capacityByType.PDP_II.size, total: totalActivePls },
        SFP: { assigned: capacityByType.SFP.size, total: totalActivePls },
        ZSP: { assigned: capacityByType.ZSP.size, total: totalActivePls },
      });
    } catch (err) {
      console.error('Failed to load capacity stats:', err);
    }
  };

  const fetchActionItems = async () => {
    setLoadingActionItems(true);
    try {
      // Fetch all planned internships for the selected year and all active teachers
      const [plannedInternships, teachers] = await Promise.all([
        internshipAssignmentService.getPlannedInternships(selectedYear),
        plService.getAllPls()
      ]);
      
      // Get unique teacher IDs who have at least one internship assigned
      const assignedTeacherIds = new Set<string>();
      plannedInternships.forEach(internship => {
        if (internship.teacherId) {
          assignedTeacherIds.add(internship.teacherId);
        }
      });
      
      // Count active teachers who are NOT in the assigned set
      const activeTeachers = teachers.filter(t => t.active);
      const unassignedTeachers = activeTeachers.filter(t => !assignedTeacherIds.has(t.teacherId.toString()));
      setUnassignedCount(unassignedTeachers.length);

      // Calculate overbooked teachers (more than 2 internships)
      const teacherInternshipMap = new Map<string, Array<{ praktikumType: string; schoolName: string | null }>>();
      
      plannedInternships.forEach(internship => {
        if (internship.teacherId) {
          if (!teacherInternshipMap.has(internship.teacherId)) {
            teacherInternshipMap.set(internship.teacherId, []);
          }
          teacherInternshipMap.get(internship.teacherId)!.push({
            praktikumType: internship.praktikumType,
            schoolName: internship.schoolName
          });
        }
      });

      const overbooked = [];
      for (const [teacherId, internships] of teacherInternshipMap.entries()) {
        if (internships.length > 2) {
          const teacher = teachers.find(t => t.teacherId.toString() === teacherId);
          if (teacher) {
            overbooked.push({
              teacherId,
              teacherName: `${teacher.firstName} ${teacher.lastName}`,
              internshipCount: internships.length,
              internships
            });
          }
        }
      }

      setOverbookedTeachers(overbooked);
      setOverbookedCount(overbooked.length);

      // Calculate student assignment status
      const [studentAssignments, studentConfigs] = await Promise.all([
        internshipAssignmentService.getStudentAssignments(selectedYear),
        studentConfigService.getConfigsByYear(selectedYear)
      ]);
      
      // Get unique student names who have ASSIGNED status
      const assignedStudentNames = new Set<string>();
      studentAssignments.forEach(assignment => {
        if (assignment.status === 'ASSIGNED') {
          assignedStudentNames.add(assignment.studentName);
        }
      });
      
      // Total unique students from student configs
      const uniqueStudentIds = new Set(studentConfigs.map(config => config.studentId));
      
      setAssignedStudents(assignedStudentNames.size);
      setTotalStudents(uniqueStudentIds.size);
    } catch (err) {
      console.error('Failed to load action items:', err);
    } finally {
      setLoadingActionItems(false);
    }
  };

  const handleYearChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedYear(e.target.value);
  };

  const handleCreateNewYear = async () => {
    // Refresh the years list to show any configs added manually
    await fetchAvailableYears();
    setShowNewYearModal(true);
  };

  const handleConfirmNewYear = () => {
    if (!newYearInput.trim()) {
      alert('Please enter a valid year');
      return;
    }

    if (availableYears.includes(newYearInput.trim())) {
      alert('This year already exists');
      return;
    }

    // Add the new year to the list and select it
    const updatedYears = [...availableYears, newYearInput.trim()].sort();
    setAvailableYears(updatedYears);
    setSelectedYear(newYearInput.trim());
    setShowNewYearModal(false);
    setNewYearInput('');
    alert(`New planning year "${newYearInput.trim()}" created successfully!`);
  };

  return (
    <div className="dashboard-root">
      {/* Current Year Status */}
      <div className="dashboard-top">
        <div className="semester-header">
          <h1>{selectedYear ? `${selectedYear} — Mission Control` : 'Mission Control'}</h1>
          <div className="semester-menu-container" style={{display: 'flex', alignItems: 'center', gap: '10px'}}>
            <select
              className="form-select-small year-selector"
              value={selectedYear}
              onChange={handleYearChange}
              style={{width: '150px'}}
            >
              <option value="">Select Year</option>
              {availableYears.map((year) => (
                <option key={year} value={year}>
                  {year}
                </option>
              ))}
            </select>
            <button 
              className="btn btn-primary"
              onClick={handleCreateNewYear}
              style={{whiteSpace: 'nowrap'}}
              onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
              onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
            >
              New Year
            </button>
          </div>
        </div>
        <div className="card-row">
          <div className="status-card info">
            <div className="card-content">
              <p>Studierende</p>
              <h3>{totalRegelpraktikum}</h3>
              <p className="card-subtext">{selectedYear ? `für ${selectedYear}` : 'pro Schuljahr'}</p>
            </div>
          </div>
          <div className="status-card success">
            <div className="card-content">
              <p>GS-PLs</p>
              <h3>{gsPlCount}</h3>
              <p className="card-subtext">Aktive Betreuer</p>
            </div>
          </div>
          <div className="status-card success">
            <div className="card-content">
              <p>MS-PLs</p>
              <h3>{msPlCount}</h3>
              <p className="card-subtext">Aktive Betreuer</p>
            </div>
          </div>
          <div className="status-card primary">
            <div className="card-content">
              <p>Total PLs</p>
              <h3>{gsPlCount + msPlCount}</h3>
              <p className="card-subtext">GS + MS Betreuer</p>
            </div>
          </div>
        </div>
      </div>

      <div className="dashboard-grid">
        <main className="main-column">
          {/* Action Items Section */}
          <section className="section-container">
            <div className="section-header">
              <h2>🎯 Aktionsbedarf</h2>
            </div>
            <div className="card-row">
              <div className="status-card urgent">
                <div className="card-header-row">
                  <span className="card-icon" style={{ fontSize: '22px' }}>⚠️</span>
                  <div className="card-content">
                    <h3 style={{ fontSize: '16px', marginBottom: '4px' }}>Nicht zugewiesen</h3>
                    <p style={{ fontSize: '13px' }}>
                      {loadingActionItems ? (
                        <span style={{ opacity: 0.5 }}>Laden...</span>
                      ) : (
                        <>{unassignedCount} {unassignedCount === 1 ? 'PL braucht' : 'PLs brauchen'} noch Zuweisung</>
                      )}
                    </p>
                  </div>
                </div>
                <button 
                  className="btn btn-primary btn-sm" 
                  onClick={() => window.location.href = '/assignments'}
                  disabled={loadingActionItems}
                  style={{ fontSize: '15px' }}
                >
                  Jetzt zuweisen
                </button>
              </div>

              <div className="status-card warning">
                <div className="card-header-row">
                  <span className="card-icon" style={{ fontSize: '22px' }}>⚡</span>
                  <div className="card-content">
                    <h3 style={{ fontSize: '16px', marginBottom: '4px' }}>Überbucht</h3>
                    <p style={{ fontSize: '13px' }}>
                      {loadingActionItems ? (
                        <span style={{ opacity: 0.5 }}>Laden...</span>
                      ) : (
                        <>{overbookedCount} {overbookedCount === 1 ? 'PL überbucht' : 'PLs überbucht'}</>
                      )}
                    </p>
                  </div>
                </div>
                <button 
                  className="btn btn-ghost btn-sm" 
                  onClick={() => setShowOverbookedModal(true)}
                  disabled={loadingActionItems || overbookedCount === 0}
                  style={{ fontSize: '15px' }}
                >
                  Anzeigen
                </button>
              </div>

              <div className="status-card info">
                <div className="card-header-row">
                  <span className="card-icon" style={{ fontSize: '22px' }}>👥</span>
                  <div className="card-content">
                    <h3 style={{ fontSize: '16px', marginBottom: '4px' }}>Studierende zugewiesen</h3>
                    <p style={{ fontSize: '13px' }}>
                      {loadingActionItems ? (
                        <span style={{ opacity: 0.5 }}>Laden...</span>
                      ) : (
                        <>{assignedStudents} von {totalStudents} zugewiesen</>
                      )}
                    </p>
                  </div>
                </div>
                <button 
                  className="btn btn-ghost btn-sm" 
                  onClick={() => window.location.href = '/assignments?tab=phase2'}
                  disabled={loadingActionItems}
                  style={{ fontSize: '15px' }}
                >
                  Details
                </button>
              </div>
            </div>
          </section>

          {/* Constraint Violations */}
          <section className="section-container">
            <div className="section-header">
              <h2>⛔ Constraint Violations</h2>
            </div>
            <div className="card-row">
              <div className="status-card warning">
                <div className="card-header-row">
                  <span className="card-icon" style={{ fontSize: '22px' }}>❌</span>
                  <div className="card-content">
                    <h3 style={{ fontSize: '16px', marginBottom: '4px' }}>Forbidden Combinations</h3>
                    <p style={{ fontSize: '13px' }}>2 invalid PL-praktika pairs</p>
                  </div>
                </div>
                <span className="badge" style={{ 
                  backgroundColor: '#fee2e2', 
                  color: '#991b1b',
                  padding: '4px 12px',
                  borderRadius: '12px',
                  fontSize: '12px',
                  fontWeight: '500'
                }}>Review needed</span>
              </div>

              <div className="status-card warning">
                <div className="card-header-row">
                  <span className="card-icon" style={{ fontSize: '22px' }}>🚫</span>
                  <div className="card-content">
                    <h3 style={{ fontSize: '16px', marginBottom: '4px' }}>Zone Violations</h3>
                    <p style={{ fontSize: '13px' }}>5 Wed-praktika in Zone 3</p>
                  </div>
                </div>
                <span className="badge" style={{ 
                  backgroundColor: '#fef3c7', 
                  color: '#92400e',
                  padding: '4px 12px',
                  borderRadius: '12px',
                  fontSize: '12px',
                  fontWeight: '500'
                }}>Reassign</span>
              </div>
            </div>
          </section>

          {/* Kapazitätsauslastung */}
          <section className="section-container">
            <div className="section-header">
              <h2>📊 Kapazitätsauslastung</h2>
            </div>
            <div className="capacity-bars">
              <div className="capacity-item">
                <div className="capacity-row">
                  <span className="capacity-label">PDP I (Herbst)</span>
                  <span className="capacity-meta">{capacityData.PDP_I.assigned} / {capacityData.PDP_I.total} PLs</span>
                </div>
                <div className="progress-bar">
                  <div 
                    className={`progress-fill ${capacityData.PDP_I.total > 0 && (capacityData.PDP_I.assigned / capacityData.PDP_I.total) > 0.8 ? 'warn' : 'ok'}`} 
                    style={{ width: capacityData.PDP_I.total > 0 ? `${(capacityData.PDP_I.assigned / capacityData.PDP_I.total) * 100}%` : '0%' }} 
                  />
                </div>
              </div>

              <div className="capacity-item">
                <div className="capacity-row">
                  <span className="capacity-label">PDP II (Frühjahr)</span>
                  <span className="capacity-meta">{capacityData.PDP_II.assigned} / {capacityData.PDP_II.total} PLs</span>
                </div>
                <div className="progress-bar">
                  <div 
                    className={`progress-fill ${capacityData.PDP_II.total > 0 && (capacityData.PDP_II.assigned / capacityData.PDP_II.total) > 0.8 ? 'warn' : 'ok'}`} 
                    style={{ width: capacityData.PDP_II.total > 0 ? `${(capacityData.PDP_II.assigned / capacityData.PDP_II.total) * 100}%` : '0%' }} 
                  />
                </div>
              </div>

              <div className="capacity-item">
                <div className="capacity-row">
                  <span className="capacity-label">SFP (SoSe)</span>
                  <span className="capacity-meta">{capacityData.SFP.assigned} / {capacityData.SFP.total} PLs</span>
                </div>
                <div className="progress-bar">
                  <div 
                    className={`progress-fill ${capacityData.SFP.total > 0 && (capacityData.SFP.assigned / capacityData.SFP.total) > 0.8 ? 'warn' : 'ok'}`} 
                    style={{ width: capacityData.SFP.total > 0 ? `${(capacityData.SFP.assigned / capacityData.SFP.total) * 100}%` : '0%' }} 
                  />
                </div>
              </div>

              <div className="capacity-item">
                <div className="capacity-row">
                  <span className="capacity-label">ZSP (WiSe)</span>
                  <span className="capacity-meta">{capacityData.ZSP.assigned} / {capacityData.ZSP.total} PLs</span>
                </div>
                <div className="progress-bar">
                  <div 
                    className={`progress-fill ${capacityData.ZSP.total > 0 && (capacityData.ZSP.assigned / capacityData.ZSP.total) > 0.8 ? 'warn' : 'ok'}`} 
                    style={{ width: capacityData.ZSP.total > 0 ? `${(capacityData.ZSP.assigned / capacityData.ZSP.total) * 100}%` : '0%' }} 
                  />
                </div>
              </div>
            </div>
          </section>

          {/* Zonen-Auslastung */}
          <section className="section-container">
            <div className="section-header">
              <h2>🗺️ Zonen-Auslastung</h2>
            </div>
            <div className="card-row">
              {Array.from(zoneStats.entries())
                .sort(([zoneA], [zoneB]) => zoneA.localeCompare(zoneB))
                .map(([zone, count]) => {
                  const totalPls = gsPlCount + msPlCount;
                  const percentage = totalPls > 0 ? (count / totalPls) * 100 : 0;
                  
                  return (
                    <div 
                      key={zone} 
                      className="status-card info"
                      style={{
                        padding: '12px 16px',
                        minWidth: '140px',
                      }}
                    >
                      <div className="card-content">
                        <h3 style={{ fontSize: '14px', margin: '0 0 8px 0', color: '#64748b' }}>
                          📍 {zone}
                        </h3>
                        <p style={{ 
                          fontSize: '20px', 
                          fontWeight: '600', 
                          margin: '0 0 8px 0',
                          color: '#547792'
                        }}>
                          {count} PLs
                        </p>
                        <div className="progress-bar small">
                          <div 
                            className="progress-fill"
                            style={{ 
                              width: `${Math.min(percentage, 100)}%`,
                              background: '#547792'
                            }} 
                          />
                        </div>
                      </div>
                    </div>
                  );
                })}
            </div>
          </section>
        </main>

        <aside className="right-column">
          <RightPanel />
        </aside>
      </div>

      {/* Overbooked Modal */}
      {showOverbookedModal && (
        <div className="modal-overlay" onClick={() => setShowOverbookedModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="student-form-container">
              <h3 className="student-form-title">Überbuchte Praktikumsleiter</h3>

              <div className="overbooked-list">
                {overbookedTeachers.length === 0 ? (
                  <div style={{ textAlign: 'center', padding: '20px', color: '#666' }}>
                    Keine überbuchten Praktikumsleiter gefunden
                  </div>
                ) : (
                  overbookedTeachers.map((teacher) => (
                    <div key={teacher.teacherId} className="overbooked-item">
                      <div className="item-header">
                        <h4>{teacher.teacherName}</h4>
                        <span className="badge danger">{teacher.internshipCount} Praktika</span>
                      </div>
                      <div className="item-detail">
                        <strong>Zugewiesene Praktika:</strong>
                        <ul style={{ marginTop: '8px', marginLeft: '20px' }}>
                          {teacher.internships.map((internship, idx) => (
                            <li key={idx}>
                              {internship.praktikumType} {internship.schoolName ? `• ${internship.schoolName}` : ''}
                            </li>
                          ))}
                        </ul>
                      </div>
                      <p className="item-detail" style={{ color: '#dc2626', marginTop: '8px' }}>
                        ⚠️ Empfohlene Kapazität: maximal 2 Praktika
                      </p>
                    </div>
                  ))
                )}
              </div>

              <div className="student-form-actions">
                <button 
                  className="btn btn-ghost" 
                  onClick={() => setShowOverbookedModal(false)}
                >
                  Schließen
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* New Year Modal */}
      {showNewYearModal && (
        <div className="modal-overlay">
          <div className="modal-content modal-small">
            <h2>Create New Planning Year</h2>
            <p>Enter the academic year (e.g., 2026)</p>
            <div style={{marginBottom: '20px'}}>
              <input
                type="text"
                className="student-config-input"
                placeholder="e.g., 2026"
                value={newYearInput}
                onChange={(e) => setNewYearInput(e.target.value)}
                onKeyPress={(e) => {
                  if (e.key === 'Enter') {
                    handleConfirmNewYear();
                  }
                }}
                autoFocus
                style={{width: '100%', padding: '10px', fontSize: '1rem'}}
              />
            </div>
            <div className="modal-actions">
              <button
                className="btn-secondary"
                onClick={() => {
                  setShowNewYearModal(false);
                  setNewYearInput('');
                }}
              >
                Cancel
              </button>
              <button
                className="btn-primary"
                onClick={handleConfirmNewYear}
              >
                Create Year
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard;
