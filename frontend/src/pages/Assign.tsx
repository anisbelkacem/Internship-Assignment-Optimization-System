import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import studentConfigService from "../services/studentConfigService";
import plService from "../services/plService";
import studentService from "../services/studentService";
import StudentConfigForm from "../components/InternshipsAssignment/InternshipAssignmentStudentForm";
import TeacherConfigForm from "../components/InternshipsAssignment/TeacherConfigForm";
import type { StudentConfigDto } from "../services/studentConfigService";
import type { TeacherPlConfigDto, TeacherDto } from "../services/plService";
import type { Student } from "../services/studentService";
import "../styles/InternshipsAssignment/InternshipAssignmentModal.css";

type TabType = "assignments" | "pl-config" | "student-config";

export default function InternshipAssignments() {
  const [searchParams] = useSearchParams();
  const [activeTab, setActiveTab] = useState<TabType>("assignments");
  const [selectedYear, setSelectedYear] = useState<string>("");
  const [availableYears, setAvailableYears] = useState<string[]>([]);
  const [studentConfigs, setStudentConfigs] = useState<StudentConfigDto[]>([]);
  const [teachers, setTeachers] = useState<TeacherDto[]>([]);
  const [students, setStudents] = useState<Student[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Student Config Modal States
  const [showStudentConfigModal, setShowStudentConfigModal] = useState(false);
  const [editingStudentConfig, setEditingStudentConfig] = useState<StudentConfigDto | null>(null);
  const [showDeleteStudentModal, setShowDeleteStudentModal] = useState(false);
  const [deletingStudentConfigId, setDeletingStudentConfigId] = useState<number | null>(null);

  // Teacher Config Modal States
  const [showTeacherConfigModal, setShowTeacherConfigModal] = useState(false);
  const [editingTeacherConfig, setEditingTeacherConfig] = useState<{teacher: TeacherDto; config: TeacherPlConfigDto | null} | null>(null);
  const [selectedTeacherForConfig, setSelectedTeacherForConfig] = useState<TeacherDto | null>(null);
  const [showDeleteTeacherModal, setShowDeleteTeacherModal] = useState(false);
  const [deletingTeacherConfig, setDeletingTeacherConfig] = useState<{teacherId: number; configId: number} | null>(null);

  // Assignment Results States
  const [showTeacherAssignments, setShowTeacherAssignments] = useState(false);
  const [showStudentAssignments, setShowStudentAssignments] = useState(false);

  const [studentSearchTerm, setStudentSearchTerm] = useState('');
  const [teacherSearchTerm, setTeacherSearchTerm] = useState('');

  // New Year Modal State
  const [showNewYearModal, setShowNewYearModal] = useState(false);
  const [newYearInput, setNewYearInput] = useState('');

  useEffect(() => {
    // Read tab from URL params on component mount
    const tabParam = searchParams.get('tab');
    if (tabParam === 'pl-config' || tabParam === 'student-config') {
      setActiveTab(tabParam as TabType);
    } else {
      setActiveTab('assignments');
    }
  }, [searchParams]);

  useEffect(() => {
    fetchInitialData();
  }, []);

  useEffect(() => {
    if (selectedYear) {
      fetchStudentConfigsByYear();
    }
  }, [selectedYear]);

  const fetchInitialData = async () => {
    try {
      setLoading(true);
      const [teachersData, studentsData] = await Promise.all([
        plService.getAllPls(),
        studentService.getAllStudent(),
      ]);
      
      setTeachers(teachersData);
      setStudents(studentsData);
    } catch (err) {
      console.error("Error fetching initial data:", err);
      setError(err instanceof Error ? err.message : "Failed to load data");
    } finally {
      setLoading(false);
    }
  };

  const fetchStudentConfigsByYear = async () => {
    if (!selectedYear) return;
    try {
      const configs = await studentConfigService.getConfigsByYear(selectedYear);
      console.log('Fetched configs for year', selectedYear, ':', configs);
      setStudentConfigs(configs);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load student configurations");
    }
  };

  const getTeacherName = (teacher: TeacherDto): string => {
    return `${teacher.firstName} ${teacher.lastName}`;
  };

  const handleYearChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedYear(e.target.value);
  };

  // Student Config CRUD Handlers
  const handleOpenStudentConfigModal = (config?: StudentConfigDto) => {
    setEditingStudentConfig(config || null);
    setShowStudentConfigModal(true);
  };

  const handleCloseStudentConfigModal = () => {
    setShowStudentConfigModal(false);
    setEditingStudentConfig(null);
  };

  const handleDeleteStudentConfig = (id: number) => {
    setDeletingStudentConfigId(id);
    setShowDeleteStudentModal(true);
  };

  const handleConfirmDeleteStudentConfig = async () => {
    if (!deletingStudentConfigId) {
      return;
    }

    try {
      await studentConfigService.deleteConfig(deletingStudentConfigId);
      setSuccess("Student configuration deleted successfully!");
      fetchStudentConfigsByYear();
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete configuration");
    } finally {
      setShowDeleteStudentModal(false);
      setDeletingStudentConfigId(null);
    }
  };

  // Teacher Config CRUD Handlers
  const handleOpenTeacherConfigModal = (teacher: TeacherDto, config?: TeacherPlConfigDto) => {
    setSelectedTeacherForConfig(teacher);
    setEditingTeacherConfig({teacher, config: config || null});
    setShowTeacherConfigModal(true);
  };

  const handleCloseTeacherConfigModal = () => {
    setShowTeacherConfigModal(false);
    setEditingTeacherConfig(null);
    setSelectedTeacherForConfig(null);
  };

  const handleDeleteTeacherConfig = (teacherId: number, configId: number) => {
    setDeletingTeacherConfig({teacherId, configId});
    setShowDeleteTeacherModal(true);
  };

  const handleConfirmDeleteTeacherConfig = async () => {
    if (!deletingTeacherConfig) {
      return;
    }

    try {
      await plService.deleteConfig(deletingTeacherConfig.teacherId, deletingTeacherConfig.configId);
      setSuccess("Teacher configuration deleted successfully!");
      await fetchInitialData(); // Refresh teachers with updated configs
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete configuration");
    } finally {
      setShowDeleteTeacherModal(false);
      setDeletingTeacherConfig(null);
    }
  };

  // New Year Handler
  const handleCreateNewYear = () => {
    setShowNewYearModal(true);
  };

  const handleConfirmNewYear = () => {
    if (!newYearInput.trim()) {
      setError("Please enter a valid year");
      setTimeout(() => setError(null), 3000);
      return;
    }

    if (availableYears.includes(newYearInput.trim())) {
      setError("This year already exists");
      setTimeout(() => setError(null), 3000);
      return;
    }

    // Add the new year to the list and select it
    const updatedYears = [...availableYears, newYearInput.trim()].sort();
    setAvailableYears(updatedYears);
    setSelectedYear(newYearInput.trim());
    setStudentConfigs([]); // Clear configs for new year
    
    setShowNewYearModal(false);
    setNewYearInput('');
    setSuccess(`New planning year "${newYearInput.trim()}" created successfully!`);
    setTimeout(() => setSuccess(null), 3000);
  };

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  return (
    <div className="assign-root">
      <div className="assign-header">
        <h1>Praktikum Planning - {selectedYear || "Select Year"}</h1>
        <div className="header-actions" style={{display: 'flex', alignItems: 'center', gap: '10px'}}>
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
          >
            New Year
          </button>
        </div>
      </div>

      {error && (
        <div className="alert alert-error">
          {error}
          <button onClick={() => setError(null)} className="alert-close">×</button>
        </div>
      )}

      {success && (
        <div className="alert alert-success">
          {success}
          <button onClick={() => setSuccess(null)} className="alert-close">×</button>
        </div>
      )}

      {/* Tab Content: Assignments (Default View) */}
      {activeTab === 'assignments' && (
        <>
          {/* Assignment for Teachers Results */}
          <section className="section-container">
            <div className="section-header">
              <div>
                <h2>Assignment for Teachers Results</h2>
                <p style={{color: '#6f7276', fontSize: '13px', marginTop: '4px'}}>
                  Übersicht der Lehrerzuweisungen
                </p>
              </div>
              <button 
                className="btn btn-primary"
                onClick={() => setShowTeacherAssignments(!showTeacherAssignments)}
              >
                {showTeacherAssignments ? "Löschen" : "Zuweisen"}
              </button>
            </div>

            <div className="table-card">
              <div className="table-card-header">
                <div className="table-card-title">
                  <h3>Lehrerzuweisungen</h3>
                  <span className="table-card-subtitle">Alle Praktikumszuweisungen für Lehrkräfte</span>
                </div>
              </div>

              {!showTeacherAssignments ? (
                <p className="table-empty">Klicken Sie auf „Zuweisen", um Zuweisungen zu generieren</p>
              ) : (
                <div className="table-container">
                  <table className="schools-table">
                    <thead>
                      <tr>
                        <th>Lehrername</th>
                        <th>Praktikumszuweisungen</th>
                        <th>Aktionen</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <td>Dr. Smith Johnson</td>
                        <td>
                          <div style={{display: 'flex', flexDirection: 'column', gap: '10px'}}>
                            <div>
                              <strong>PDP-I</strong>
                              <div style={{fontSize: '0.9em', color: '#94a3b8', marginTop: '4px'}}>
                                Schulen: Grundschule A, Mittelschule B
                              </div>
                            </div>
                            <div>
                              <strong>ZSP - Physics</strong>
                              <div style={{fontSize: '0.9em', color: '#94a3b8', marginTop: '4px'}}>
                                Schulen: Grundschule C
                              </div>
                            </div>
                          </div>
                        </td>
                        <td>
                          <button className="btn btn-ghost btn-sm" onClick={() => alert('Edit assignment for Dr. Smith Johnson')}>
                            Bearbeiten
                          </button>
                        </td>
                      </tr>
                      <tr>
                        <td>Prof. Maria Garcia</td>
                        <td>
                          <div style={{display: 'flex', flexDirection: 'column', gap: '10px'}}>
                            <div>
                              <strong>PDP-II - German Literature</strong>
                              <div style={{fontSize: '0.9em', color: '#94a3b8', marginTop: '4px'}}>
                                Schulen: Grundschule C, Mittelschule D
                              </div>
                            </div>
                            <div>
                              <strong>SFP - History</strong>
                              <div style={{fontSize: '0.9em', color: '#94a3b8', marginTop: '4px'}}>
                                Schulen: Grundschule A
                              </div>
                            </div>
                          </div>
                        </td>
                        <td>
                          <button className="btn btn-ghost btn-sm" onClick={() => alert('Edit assignment for Prof. Maria Garcia')}>
                            Bearbeiten
                          </button>
                        </td>
                      </tr>
                      <tr>
                        <td>Dr. Thomas Weber</td>
                        <td>
                          <div style={{display: 'flex', flexDirection: 'column', gap: '10px'}}>
                            <div>
                              <strong>ZSP - Chemistry</strong>
                              <div style={{fontSize: '0.9em', color: '#94a3b8', marginTop: '4px'}}>
                                Schulen: Mittelschule D, Grundschule A
                              </div>
                            </div>
                          </div>
                        </td>
                        <td>
                          <button className="btn btn-ghost btn-sm" onClick={() => alert('Edit assignment for Dr. Thomas Weber')}>
                            Bearbeiten
                          </button>
                        </td>
                      </tr>
                      <tr>
                        <td>Anna Schmidt</td>
                        <td>
                          <div style={{display: 'flex', flexDirection: 'column', gap: '10px'}}>
                            <div>
                              <strong>PDP-I</strong>
                              <div style={{fontSize: '0.9em', color: '#94a3b8', marginTop: '4px'}}>
                                Schulen: Grundschule A
                              </div>
                            </div>
                            <div>
                              <strong>PDP-II - Biology</strong>
                              <div style={{fontSize: '0.9em', color: '#94a3b8', marginTop: '4px'}}>
                                Schulen: Mittelschule B, Grundschule C
                              </div>
                            </div>
                          </div>
                        </td>
                        <td>
                          <button className="btn btn-ghost btn-sm" onClick={() => alert('Edit assignment for Anna Schmidt')}>
                            Bearbeiten
                          </button>
                        </td>
                      </tr>
                      <tr>
                        <td>Dr. Michael Brown</td>
                        <td>
                          <div style={{color: '#d97706', fontStyle: 'italic'}}>
                            Noch keine Zuweisungen
                          </div>
                        </td>
                        <td>
                          <button className="btn btn-ghost btn-sm" onClick={() => alert('Edit assignment for Dr. Michael Brown')}>
                            Bearbeiten
                          </button>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              )}
            </div>

            {showTeacherAssignments && (
              <div style={{marginTop: '16px', display: 'flex', justifyContent: 'flex-end'}}>
                <button className="btn btn-primary" style={{backgroundColor: '#509CDB'}}>
                  Überprüfen
                </button>
              </div>
            )}
          </section>

          {/* Assignment Results - Student */}
          <section className="section-container">
            <div className="section-header">
              <div>
                <h2>Assignment Results - Students</h2>
                <p style={{color: '#6f7276', fontSize: '13px', marginTop: '4px'}}>
                  Übersicht der Praktikumszuweisungen
                </p>
              </div>
              <button 
                className="btn btn-primary"
                onClick={() => setShowStudentAssignments(!showStudentAssignments)}
              >
                {showStudentAssignments ? "Löschen" : "Zuweisen"}
              </button>
            </div>

            <div className="table-card">
              <div className="table-card-header">
                <div className="table-card-title">
                  <h3>Studentenzuweisungen</h3>
                  <span className="table-card-subtitle">Alle Praktikumszuweisungen</span>
                </div>
              </div>

              {!showStudentAssignments ? (
                <p className="table-empty">Klicken Sie auf „Zuweisen", um Zuweisungen zu generieren</p>
              ) : (
                <div className="table-container">
                  <table className="schools-table">
                    <thead>
                      <tr>
                        <th>Schülername</th>
                        <th>Betreuer</th>
                        <th>Praktikumtyp</th>
                        <th>Schule</th>
                        <th>Startdatum</th>
                        <th>Enddatum</th>
                        <th>Status</th>
                        <th>Aktionen</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <td>Emma Müller</td>
                        <td>Dr. Smith Johnson</td>
                        <td>PDP-I</td>
                        <td>Grundschule A</td>
                        <td>2025-03-01</td>
                        <td>2025-03-15</td>
                        <td><span style={{color: '#15803d', fontWeight: 600}}>Bestätigt</span></td>
                        <td>
                          <button className="btn btn-ghost btn-sm">Bearbeiten</button>
                        </td>
                      </tr>
                      <tr>
                        <td>Lukas Wagner</td>
                        <td>Prof. Maria Garcia</td>
                        <td>PDP-II</td>
                        <td>Mittelschule B</td>
                        <td>2025-03-05</td>
                        <td>2025-03-20</td>
                        <td><span style={{color: '#d97706', fontWeight: 600}}>Ausstehend</span></td>
                        <td>
                          <button className="btn btn-ghost btn-sm">Bearbeiten</button>
                        </td>
                      </tr>
                      <tr>
                        <td>Sophie Fischer</td>
                        <td>Dr. Thomas Weber</td>
                        <td>SFP</td>
                        <td>Grundschule C</td>
                        <td>2025-03-10</td>
                        <td>2025-03-25</td>
                        <td><span style={{color: '#15803d', fontWeight: 600}}>Bestätigt</span></td>
                        <td>
                          <button className="btn btn-ghost btn-sm">Bearbeiten</button>
                        </td>
                      </tr>
                      <tr>
                        <td>Max Schneider</td>
                        <td>Anna Schmidt</td>
                        <td>PDP-I</td>
                        <td>Mittelschule D</td>
                        <td>2025-03-08</td>
                        <td>2025-03-22</td>
                        <td><span style={{color: '#d97706', fontWeight: 600}}>Ausstehend</span></td>
                        <td>
                          <button className="btn btn-ghost btn-sm">Bearbeiten</button>
                        </td>
                      </tr>
                      <tr>
                        <td>Lisa Hoffmann</td>
                        <td>Prof. Maria Garcia</td>
                        <td>ZSP</td>
                        <td>Mittelschule B</td>
                        <td>2025-03-12</td>
                        <td>2025-03-28</td>
                        <td><span style={{color: '#15803d', fontWeight: 600}}>Bestätigt</span></td>
                        <td>
                          <button className="btn btn-ghost btn-sm">Bearbeiten</button>
                        </td>
                      </tr>
                      <tr>
                        <td>Jonas Becker</td>
                        <td>Dr. Smith Johnson</td>
                        <td>PDP-I</td>
                        <td>Grundschule A</td>
                        <td>2025-03-15</td>
                        <td>2025-03-30</td>
                        <td><span style={{color: '#15803d', fontWeight: 600}}>Bestätigt</span></td>
                        <td>
                          <button className="btn btn-ghost btn-sm">Bearbeiten</button>
                        </td>
                      </tr>
                      <tr>
                        <td>Mia Koch</td>
                        <td>Anna Schmidt</td>
                        <td>PDP-II</td>
                        <td>Mittelschule D</td>
                        <td>2025-03-18</td>
                        <td>2025-04-02</td>
                        <td><span style={{color: '#d97706', fontWeight: 600}}>Ausstehend</span></td>
                        <td>
                          <button className="btn btn-ghost btn-sm">Bearbeiten</button>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              )}
            </div>

            {showStudentAssignments && (
              <div style={{marginTop: '16px', display: 'flex', justifyContent: 'flex-end'}}>
                <button className="btn btn-primary" style={{backgroundColor: '#509CDB'}}>
                  Überprüfen
                </button>
              </div>
            )}
          </section>
        </>
      )}

      {/* Tab Content: PL Configuration */}
      {activeTab === 'pl-config' && (
        <section className="section-container">
          <div className="section-header">
            <h2>PL Configuration</h2>
          </div>
          <div style={{marginBottom: '20px'}}>
                <input
                  type="text"
                  placeholder="🔍 Search teachers..."
                  value={teacherSearchTerm}
                  onChange={(e) => setTeacherSearchTerm(e.target.value)}
                  style={{
                    width: '280px',
                    padding: '8px 14px',
                    fontSize: '0.95rem',
                    border: '1px solid #e2e8f0',
                    borderRadius: '8px',
                    outline: 'none',
                    transition: 'all 0.2s ease',
                    backgroundColor: '#f8fafc',
                    boxShadow: '0 1px 2px rgba(0, 0, 0, 0.05)',
                    color: '#000000'
                  }}
                  onFocus={(e) => {
                    e.target.style.borderColor = '#3b82f6';
                    e.target.style.backgroundColor = '#ffffff';
                    e.target.style.boxShadow = '0 0 0 3px rgba(59, 130, 246, 0.1)';
                  }}
                  onBlur={(e) => {
                    e.target.style.borderColor = '#e2e8f0';
                    e.target.style.backgroundColor = '#f8fafc';
                    e.target.style.boxShadow = '0 1px 2px rgba(0, 0, 0, 0.05)';
                  }}
                />
          </div>
            <div className="table-container" style={{overflowX: 'auto'}}>
              <table className="schools-table">
                <thead>
                  <tr>
                    <th>Teacher Name</th>
                    <th>Main Subject</th>
                    <th>School Year</th>
                    <th>Subject Specializations</th>
                    <th>Internship Preferences</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {teachers.filter(teacher => {
                    const fullName = `${teacher.firstName} ${teacher.lastName}`.toLowerCase();
                    return fullName.includes(teacherSearchTerm.toLowerCase());
                  }).length === 0 ? (
                    <tr>
                      <td colSpan={6} className="empty-state">
                        {teacherSearchTerm ? 'No teachers match your search' : 'No teachers found'}
                      </td>
                    </tr>
                  ) : (
                    teachers.filter(teacher => {
                      const fullName = `${teacher.firstName} ${teacher.lastName}`.toLowerCase();
                      return fullName.includes(teacherSearchTerm.toLowerCase());
                    }).flatMap((teacher) => {
                      const teacherConfigsForYear = teacher.plConfigs.filter(
                        (config) => config.schoolYear === selectedYear
                      );

                      if (teacherConfigsForYear.length === 0) {
                        return (
                          <tr key={`teacher-${teacher.teacherId}`}>
                            <td>{getTeacherName(teacher)}</td>
                            <td>{teacher.mainSubject}</td>
                            <td colSpan={3} className="empty-state">No configurations</td>
                            <td>
                              <button
                                className="edit-config-btn"
                                onClick={() => handleOpenTeacherConfigModal(teacher)}
                                title="Edit teacher configuration"
                              >
                                ✏️
                              </button>
                            </td>
                          </tr>
                        );
                      }
                      
                      return teacherConfigsForYear.map((config, index) => (
                        <tr key={`teacher-${teacher.teacherId}-config-${config.id}`}>
                          {index === 0 && (
                            <>
                              <td rowSpan={teacherConfigsForYear.length}>{getTeacherName(teacher)}</td>
                              <td rowSpan={teacherConfigsForYear.length}>{teacher.mainSubject}</td>
                            </>
                          )}
                          <td>{config.schoolYear}</td>
                          <td>{config.subjectSpecializations.join(", ") || "-"}</td>
                          <td>{config.internshipPreferences.join(", ") || "-"}</td>
                          <td>
                            <div className="action-buttons">
                              <button
                                className="btn-secondary btn-sm"
                                onClick={() => handleOpenTeacherConfigModal(teacher, config)}
                              >
                                Edit
                              </button>
                              <button
                                className="btn-danger btn-sm"
                                onClick={() => handleDeleteTeacherConfig(teacher.teacherId, config.id)}
                              >
                                Delete
                              </button>
                            </div>
                          </td>
                        </tr>
                      ));
                    })
                  )}
                </tbody>
              </table>
            </div>
        </section>
      )}

      {/* Tab Content: Student Configuration */}
      {activeTab === 'student-config' && (
        <section className="section-container">
          <div className="section-header">
            <h2>Student Configuration</h2>
            <button 
              className="btn btn-primary"
              onClick={() => handleOpenStudentConfigModal()}
            >
              Add Config
            </button>
          </div>
          <div style={{marginBottom: '20px'}}>
                <input
                  type="text"
                  placeholder="🔍 Search students..."
                  value={studentSearchTerm}
                  onChange={(e) => setStudentSearchTerm(e.target.value)}
                  style={{
                    width: '280px',
                    padding: '8px 14px',
                    fontSize: '0.95rem',
                    border: '1px solid #e2e8f0',
                    borderRadius: '8px',
                    outline: 'none',
                    transition: 'all 0.2s ease',
                    backgroundColor: '#f8fafc',
                    boxShadow: '0 1px 2px rgba(0, 0, 0, 0.05)',
                    color: '#000000'
                  }}
                  onFocus={(e) => {
                    e.target.style.borderColor = '#3b82f6';
                    e.target.style.backgroundColor = '#ffffff';
                    e.target.style.boxShadow = '0 0 0 3px rgba(59, 130, 246, 0.1)';
                  }}
                  onBlur={(e) => {
                    e.target.style.borderColor = '#e2e8f0';
                    e.target.style.backgroundColor = '#f8fafc';
                    e.target.style.boxShadow = '0 1px 2px rgba(0, 0, 0, 0.05)';
                  }}
                />
          </div>
            <div className="table-container" style={{overflowX: 'auto'}}>
              <table className="schools-table">
                <thead>
                  <tr>
                    <th style={{ whiteSpace: 'nowrap' }}>Student Name</th>
                    <th style={{ whiteSpace: 'nowrap' }}>Year</th>
                    <th style={{ whiteSpace: 'nowrap' }}>School Type</th>
                    <th style={{ whiteSpace: 'nowrap' }}>Main Course</th>
                    <th style={{ whiteSpace: 'nowrap' }}>Course 1</th>
                    <th style={{ whiteSpace: 'nowrap' }}>Course 2</th>
                    <th style={{ whiteSpace: 'nowrap' }}>Course 3</th>
                    <th style={{ whiteSpace: 'nowrap' }}>PDP-I</th>
                    <th style={{ whiteSpace: 'nowrap' }}>PDP-II</th>
                    <th style={{ whiteSpace: 'nowrap' }}>ZSP</th>
                    <th style={{ whiteSpace: 'nowrap' }}>SFP</th>
                    <th style={{ whiteSpace: 'nowrap' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {students.filter(student => {
                    const fullName = `${student.firstName} ${student.lastName}`.toLowerCase();
                    return fullName.includes(studentSearchTerm.toLowerCase());
                  }).length === 0 ? (
                    <tr>
                      <td colSpan={12} className="empty-state">
                        {studentSearchTerm ? 'No students match your search' : 'No students found'}
                      </td>
                    </tr>
                  ) : (
                    students.filter(student => {
                      const fullName = `${student.firstName} ${student.lastName}`.toLowerCase();
                      return fullName.includes(studentSearchTerm.toLowerCase());
                    }).flatMap((student) => {
                      const studentConfigsForYear = studentConfigs.filter(
                        (config) => config.studentId === student.matriculationNbr && config.year === selectedYear
                      );

                      if (studentConfigsForYear.length === 0) {
                        return (
                          <tr key={`student-${student.matriculationNbr}`}>
                            <td>{student.firstName} {student.lastName}</td>
                            <td colSpan={10} className="empty-state">No configurations</td>
                            <td>
                              <button
                                className="edit-config-btn"
                                onClick={() => {
                                  setEditingStudentConfig({
                                    studentId: student.matriculationNbr,
                                    year: selectedYear,
                                    schoolType: student.schoolType,
                                    mainCourse: student.mainCourse,
                                    prefCourse1: student.prefCourse1,
                                    prefCourse2: student.prefCourse2,
                                    prefCourse3: student.prefCourse3,
                                    pdpI: false,
                                    pdpII: false,
                                    zsp: false,
                                    sfp: false,
                                  } as StudentConfigDto);
                                  setShowStudentConfigModal(true);
                                }}
                                title="Edit student configuration"
                              >
                                ✏️
                              </button>
                            </td>
                          </tr>
                        );
                      }

                      return studentConfigsForYear.map((config, index) => (
                        <tr key={`student-${student.matriculationNbr}-config-${config.id}`}>
                          {index === 0 && (
                            <td rowSpan={studentConfigsForYear.length}>
                              {student.firstName} {student.lastName}
                            </td>
                          )}
                          <td>{config.year}</td>
                          <td>{config.schoolType}</td>
                          <td>{config.mainCourse}</td>
                          <td>{config.prefCourse1 || "-"}</td>
                          <td>{config.prefCourse2 || "-"}</td>
                          <td>{config.prefCourse3 || "-"}</td>
                          <td>
                            <input type="checkbox" checked={config.pdpI} disabled />
                          </td>
                          <td>
                            <input type="checkbox" checked={config.pdpII} disabled />
                          </td>
                          <td>
                            <input type="checkbox" checked={config.zsp} disabled />
                          </td>
                          <td>
                            <input type="checkbox" checked={config.sfp} disabled />
                          </td>
                          <td>
                            <div className="action-buttons">
                              <button
                                className="btn-secondary btn-sm"
                                onClick={() => handleOpenStudentConfigModal(config)}
                              >
                                Edit
                              </button>
                              <button
                                className="btn-danger btn-sm"
                                onClick={() => config.id && handleDeleteStudentConfig(config.id)}
                              >
                                Delete
                              </button>
                            </div>
                          </td>
                        </tr>
                      ));
                    })
                  )}
                </tbody>
              </table>
            </div>
        </section>
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

      {/* Delete Student Config Confirmation Modal */}
      {showDeleteStudentModal && (
        <div className="modal-overlay">
          <div className="modal-content modal-small">
            <h2>Confirm Delete</h2>
            <p>Are you sure you want to delete this student configuration?</p>
            <div className="modal-actions">
              <button
                className="btn-secondary"
                onClick={() => setShowDeleteStudentModal(false)}
              >
                Cancel
              </button>
              <button
                className="btn-danger"
                onClick={handleConfirmDeleteStudentConfig}
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Student Config Form Modal */}
      {showStudentConfigModal && (
        <div className="modal-overlay" onClick={handleCloseStudentConfigModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <StudentConfigForm
              config={editingStudentConfig || undefined}
              year={selectedYear}
              onClose={handleCloseStudentConfigModal}
              onSave={async () => {
                const wasEditing = !!editingStudentConfig?.id;
                handleCloseStudentConfigModal();
                
                // Refresh years list and configs
                const yearsData = await studentConfigService.getAllYears();
                setAvailableYears(yearsData);
                await fetchStudentConfigsByYear();
                
                setSuccess(wasEditing ? "Student configuration updated!" : "Student configuration created!");
                setTimeout(() => setSuccess(null), 3000);
              }}
            />
          </div>
        </div>
      )}

      {/* Delete Teacher Config Confirmation Modal */}
      {showDeleteTeacherModal && (
        <div className="modal-overlay">
          <div className="modal-content modal-small">
            <h2>Confirm Delete</h2>
            <p>Are you sure you want to delete this teacher configuration?</p>
            <div className="modal-actions">
              <button
                className="btn-secondary"
                onClick={() => setShowDeleteTeacherModal(false)}
              >
                Cancel
              </button>
              <button
                className="btn-danger"
                onClick={handleConfirmDeleteTeacherConfig}
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Teacher Config Form Modal */}
      {showTeacherConfigModal && selectedTeacherForConfig && (
        <div className="modal-overlay" onClick={handleCloseTeacherConfigModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <TeacherConfigForm
              teacher={selectedTeacherForConfig}
              config={editingTeacherConfig?.config || undefined}
              selectedYear={selectedYear}
              onClose={handleCloseTeacherConfigModal}
              onSave={async () => {
                handleCloseTeacherConfigModal();
                await fetchInitialData();
                setSuccess(editingTeacherConfig?.config ? "Teacher configuration updated!" : "Teacher configuration created!");
                setTimeout(() => setSuccess(null), 3000);
              }}
            />
          </div>
        </div>
      )}
    </div>
  );
}
