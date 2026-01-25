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
  const [showUnassignedModal, setShowUnassignedModal] = useState(false);
  const [unassignedStudents, setUnassignedStudents] = useState<Array<{
    studentName: string;
    assignments: Array<{ praktikumType: string; status: string }>;
  }>>([]);
  const [forbiddenCombinationsCount, setForbiddenCombinationsCount] = useState(0);
  const [showForbiddenModal, setShowForbiddenModal] = useState(false);
  const [teachersWithForbiddenCombinations, setTeachersWithForbiddenCombinations] = useState<Array<{
    teacherId: string;
    teacherName: string;
    combinations: string[];
  }>>([]);
  const [zoneViolationsCount, setZoneViolationsCount] = useState(0);
  const [showZoneViolationsModal, setShowZoneViolationsModal] = useState(false);
  const [zoneViolations, setZoneViolations] = useState<Array<{
    zone: string;
    count: number;
    internships: Array<{ praktikumType: string; schoolName: string; teacherName: string | null }>;
  }>>([]);
  const [selectedZoneForModal, setSelectedZoneForModal] = useState<string | null>(null);
  const [statusBreakdown, setStatusBreakdown] = useState<{
    proposed: number;
    confirmed: number;
    cancelled: number;
  }>({ proposed: 0, confirmed: 0, cancelled: 0 });
  const [praktikumDistribution, setPraktikumDistribution] = useState<{
    PDP_I: number;
    PDP_II: number;
    SFP: number;
    ZSP: number;
  }>({ PDP_I: 0, PDP_II: 0, SFP: 0, ZSP: 0 });

  // Define valid combinations - any other combination is forbidden
  // Each PL must supervise exactly 2 Praktika
  const VALID_COMBINATIONS = [
    ['PDP_I', 'PDP_II'],
    ['PDP_I', 'SFP'],
    ['PDP_I', 'ZSP'],
    ['PDP_II', 'SFP'],
    ['PDP_II', 'ZSP'],
    ['SFP', 'ZSP']
  ];

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
      const studentAssignments = await internshipAssignmentService.getStudentAssignments(selectedYear);
      
      // Group assignments by student and check if ALL their assignments are valid
      const assignedStudentNames = new Set<string>();
      const allStudentNames = new Set<string>();
      const studentAssignmentMap = new Map<string, Array<{ praktikumType: string; status: string }>>();
      
      studentAssignments.forEach(assignment => {
        allStudentNames.add(assignment.studentName);
        
        if (!studentAssignmentMap.has(assignment.studentName)) {
          studentAssignmentMap.set(assignment.studentName, []);
        }
        studentAssignmentMap.get(assignment.studentName)!.push({
          praktikumType: assignment.praktikumType,
          status: assignment.status
        });
      });
      
      // A student is "assigned" only if ALL their assignments are PROPOSED or CONFIRMED (none CANCELLED)
      for (const [studentName, assignments] of studentAssignmentMap.entries()) {
        const hasOnlyValidAssignments = assignments.every(
          a => a.status === 'PROPOSED' || a.status === 'CONFIRMED'
        );
        if (hasOnlyValidAssignments) {
          assignedStudentNames.add(studentName);
        }
      }
      
      // Find unassigned or partially assigned students
      const unassigned = [];
      for (const studentName of allStudentNames) {
        if (!assignedStudentNames.has(studentName)) {
          const assignments = studentAssignmentMap.get(studentName) || [];
          unassigned.push({
            studentName,
            assignments
          });
        }
      }
      
      setAssignedStudents(assignedStudentNames.size);
      setTotalStudents(allStudentNames.size);
      setUnassignedStudents(unassigned);

      // Calculate forbidden combinations
      const teachersWithViolations = [];
      for (const [teacherId, internships] of teacherInternshipMap.entries()) {
        // Get unique praktikum types for this teacher
        const types = [...new Set(internships.map(i => i.praktikumType))].sort();
        
        // If teacher has 2 or more types, check if combination is valid
        if (types.length >= 2) {
          const isValid = VALID_COMBINATIONS.some(validCombo => {
            const sortedValid = [...validCombo].sort();
            return types.length === sortedValid.length && 
                   types.every((type, idx) => type === sortedValid[idx]);
          });
          
          if (!isValid) {
            const teacher = teachers.find(t => t.teacherId.toString() === teacherId);
            if (teacher) {
              teachersWithViolations.push({
                teacherId,
                teacherName: `${teacher.firstName} ${teacher.lastName}`,
                combinations: types
              });
            }
          }
        }
      }
      
      setTeachersWithForbiddenCombinations(teachersWithViolations);
      setForbiddenCombinationsCount(teachersWithViolations.length);

      // Calculate zone violations - Wednesday praktika (SFP, ZSP) should be in Zone 1, not Zone 3
      const WEDNESDAY_PRAKTIKA = ['SFP', 'ZSP'];
      const FORBIDDEN_ZONES_FOR_WEDNESDAY = ['Zone 3', '3'];
      
      const zoneViolationsByZone = new Map<string, Array<{ praktikumType: string; schoolName: string; teacherName: string | null }>>();
      
      plannedInternships.forEach(internship => {
        if (internship.schoolZone && WEDNESDAY_PRAKTIKA.includes(internship.praktikumType)) {
          // Check if Wednesday praktikum is in forbidden zone
          if (FORBIDDEN_ZONES_FOR_WEDNESDAY.some(forbiddenZone => internship.schoolZone?.includes(forbiddenZone))) {
            if (!zoneViolationsByZone.has(internship.schoolZone)) {
              zoneViolationsByZone.set(internship.schoolZone, []);
            }
            zoneViolationsByZone.get(internship.schoolZone)!.push({
              praktikumType: internship.praktikumType,
              schoolName: internship.schoolName || 'Unknown School',
              teacherName: internship.teacherName
            });
          }
        }
      });
      
      const violations = [];
      for (const [zone, internships] of zoneViolationsByZone.entries()) {
        violations.push({
          zone,
          count: internships.length,
          internships
        });
      }
      
      setZoneViolations(violations);
      setZoneViolationsCount(violations.reduce((sum, v) => sum + v.count, 0));

      // Calculate status breakdown
      const statusCounts = { proposed: 0, confirmed: 0, cancelled: 0 };
      studentAssignments.forEach(assignment => {
        if (assignment.status === 'PROPOSED') statusCounts.proposed++;
        else if (assignment.status === 'CONFIRMED') statusCounts.confirmed++;
        else if (assignment.status === 'CANCELLED') statusCounts.cancelled++;
      });
      setStatusBreakdown(statusCounts);

      // Calculate praktikum type distribution (unique students per type)
      const praktikumCounts = { PDP_I: 0, PDP_II: 0, SFP: 0, ZSP: 0 };
      const studentsByType = {
        PDP_I: new Set<string>(),
        PDP_II: new Set<string>(),
        SFP: new Set<string>(),
        ZSP: new Set<string>()
      };
      studentAssignments.forEach(assignment => {
        if (assignment.praktikumType in studentsByType) {
          studentsByType[assignment.praktikumType as keyof typeof studentsByType].add(assignment.studentName);
        }
      });
      praktikumCounts.PDP_I = studentsByType.PDP_I.size;
      praktikumCounts.PDP_II = studentsByType.PDP_II.size;
      praktikumCounts.SFP = studentsByType.SFP.size;
      praktikumCounts.ZSP = studentsByType.ZSP.size;
      setPraktikumDistribution(praktikumCounts);
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
          <section className="section-container" id="action-items">
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
                  onClick={() => {
                    if (unassignedStudents.length > 0) {
                      setShowUnassignedModal(true);
                    } else {
                      window.location.href = '/assignments?tab=phase2';
                    }
                  }}
                  disabled={loadingActionItems}
                  style={{ fontSize: '15px' }}
                >
                  {unassignedStudents.length > 0 ? 'Nicht zugewiesen' : 'Details'}
                </button>
              </div>
            </div>
          </section>

          {/* Constraint Violations */}
          <section className="section-container" id="constraint-violations">
            <div className="section-header">
              <h2>⛔ Constraint Violations</h2>
            </div>
            <div className="card-row">
              <div className="status-card warning">
                <div className="card-header-row">
                  <span className="card-icon" style={{ fontSize: '22px' }}>❌</span>
                  <div className="card-content">
                    <h3 style={{ fontSize: '16px', marginBottom: '4px' }}>Forbidden Combinations</h3>
                    <p style={{ fontSize: '13px' }}>
                      {loadingActionItems ? (
                        <span style={{ opacity: 0.5 }}>Laden...</span>
                      ) : (
                        <>{forbiddenCombinationsCount} {forbiddenCombinationsCount === 1 ? 'PL mit ungültigen Kombinationen' : 'PLs mit ungültigen Kombinationen'}</>
                      )}
                    </p>
                  </div>
                </div>
                <button 
                  className="btn btn-ghost btn-sm" 
                  onClick={() => setShowForbiddenModal(true)}
                  disabled={loadingActionItems || forbiddenCombinationsCount === 0}
                  style={{ fontSize: '15px' }}
                >
                  Details
                </button>
              </div>

              <div className="status-card warning">
                <div className="card-header-row">
                  <span className="card-icon" style={{ fontSize: '22px' }}>🚫</span>
                  <div className="card-content">
                    <h3 style={{ fontSize: '16px', marginBottom: '4px' }}>Zone Violations</h3>
                    <p style={{ fontSize: '13px' }}>
                      {loadingActionItems ? (
                        <span style={{ opacity: 0.5 }}>Laden...</span>
                      ) : (
                        <>{zoneViolationsCount} {zoneViolationsCount === 1 ? 'Mittwochspraktikum in falscher Zone' : 'Mittwochspraktika in falscher Zone'}</>
                      )}
                    </p>
                  </div>
                </div>
                <button 
                  className="btn btn-ghost btn-sm" 
                  onClick={() => setShowZoneViolationsModal(true)}
                  disabled={loadingActionItems || zoneViolationsCount === 0}
                  style={{ fontSize: '15px' }}
                >
                  Details
                </button>
              </div>
            </div>
          </section>

          {/* Status Breakdown & Praktikum Distribution */}
          <section className="section-container">
            <div className="section-header">
              <h2>📈 Statistiken</h2>
            </div>
            <div className="card-row">
              {/* Status Breakdown Donut Chart */}
              <div className="status-card info" style={{ flex: 1, width: '280px', height: '280px', minWidth: '280px', maxWidth: '280px' }}>
                <div className="card-content" style={{ padding: '10px' }}>
                  <h3 style={{ fontSize: '14px', marginBottom: '10px', fontWeight: '600', color: '#111827' }}>
                    Zuweisungsstatus
                  </h3>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    {/* Donut Chart */}
                    <div style={{ position: 'relative', width: '85px', height: '85px', flexShrink: 0 }}>
                      {(() => {
                        const total = statusBreakdown.proposed + statusBreakdown.confirmed + statusBreakdown.cancelled;
                        if (total === 0) {
                          return (
                            <div style={{ 
                              width: '85px', 
                              height: '85px', 
                              borderRadius: '50%', 
                              background: '#e5e7eb',
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              fontSize: '10px',
                              color: '#6b7280',
                              textAlign: 'center',
                              padding: '8px'
                            }}>
                              Keine Daten
                            </div>
                          );
                        }
                        const proposedPercent = (statusBreakdown.proposed / total) * 100;
                        const confirmedPercent = (statusBreakdown.confirmed / total) * 100;
                        
                        // Create conic gradient
                        let gradientStops = [];
                        let currentPercent = 0;
                        
                        if (statusBreakdown.proposed > 0) {
                          gradientStops.push(`#a8c5d9 0% ${proposedPercent}%`);
                          currentPercent = proposedPercent;
                        }
                        if (statusBreakdown.confirmed > 0) {
                          gradientStops.push(`#547792 ${currentPercent}% ${currentPercent + confirmedPercent}%`);
                          currentPercent += confirmedPercent;
                        }
                        if (statusBreakdown.cancelled > 0) {
                          gradientStops.push(`#213448 ${currentPercent}% 100%`);
                        }
                        
                        return (
                          <div style={{ 
                            width: '85px', 
                            height: '85px', 
                            borderRadius: '50%', 
                            background: `conic-gradient(${gradientStops.join(', ')})`,
                            position: 'relative',
                            boxShadow: '0 1px 4px rgba(0,0,0,0.08)'
                          }}>
                            <div style={{
                              position: 'absolute',
                              top: '50%',
                              left: '50%',
                              transform: 'translate(-50%, -50%)',
                              width: '52px',
                              height: '52px',
                              borderRadius: '50%',
                              background: 'white',
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              flexDirection: 'column',
                              fontSize: '18px',
                              fontWeight: '700',
                              color: '#111827'
                            }}>
                              {total}
                              <span style={{ fontSize: '9px', fontWeight: '500', color: '#6b7280', marginTop: '0px' }}>
                                Total
                              </span>
                            </div>
                          </div>
                        );
                      })()}
                    </div>
                    
                    {/* Legend */}
                    <div style={{ flex: 1 }}>
                      <div style={{ marginBottom: '10px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                          <div style={{ width: '12px', height: '12px', borderRadius: '2px', background: '#a8c5d9' }}></div>
                          <span style={{ fontSize: '13px', color: '#4b5563', fontWeight: '500' }}>Vorgeschlagen</span>
                        </div>
                        <div style={{ fontSize: '18px', fontWeight: '700', color: '#111827', marginLeft: '20px' }}>
                          {statusBreakdown.proposed}
                        </div>
                      </div>
                      <div style={{ marginBottom: '10px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                          <div style={{ width: '12px', height: '12px', borderRadius: '2px', background: '#547792' }}></div>
                          <span style={{ fontSize: '13px', color: '#4b5563', fontWeight: '500' }}>Bestätigt</span>
                        </div>
                        <div style={{ fontSize: '18px', fontWeight: '700', color: '#111827', marginLeft: '20px' }}>
                          {statusBreakdown.confirmed}
                        </div>
                      </div>
                      <div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                          <div style={{ width: '12px', height: '12px', borderRadius: '2px', background: '#213448' }}></div>
                          <span style={{ fontSize: '13px', color: '#4b5563', fontWeight: '500' }}>Abgesagt</span>
                        </div>
                        <div style={{ fontSize: '18px', fontWeight: '700', color: '#111827', marginLeft: '20px' }}>
                          {statusBreakdown.cancelled}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Praktikum Distribution Bar Chart */}
              <div className="status-card info" style={{ flex: 1, width: '280px', height: '280px', minWidth: '280px', maxWidth: '280px' }}>
                <div className="card-content" style={{ padding: '10px' }}>
                  <h3 style={{ fontSize: '14px', marginBottom: '2px', fontWeight: '600', color: '#111827' }}>
                    👥 Studentenverteilung
                  </h3>
                  <div style={{ fontSize: '11px', color: '#6b7280', marginBottom: '12px' }}>
                    Studenten pro Praktikumsart
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    {(() => {
                      const maxValue = Math.max(
                        praktikumDistribution.PDP_I,
                        praktikumDistribution.PDP_II,
                        praktikumDistribution.SFP,
                        praktikumDistribution.ZSP,
                        1
                      );
                      
                      const praktikums = [
                        { name: 'PDP I', value: praktikumDistribution.PDP_I, color: '#a8c5d9' },
                        { name: 'PDP II', value: praktikumDistribution.PDP_II, color: '#7da3be' },
                        { name: 'SFP', value: praktikumDistribution.SFP, color: '#547792' },
                        { name: 'ZSP', value: praktikumDistribution.ZSP, color: '#213448' }
                      ];
                      
                      return praktikums.map(({ name, value, color }) => (
                        <div key={name}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '5px' }}>
                            <span style={{ fontSize: '13px', fontWeight: '600', color: '#4b5563' }}>
                              {name}
                            </span>
                            <span style={{ fontSize: '16px', fontWeight: '700', color: '#111827' }}>
                              {value}
                            </span>
                          </div>
                          <div className="progress-bar" style={{ height: '8px' }}>
                            <div 
                              className="progress-fill"
                              style={{ 
                                width: `${(value / maxValue) * 100}%`, 
                                background: color
                              }}
                            ></div>
                          </div>
                        </div>
                      ));
                    })()}
                  </div>
                </div>
              </div>

              {/* Kapazitätsauslastung Card */}
              <div className="status-card info" style={{ flex: 1, width: '280px', height: '280px', minWidth: '280px', maxWidth: '280px' }}>
                <div className="card-content" style={{ padding: '10px' }}>
                  <h3 style={{ fontSize: '14px', marginBottom: '2px', fontWeight: '600', color: '#111827' }}>
                    📊 Kapazitätsauslastung
                  </h3>
                  <div style={{ fontSize: '11px', color: '#6b7280', marginBottom: '12px' }}>
                    PL-Plätze: zugewiesen vs. verfügbar
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                    {/* PDP I */}
                    <div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '5px' }}>
                        <span style={{ fontSize: '13px', fontWeight: '600', color: '#4b5563' }}>
                          PDP I
                        </span>
                        <span style={{ fontSize: '13px', fontWeight: '600', color: '#111827' }}>
                          {capacityData.PDP_I.assigned} / {capacityData.PDP_I.total}
                        </span>
                      </div>
                      <div className="progress-bar" style={{ height: '8px' }}>
                        <div 
                          className="progress-fill"
                          style={{ 
                            width: capacityData.PDP_I.total > 0 ? `${(capacityData.PDP_I.assigned / capacityData.PDP_I.total) * 100}%` : '0%',
                            background: capacityData.PDP_I.total > 0 && (capacityData.PDP_I.assigned / capacityData.PDP_I.total) > 0.8 ? '#ef4444' : '#a8c5d9'
                          }}
                        ></div>
                      </div>
                    </div>

                    {/* PDP II */}
                    <div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '5px' }}>
                        <span style={{ fontSize: '13px', fontWeight: '600', color: '#4b5563' }}>
                          PDP II
                        </span>
                        <span style={{ fontSize: '13px', fontWeight: '600', color: '#111827' }}>
                          {capacityData.PDP_II.assigned} / {capacityData.PDP_II.total}
                        </span>
                      </div>
                      <div className="progress-bar" style={{ height: '8px' }}>
                        <div 
                          className="progress-fill"
                          style={{ 
                            width: capacityData.PDP_II.total > 0 ? `${(capacityData.PDP_II.assigned / capacityData.PDP_II.total) * 100}%` : '0%',
                            background: capacityData.PDP_II.total > 0 && (capacityData.PDP_II.assigned / capacityData.PDP_II.total) > 0.8 ? '#ef4444' : '#7da3be'
                          }}
                        ></div>
                      </div>
                    </div>

                    {/* SFP */}
                    <div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '5px' }}>
                        <span style={{ fontSize: '13px', fontWeight: '600', color: '#4b5563' }}>
                          SFP
                        </span>
                        <span style={{ fontSize: '13px', fontWeight: '600', color: '#111827' }}>
                          {capacityData.SFP.assigned} / {capacityData.SFP.total}
                        </span>
                      </div>
                      <div className="progress-bar" style={{ height: '8px' }}>
                        <div 
                          className="progress-fill"
                          style={{ 
                            width: capacityData.SFP.total > 0 ? `${(capacityData.SFP.assigned / capacityData.SFP.total) * 100}%` : '0%',
                            background: capacityData.SFP.total > 0 && (capacityData.SFP.assigned / capacityData.SFP.total) > 0.8 ? '#ef4444' : '#547792'
                          }}
                        ></div>
                      </div>
                    </div>

                    {/* ZSP */}
                    <div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '5px' }}>
                        <span style={{ fontSize: '13px', fontWeight: '600', color: '#4b5563' }}>
                          ZSP
                        </span>
                        <span style={{ fontSize: '13px', fontWeight: '600', color: '#111827' }}>
                          {capacityData.ZSP.assigned} / {capacityData.ZSP.total}
                        </span>
                      </div>
                      <div className="progress-bar" style={{ height: '8px' }}>
                        <div 
                          className="progress-fill"
                          style={{ 
                            width: capacityData.ZSP.total > 0 ? `${(capacityData.ZSP.assigned / capacityData.ZSP.total) * 100}%` : '0%',
                            background: capacityData.ZSP.total > 0 && (capacityData.ZSP.assigned / capacityData.ZSP.total) > 0.8 ? '#ef4444' : '#213448'
                          }}
                        ></div>
                      </div>
                    </div>
                  </div>
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
                  
                  // Check if this zone has violations
                  const hasViolation = zoneViolations.some(v => v.zone === zone);
                  const zoneViolationData = zoneViolations.find(v => v.zone === zone);
                  
                  return (
                    <div 
                      key={zone} 
                      className={`status-card ${hasViolation ? 'warning' : 'info'}`}
                      style={{
                        padding: '12px 16px',
                        minWidth: '140px',
                        position: 'relative',
                        cursor: hasViolation ? 'pointer' : 'default'
                      }}
                      onClick={() => {
                        if (hasViolation) {
                          setSelectedZoneForModal(zone);
                        }
                      }}
                    >
                      {hasViolation && (
                        <div style={{
                          position: 'absolute',
                          top: '8px',
                          right: '8px',
                          fontSize: '16px'
                        }}>
                          ⚠️
                        </div>
                      )}
                      <div className="card-content">
                        <h3 style={{ fontSize: '14px', margin: '0 0 8px 0', color: '#64748b' }}>
                          📍 {zone}
                        </h3>
                        <p style={{ 
                          fontSize: '20px', 
                          fontWeight: '600', 
                          margin: '0 0 8px 0',
                          color: hasViolation ? '#d97706' : '#547792'
                        }}>
                          {count} PLs
                        </p>
                        <div className="progress-bar small">
                          <div 
                            className="progress-fill"
                            style={{ 
                              width: `${Math.min(percentage, 100)}%`,
                              background: hasViolation ? '#f59e0b' : '#547792'
                            }} 
                          />
                        </div>
                        {hasViolation && (
                          <p style={{ 
                            fontSize: '11px', 
                            marginTop: '6px', 
                            color: '#d97706',
                            fontWeight: '500',
                            textDecoration: 'underline'
                          }}>
                            {zoneViolationData?.count} Verstoß{zoneViolationData && zoneViolationData.count > 1 ? 'e' : ''} - Details
                          </p>
                        )}
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

      {/* Forbidden Combinations Modal */}
      {showForbiddenModal && (
        <div className="modal-overlay" onClick={() => setShowForbiddenModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="student-form-container">
              <h3 className="student-form-title">PLs mit ungültigen Kombinationen</h3>

              <div style={{ marginBottom: '16px', padding: '12px', backgroundColor: '#fef3c7', borderRadius: '6px' }}>
                <p style={{ fontSize: '14px', margin: 0 }}>
                  <strong>Gültige Kombinationen (genau 2 Praktika):</strong><br/>
                  • PDP I + PDP II<br/>
                  • PDP I + SFP<br/>
                  • PDP I + ZSP<br/>
                  • PDP II + SFP<br/>
                  • PDP II + ZSP<br/>
                  • SFP + ZSP
                </p>
              </div>

              <div className="overbooked-list">
                {teachersWithForbiddenCombinations.length === 0 ? (
                  <div style={{ textAlign: 'center', padding: '20px', color: '#666' }}>
                    Keine ungültigen Kombinationen gefunden
                  </div>
                ) : (
                  teachersWithForbiddenCombinations.map((teacher) => (
                    <div key={teacher.teacherId} className="overbooked-item">
                      <div className="item-header">
                        <h4>{teacher.teacherName}</h4>
                        <span className="badge danger">Ungültige Kombination</span>
                      </div>
                      <div className="item-detail">
                        <strong>Zugewiesene Praktikumstypen:</strong>
                        <ul style={{ marginTop: '8px', marginLeft: '20px' }}>
                          {teacher.combinations.map((type, idx) => (
                            <li key={idx} style={{ color: '#dc2626', fontWeight: '500' }}>
                              {type}
                            </li>
                          ))}
                        </ul>
                      </div>
                      <p className="item-detail" style={{ color: '#dc2626', marginTop: '8px' }}>
                        ❌ Diese Kombination ist nicht erlaubt
                      </p>
                    </div>
                  ))
                )}
              </div>

              <div className="student-form-actions">
                <button 
                  className="btn btn-ghost" 
                  onClick={() => setShowForbiddenModal(false)}
                >
                  Schließen
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Zone Violations Modal */}
      {showZoneViolationsModal && (
        <div className="modal-overlay" onClick={() => setShowZoneViolationsModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="student-form-container">
              <h3 className="student-form-title">Zonen-Verstöße: Mittwochspraktika</h3>

              <div style={{ marginBottom: '16px', padding: '12px', backgroundColor: '#fef3c7', borderRadius: '6px' }}>
                <p style={{ fontSize: '14px', margin: 0 }}>
                  <strong>Regel:</strong><br/>
                  Mittwochspraktika (SFP und ZSP) sollten in <strong>Zone 1</strong> (Passau-nah) stattfinden, da Studierende nachmittags zur Universität zurückkehren müssen.<br/>
                  <strong>Zone 3 ist zu weit entfernt für Mittwochspraktika.</strong>
                </p>
              </div>

              <div className="overbooked-list">
                {zoneViolations.length === 0 ? (
                  <div style={{ textAlign: 'center', padding: '20px', color: '#666' }}>
                    Keine Zonen-Verstöße gefunden
                  </div>
                ) : (
                  zoneViolations.map((violation) => (
                    <div key={violation.zone} className="overbooked-item">
                      <div className="item-header">
                        <h4>{violation.zone}</h4>
                        <span className="badge danger">{violation.count} Verstöße</span>
                      </div>
                      <div className="item-detail">
                        <strong>Problematische Zuweisungen:</strong>
                        <ul style={{ marginTop: '8px', marginLeft: '20px' }}>
                          {violation.internships.map((internship, idx) => (
                            <li key={idx} style={{ color: '#dc2626' }}>
                              {internship.praktikumType} • {internship.schoolName}
                              {internship.teacherName && ` • ${internship.teacherName}`}
                            </li>
                          ))}
                        </ul>
                      </div>
                      <p className="item-detail" style={{ color: '#dc2626', marginTop: '8px' }}>
                        ⚠️ Diese Mittwochspraktika sollten in Zone 1 (Passau-nah) stattfinden
                      </p>
                    </div>
                  ))
                )}
              </div>

              <div className="student-form-actions">
                <button 
                  className="btn btn-ghost" 
                  onClick={() => setShowZoneViolationsModal(false)}
                >
                  Schließen
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Zone-Specific Violations Modal (from zone card click) */}
      {selectedZoneForModal && (
        <div className="modal-overlay" onClick={() => setSelectedZoneForModal(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '600px' }}>
            <button 
              className="modal-close" 
              onClick={() => setSelectedZoneForModal(null)}
              style={{
                position: 'absolute',
                top: '1rem',
                right: '1rem',
                background: 'none',
                border: 'none',
                fontSize: '1.5rem',
                cursor: 'pointer',
                color: '#6b7280',
                padding: '0',
                lineHeight: '1',
                width: '24px',
                height: '24px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                zIndex: 10
              }}
            >×</button>
            <div className="modal-header">
              <h2>📍 {selectedZoneForModal} - Zonen-Verstöße</h2>
            </div>
            <div className="modal-body" style={{ maxHeight: '500px', overflowY: 'auto' }}>
              {(() => {
                const violation = zoneViolations.find(v => v.zone === selectedZoneForModal);
                if (!violation) {
                  return <p>Keine Verstöße gefunden</p>;
                }
                
                return (
                  <>
                    <div style={{ marginBottom: '16px', padding: '12px', backgroundColor: '#fef3c7', borderRadius: '6px', border: '1px solid #fcd34d' }}>
                      <p style={{ fontSize: '14px', margin: 0, color: '#92400e' }}>
                        <strong>⚠️ Problem:</strong><br/>
                        Diese Zone hat <strong>{violation.count} Mittwochspraktikum{violation.count > 1 ? 'a' : ''}</strong> (SFP/ZSP), die zu weit von der Universität entfernt sind.<br/>
                        Studierende müssen mittwochs nachmittags zurück zur Uni.
                      </p>
                    </div>
                    
                    <h4 style={{ fontSize: '15px', fontWeight: '600', marginBottom: '12px', color: '#111827' }}>
                      Betroffene Zuweisungen:
                    </h4>
                    
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                      {violation.internships.map((internship, idx) => (
                        <div key={idx} style={{ 
                          padding: '12px', 
                          border: '2px solid #fca5a5', 
                          borderRadius: '6px',
                          backgroundColor: '#fef2f2'
                        }}>
                          <div style={{ display: 'flex', alignItems: 'start', gap: '8px', marginBottom: '8px' }}>
                            <span style={{ fontSize: '18px' }}>🚫</span>
                            <div style={{ flex: 1 }}>
                              <div style={{ fontWeight: '600', color: '#991b1b', marginBottom: '4px' }}>
                                {internship.praktikumType}
                              </div>
                              <div style={{ fontSize: '14px', color: '#7f1d1d' }}>
                                <strong>Schule:</strong> {internship.schoolName}
                              </div>
                              {internship.teacherName && (
                                <div style={{ fontSize: '14px', color: '#7f1d1d' }}>
                                  <strong>Betreuer:</strong> {internship.teacherName}
                                </div>
                              )}
                            </div>
                          </div>
                          <div style={{ 
                            fontSize: '12px', 
                            color: '#991b1b', 
                            backgroundColor: '#fee2e2',
                            padding: '6px 8px',
                            borderRadius: '4px',
                            marginTop: '8px'
                          }}>
                            💡 <strong>Empfehlung:</strong> Zu Zone 1 (Passau-nah) verschieben
                          </div>
                        </div>
                      ))}
                    </div>
                  </>
                );
              })()}
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setSelectedZoneForModal(null)}>
                Schließen
              </button>
              <button 
                className="btn btn-primary" 
                onClick={() => {
                  setSelectedZoneForModal(null);
                  window.location.href = '/assignments';
                }}
              >
                Beheben
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Unassigned Students Modal */}
      {showUnassignedModal && (
        <div className="modal-overlay" onClick={() => setShowUnassignedModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '600px' }}>
            <div className="modal-header">
              <h2>Nicht zugewiesene Studierende</h2>
              <button className="modal-close" onClick={() => setShowUnassignedModal(false)}>×</button>
            </div>
            <div className="modal-body" style={{ maxHeight: '500px', overflowY: 'auto' }}>
              {unassignedStudents.length === 0 ? (
                <p>Alle Studierenden wurden zugewiesen.</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {unassignedStudents.map((student, idx) => (
                    <div key={idx} style={{ 
                      padding: '12px', 
                      border: '1px solid #e5e7eb', 
                      borderRadius: '6px',
                      backgroundColor: '#f9fafb'
                    }}>
                      <div style={{ fontWeight: '600', marginBottom: '8px', color: '#111827' }}>
                        {student.studentName}
                      </div>
                      {student.assignments.length > 0 ? (
                        <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                          <strong>Zuweisungen:</strong>
                          <ul style={{ margin: '4px 0 0 20px', padding: 0 }}>
                            {student.assignments.map((assignment, assignIdx) => (
                              <li key={assignIdx}>
                                {assignment.praktikumType} - <span style={{ 
                                  color: assignment.status === 'CANCELLED' ? '#dc2626' : '#f59e0b',
                                  fontWeight: '500'
                                }}>{assignment.status}</span>
                              </li>
                            ))}
                          </ul>
                        </div>
                      ) : (
                        <div style={{ fontSize: '0.875rem', color: '#dc2626' }}>
                          Keine Zuweisungen vorhanden
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowUnassignedModal(false)}>
                Schließen
              </button>
              <button 
                className="btn btn-primary" 
                onClick={() => {
                  setShowUnassignedModal(false);
                  window.location.href = '/assignments';
                }}
              >
                Zu Zuweisungen
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Unassigned Students Modal */}
      {showUnassignedModal && (
        <div className="modal-overlay" onClick={() => setShowUnassignedModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '600px', position: 'relative' }}>
            <button 
              className="modal-close" 
              onClick={() => setShowUnassignedModal(false)}
              style={{
                position: 'absolute',
                top: '1rem',
                right: '1rem',
                background: 'none',
                border: 'none',
                fontSize: '1.5rem',
                cursor: 'pointer',
                color: '#6b7280',
                padding: '0',
                lineHeight: '1',
                width: '24px',
                height: '24px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                zIndex: 10
              }}
            >×</button>
            <div className="modal-header">
              <h2>Nicht zugewiesene Studierende</h2>
            </div>
            <div className="modal-body" style={{ maxHeight: '500px', overflowY: 'auto' }}>
              {unassignedStudents.length === 0 ? (
                <p>Alle Studierenden wurden zugewiesen.</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {unassignedStudents.map((student, idx) => (
                    <div key={idx} style={{ 
                      padding: '12px', 
                      border: '1px solid #e5e7eb', 
                      borderRadius: '6px',
                      backgroundColor: '#f9fafb'
                    }}>
                      <div style={{ fontWeight: '600', marginBottom: '8px', color: '#111827' }}>
                        {student.studentName}
                      </div>
                      {student.assignments.length > 0 ? (
                        <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                          <strong>Zuweisungen:</strong>
                          <ul style={{ margin: '4px 0 0 20px', padding: 0 }}>
                            {student.assignments.map((assignment, assignIdx) => (
                              <li key={assignIdx}>
                                {assignment.praktikumType} - <span style={{ 
                                  color: assignment.status === 'CANCELLED' ? '#dc2626' : '#f59e0b',
                                  fontWeight: '500'
                                }}>{assignment.status}</span>
                              </li>
                            ))}
                          </ul>
                        </div>
                      ) : (
                        <div style={{ fontSize: '0.875rem', color: '#dc2626' }}>
                          Keine Zuweisungen vorhanden
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setShowUnassignedModal(false)}>
                Schließen
              </button>
              <button 
                className="btn btn-primary" 
                onClick={() => {
                  setShowUnassignedModal(false);
                  window.location.href = '/assignments';
                }}
              >
                Zu Zuweisungen
              </button>
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
