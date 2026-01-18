/* eslint-disable @typescript-eslint/no-explicit-any */
import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import studentConfigService from "../services/studentConfigService";
import plService from "../services/plService";
import studentService from "../services/studentService";
import schoolService from "../services/schoolService";
import internshipAssignmentService from "../services/internshipAssignmentService";
import StudentConfigForm from "../components/InternshipsAssignment/InternshipAssignmentStudentForm";
import TeacherConfigForm from "../components/InternshipsAssignment/TeacherConfigForm";
import EditPlannedInternshipForm from "../components/InternshipsAssignment/EditPlannedInternshipForm";
import type { StudentConfigDto } from "../services/studentConfigService";
import type { TeacherPlConfigDto, TeacherDto } from "../services/plService";
import type { Student } from "../services/studentService";
import type { TeacherAssignmentResult, PlannedInternshipDto, StudentAssignmentResult, AssignmentDto } from "../services/internshipAssignmentService";
import "../styles/InternshipsAssignment/InternshipAssignmentModal.css";
import "../styles/InternshipsAssignment/StudentConfigTable.css";

type TabType = "assignments" | "pl-config" | "student-config";

export default function InternshipAssignments() {
  const [searchParams] = useSearchParams();
  const [activeTab, setActiveTab] = useState<TabType>("assignments");
  const [selectedYear, setSelectedYear] = useState<string>("");
  const [availableYears, setAvailableYears] = useState<string[]>([]);
  const [studentConfigs, setStudentConfigs] = useState<StudentConfigDto[]>([]);
  const [teachers, setTeachers] = useState<TeacherDto[]>([]);
  const [students, setStudents] = useState<Student[]>([]);
  const [schools, setSchools] = useState<any[]>([]);
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
  const [phase1Result, setPhase1Result] = useState<TeacherAssignmentResult | null>(null);
  const [assigningPhase1, setAssigningPhase1] = useState(false);
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [_phase2Result, setPhase2Result] = useState<StudentAssignmentResult | null>(null);
  const [assigningPhase2, setAssigningPhase2] = useState(false);
  const [studentAssignments, setStudentAssignments] = useState<AssignmentDto[]>([]);

  // Edit Planned Internship Modal State
  const [showEditInternshipModal, setShowEditInternshipModal] = useState(false);
  const [editingInternship, setEditingInternship] = useState<PlannedInternshipDto | null>(null);

  // Edit Student Assignment Modal State
  const [showEditAssignmentModal, setShowEditAssignmentModal] = useState(false);
  const [editingAssignment, setEditingAssignment] = useState<AssignmentDto | null>(null);

  const [studentSearchTerm, setStudentSearchTerm] = useState('');
  const [teacherSearchTerm, setTeacherSearchTerm] = useState('');

  // New Year Modal State
  const [showNewYearModal, setShowNewYearModal] = useState(false);
  const [newYearInput, setNewYearInput] = useState('');

  // eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/no-unused-vars
  const [validationResult, setValidationResult] = useState<any>(null);

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
      fetchPlannedInternships(); // Load saved teacher assignments
      fetchStudentAssignments(); // Load saved student assignments
    }
  }, [selectedYear]);

const fetchInitialData = async () => {
  try {
    setLoading(true);
    const [yearsData, teachersData, studentsData, schoolsData] = await Promise.all([
      studentConfigService.getAllYears(),
      plService.getAllPls(),
      studentService.getAllStudent(),
      schoolService.getAllSchools(),
    ]);
    
    setAvailableYears(yearsData);
    setTeachers(teachersData);
    setSchools(schoolsData);
    setStudents(studentsData);
    
    if (yearsData.length > 0) {
      setSelectedYear(yearsData[0]);
    }
  } catch (err) {
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

  // Phase 1 Optimization Handler
  const handlePhase1Assignment = async () => {
    if (!selectedYear) {
      setError("Please select a school year first");
      setTimeout(() => setError(null), 3000);
      return;
    }

    setAssigningPhase1(true);
    
    try {
      const result = await internshipAssignmentService.optimizePhase1(selectedYear, 28);
      setSuccess(`Phase 1 optimization complete! Assigned ${result.assignedCount}/${result.totalPlannedInternships} internships.`);
      setTimeout(() => setSuccess(null), 5000);
      
      // Fetch saved results from database
      await fetchPlannedInternships();
      setShowTeacherAssignments(true);
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : "Optimization failed";
      setError(errorMsg);
      setTimeout(() => setError(null), 5000);
    } finally {
      setAssigningPhase1(false);
    }
  };

  // Fetch planned internships from database
  const fetchPlannedInternships = async () => {
    if (!selectedYear) return;
    
    try {
      const internships = await internshipAssignmentService.getPlannedInternships(selectedYear);
      const mockResult: TeacherAssignmentResult = {
        schoolYear: selectedYear,
        totalPlannedInternships: internships.length,
        assignedCount: internships.filter(i => i.teacherId).length,
        unassignedCount: internships.filter(i => !i.teacherId).length,
        score: "",
        plannedInternships: internships
      };
      setPhase1Result(mockResult);
      if (internships.length > 0) {
        setShowTeacherAssignments(true);
      }
    } catch (err) {
      console.error("Failed to fetch planned internships:", err);
    }
  };

  // Delete all assignments for current year
  const handleDeleteAllAssignments = async () => {
    if (!selectedYear) return;
    
    if (!confirm(`Delete all teacher assignments for ${selectedYear}?`)) {
      return;
    }

    try {
      await internshipAssignmentService.deleteAllPlannedInternships(selectedYear);
      setPhase1Result(null);
      setShowTeacherAssignments(false);
      setSuccess("All assignments deleted successfully!");
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete assignments");
      setTimeout(() => setError(null), 3000);
    }
  };

  // Phase 2: Student Assignment Optimization
  const handleAssignPhase2 = async () => {
    if (!selectedYear) {
      setError("Please select a school year");
      setTimeout(() => setError(null), 3000);
      return;
    }

    setAssigningPhase2(true);
    
    try {
      const result = await internshipAssignmentService.optimizePhase2(selectedYear);
      setPhase2Result(result);
      setStudentAssignments(result.assignments);
      setSuccess(`Phase 2 optimization complete! Assigned ${result.assignedStudents}/${result.totalStudents} students.`);
      setTimeout(() => setSuccess(null), 5000);
      setShowStudentAssignments(true);
    } catch (err: any) {
      console.error("Phase 2 optimization error:", err);
      let errorMsg = "Phase 2 optimization failed";
      
      if (err.status === 401 || err.status === 403) {
        errorMsg = "Authentication error: Please log in again";
      } else if (err.message) {
        errorMsg = err.message;
      }
      
      setError(errorMsg);
      setTimeout(() => setError(null), 5000);
    } finally {
      setAssigningPhase2(false);
    }
  };

  // Fetch student assignments from database
  const fetchStudentAssignments = async () => {
    if (!selectedYear) return;
    
    try {
      const assignments = await internshipAssignmentService.getStudentAssignments(selectedYear);
      setStudentAssignments(assignments);
      if (assignments.length > 0) {
        setShowStudentAssignments(true);
        // Build mock result for display
        const mockResult: StudentAssignmentResult = {
          schoolYear: selectedYear,
          totalStudents: assignments.length,
          assignedStudents: assignments.length,
          unassignedStudents: 0,
          score: "",
          assignments: assignments
        };
        setPhase2Result(mockResult);
      }
    } catch (err) {
      console.error("Failed to fetch student assignments:", err);
    }
  };

  // Delete all student assignments for current year
  const handleDeleteAllStudentAssignments = async () => {
    if (!selectedYear) return;
    
    if (!confirm(`Delete all student assignments for ${selectedYear}?`)) {
      return;
    }

    try {
      await internshipAssignmentService.deleteAllStudentAssignments(selectedYear);
      setPhase2Result(null);
      setStudentAssignments([]);
      setShowStudentAssignments(false);
      setSuccess("All student assignments deleted successfully!");
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete student assignments");
      setTimeout(() => setError(null), 3000);
    }
  };

  // Edit Planned Internship Handlers
  const handleEditInternship = (internship: PlannedInternshipDto) => {
    setEditingInternship(internship);
    setShowEditInternshipModal(true);
  };

  const handleCloseEditInternshipModal = () => {
    setShowEditInternshipModal(false);
    setEditingInternship(null);
  };

  const handleSaveEditInternship = async () => {
    handleCloseEditInternshipModal();
    await fetchPlannedInternships();
    setSuccess("Assignment updated successfully!");
    setTimeout(() => setSuccess(null), 3000);
  };

  // Edit Student Assignment Handlers
  const handleEditAssignment = (assignment: AssignmentDto) => {
    console.log('Original assignment:', assignment);
    console.log('Available teachers:', teachers);
    console.log('Available schools:', schools);
    
    // Find teacher and school IDs by matching names
    const teacher = teachers.find(t => 
      `${t.firstName} ${t.lastName}` === assignment.teacherName
    );
    const school = schools.find(s => s.name === assignment.schoolName);
    
    console.log('Found teacher:', teacher);
    console.log('Found school:', school);
    
    const updatedAssignment = {
      ...assignment,
      teacherId: teacher?.teacherId || assignment.teacherId || null,
      schoolId: school?.id || assignment.schoolId || null,
    };
    
    console.log('Updated assignment with IDs:', updatedAssignment);
    setEditingAssignment(updatedAssignment);
    setShowEditAssignmentModal(true);
    setValidationResult(null);
  };

  const handleCloseEditAssignmentModal = () => {
    setShowEditAssignmentModal(false);
    setEditingAssignment(null);
    setValidationResult(null);
  };

  const handleSaveEditAssignment = async () => {
    if (!editingAssignment) return;
    
    console.log('=== SAVING ASSIGNMENT ===');
    console.log('Full assignment object:', editingAssignment);
    console.log('Saving assignment with data:', {
      id: editingAssignment.id,
      teacherId: editingAssignment.teacherId,
      schoolId: editingAssignment.schoolId,
      status: editingAssignment.status,
    });
    
    try {
      const result = await internshipAssignmentService.updateStudentAssignment(
        editingAssignment.id,
        {
          teacherId: editingAssignment.teacherId || null,
          schoolId: editingAssignment.schoolId || null,
          status: editingAssignment.status,
        }
      );
      console.log('Update result from backend:', result);
      console.log('Fetching updated assignments...');
      await fetchStudentAssignments();
      console.log('Student assignments refreshed');
      setSuccess("Student assignment updated successfully!");
      setTimeout(() => setSuccess(null), 3000);
      handleCloseEditAssignmentModal();
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err:any) {
      console.error('Update error:', err);

    //  backend validation (AssignmentValidationException -> ValidationResult) comes here
    if (err?.status === 400 && err?.hardViolations) {
      setValidationResult(err);       // store full ValidationResult
      setError(null);                 // don't show generic "Error"
      return;                         // keep modal open
    }

    // fallback generic error
    setValidationResult(null);
    setError(err?.message || "Failed to update assignment");
    setTimeout(() => setError(null), 3000);
    }
  };

  const handleValidateAssignment = async (assignmentId: number) => {
    const confirmed = window.confirm('Möchten Sie diese Zuweisung bestätigen?');
    if (!confirmed) {
      console.log('Validation cancelled by user');
      return;
    }
    
    console.log('Validating assignment:', assignmentId);
    try {
      const result = await internshipAssignmentService.updateAssignmentStatus(assignmentId, 'CONFIRMED');
      console.log('Validation result:', result);
      await fetchStudentAssignments();
      console.log('Student assignments refreshed');
      setSuccess("Assignment validated successfully!");
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      console.error('Validation error:', err);
      setError(err instanceof Error ? err.message : "Failed to validate assignment");
      setTimeout(() => setError(null), 3000);
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
    return (
      <div className="schools-container">
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="schools-container">
      <div className="schools-header">
        <div className="schools-header-content">
          <h1>Praktikum Planning</h1>
        </div>
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
        <div className="error-container">
          <strong>Error:</strong> {error}
        </div>
      )}

      {success && (
        <div className="success-container">
          <strong>Success:</strong> {success}
        </div>
      )}

      {/* Tab Content: Assignments (Default View) */}
      {activeTab === 'assignments' && (
        <>
          {/* Assignment for Teachers Results */}
          <div style={{marginBottom: '24px'}}>
            <div className="schools-header" style={{marginBottom: '16px'}}>
              <div className="schools-header-content">
                <h1 style={{fontSize: '20px'}}>Lehrerzuweisungen</h1>
              </div>
              <div className="header-actions">
                <button 
                  className="btn-primary"
                  onClick={handlePhase1Assignment}
                  disabled={!selectedYear || assigningPhase1}
                >
                  {assigningPhase1 ? "Optimizing..." : "Zuweisen"}
                </button>
              </div>
            </div>

            {/* Optimization Progress Bar */}
            {assigningPhase1 && (
              <div className="optimization-progress-container">
                <span className="optimization-progress-text">⚙️ Optimierung läuft...</span>
                <div className="progress-bar-wrapper">
                  <div className="progress-bar-animated"></div>
                </div>
              </div>
            )}

            <div className="schools-table-container">
              {!showTeacherAssignments || !phase1Result ? (
                <div className="empty-state">
                  <h3>Keine Zuweisungen vorhanden</h3>
                  <p>Klicken Sie auf „Zuweisen", um Zuweisungen zu generieren</p>
                </div>
              ) : (
                <>
                  {phase1Result && (
                    <div style={{marginBottom: '16px', padding: '12px', backgroundColor: '#f0f9ff', border: '1px solid #bae6fd', borderRadius: '8px'}}>
                      <div style={{display: 'flex', gap: '24px', fontSize: '0.9em'}}>
                        <span><strong>Total Internships:</strong> {phase1Result.totalPlannedInternships}</span>
                        <span style={{color: '#15803d'}}><strong>Assigned:</strong> {phase1Result.assignedCount}</span>
                        <span style={{color: '#dc2626'}}><strong>Unassigned:</strong> {phase1Result.unassignedCount}</span>
                      </div>
                    </div>
                  )}
                  <table className="schools-table">
                    <thead>
                      <tr>
                        <th>Lehrername</th>
                        <th>Praktikumtyp</th>
                        <th>Kurs</th>
                        <th>Schule</th>
                        <th>Zone</th>
                        <th>Schultyp</th>
                        <th>Aktionen</th>
                      </tr>
                    </thead>
                    <tbody>
                        {phase1Result.plannedInternships.length === 0 ? (
                          <tr>
                            <td colSpan={7} className="table-empty">
                              No assignments generated
                            </td>
                          </tr>
                        ) : (
                          phase1Result.plannedInternships.map((pi) => (
                            <tr key={pi.id}>
                              <td>
                                <span style={{
                                  fontWeight: pi.teacherName ? '500' : 'normal',
                                  color: pi.teacherName ? 'inherit' : '#dc2626'
                                }}>
                                  {pi.teacherName ?? 'Unassigned'}
                                </span>
                              </td>
                              <td>{pi.praktikumType}</td>
                              <td>{pi.course ?? '—'}</td>
                              <td>{pi.schoolName ?? '—'}</td>
                              <td>{pi.schoolZone ?? '—'}</td>
                              <td>{pi.schoolType}</td>
                              <td>
                                <button 
                                  className="action-btn edit-btn"
                                  onClick={() => handleEditInternship(pi)}
                                  title="Bearbeiten"
                                >
                                  ✏️
                                </button>
                              </td>
                            </tr>
                          ))
                        )}
                      </tbody>
                    </table>
                    
                    {/* Delete All Button at Bottom */}
                    <div className="table-footer-actions">
                      <button 
                        className="btn-delete-all" 
                        onClick={handleDeleteAllAssignments}
                      >
                        <span className="delete-icon-circle">
                          <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M2 4h12M5.333 4V2.667a1.333 1.333 0 0 1 1.334-1.334h2.666a1.333 1.333 0 0 1 1.334 1.334V4m2 0v9.333a1.333 1.333 0 0 1-1.334 1.334H4.667a1.333 1.333 0 0 1-1.334-1.334V4h9.334Z" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                          </svg>
                        </span>
                        Alle Löschen
                      </button>
                    </div>
                </>
              )}
            </div>
          </div>

          {/* Assignment Results - Student */}
          <div style={{marginBottom: '24px'}}>
            <div className="schools-header" style={{marginBottom: '16px'}}>
              <div className="schools-header-content">
                <h1 style={{fontSize: '20px'}}>Studentenzuweisungen</h1>
              </div>
              <div className="header-actions">
                <button 
                  className="btn-primary"
                  onClick={handleAssignPhase2}
                  disabled={assigningPhase2 || !phase1Result}
                >
                  {assigningPhase2 ? "Läuft..." : "Zuweisen"}
                </button>
              </div>
            </div>

            {/* Phase 2 Progress Bar */}
            {assigningPhase2 && (
              <div className="optimization-progress-container">
                <span className="optimization-progress-text">Optimierung läuft...</span>
                <div className="progress-bar-wrapper">
                  <div className="progress-bar-animated"></div>
                </div>
              </div>
            )}

            <div className="schools-table-container">
              {!showStudentAssignments ? (
                <div className="empty-state">
                  <h3>Keine Zuweisungen vorhanden</h3>
                  <p>Klicken Sie auf „Zuweisen", um Zuweisungen zu generieren</p>
                </div>
              ) : (
                <>
                  <table className="schools-table">
                    <thead>
                      <tr>
                        <th>Schülername</th>
                        <th>Betreuer</th>
                        <th>Praktikumtyp</th>
                        <th>Kurs</th>
                        <th>Schule</th>
                        <th>Status</th>
                        <th>Aktionen</th>
                      </tr>
                    </thead>
                    <tbody>
                      {studentAssignments.length === 0 ? (
                        <tr>
                          <td colSpan={7} className="table-empty">
                            No student assignments generated
                          </td>
                        </tr>
                      ) : (
                        studentAssignments.map((assignment) => (
                          <tr key={assignment.id}>
                            <td>
                              <span style={{fontWeight: '500'}}>
                                {assignment.studentName}
                              </span>
                            </td>
                            <td>{assignment.teacherName ?? '—'}</td>
                            <td>{assignment.praktikumType}</td>
                            <td>{assignment.course ?? '—'}</td>
                            <td>{assignment.schoolName ?? '—'}</td>
                            <td>
                              <span className={`status-badge ${
                                assignment.status === 'CONFIRMED' ? 'status-confirmed' : 
                                assignment.status === 'PROPOSED' ? 'status-proposed' : 
                                'status-other'
                              }`}>
                                {assignment.status === 'CONFIRMED' ? 'Bestätigt' : 
                                 assignment.status === 'PROPOSED' ? 'Vorgeschlagen' : 
                                 assignment.status}
                              </span>
                            </td>
                            <td>
                              <button 
                                className="action-btn edit-btn"
                                onClick={() => handleEditAssignment(assignment)}
                                title="Bearbeiten"
                              >
                                ✏️
                              </button>
                              <button 
                                className="action-btn validate-btn"
                                onClick={() => handleValidateAssignment(assignment.id)}
                                title="Bestätigen"
                                style={{marginLeft: '8px'}}
                              >
                                ✓
                              </button>
                            </td>
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                  
                  {/* Delete All Button at Bottom */}
                  <div className="table-footer-actions">
                    <button 
                      className="btn-delete-all" 
                      onClick={handleDeleteAllStudentAssignments}
                    >
                      <span className="delete-icon-circle">
                        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                          <path d="M2 4h12M5.333 4V2.667a1.333 1.333 0 0 1 1.334-1.334h2.666a1.333 1.333 0 0 1 1.334 1.334V4m2 0v9.333a1.333 1.333 0 0 1-1.334 1.334H4.667a1.333 1.333 0 0 1-1.334-1.334V4h9.334Z" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                      </span>
                      Alle Löschen
                    </button>
                  </div>
                </>
              )}
            </div>
          </div>
        </>
      )}

      {/* Tab Content: PL Configuration */}
      {activeTab === 'pl-config' && (
        <div style={{marginBottom: '24px'}}>
          <div className="schools-header" style={{marginBottom: '16px'}}>
            <div className="schools-header-content">
              <h1 style={{fontSize: '20px'}}>PL Configuration</h1>
              <p>Praktikumslehrer Konfiguration und Präferenzen</p>
            </div>
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
            <div className="schools-table-container">
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
                            <td>{teacher.mainSubject.name}</td>
                            <td colSpan={3} className="empty-state">No configurations</td>
                            <td>
                              <button
                                className="action-btn edit-btn"
                                onClick={() => handleOpenTeacherConfigModal(teacher)}
                                title="Edit teacher configuration"
                                aria-label="Edit teacher configuration"
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
                              <td rowSpan={teacherConfigsForYear.length}>{teacher.mainSubject.name}</td>
                            </>
                          )}
                          <td>{config.schoolYear}</td>
                          <td>
                            {config.subjectSpecializations && config.subjectSpecializations.length > 0 
                              ? config.subjectSpecializations.map(course => course.name).join(", ")
                              : "-"
                            }
                          </td>             
                    <td>
                        {config.internshipPreferences && config.internshipPreferences.length > 0
                          ? config.internshipPreferences.join(", ")
                          : "-"
                        }
                      </td>                          <td>
                            <div className="action-buttons">
                              <button
                                className="action-btn edit-btn"
                                onClick={() => handleOpenTeacherConfigModal(teacher, config)}
                                title="Edit configuration"
                                aria-label="Edit configuration"
                              >
                                ✏️
                              </button>
                              <button
                                className="action-btn delete-btn"
                                onClick={() => handleDeleteTeacherConfig(teacher.teacherId, config.id)}
                                title="Delete configuration"
                                aria-label="Delete configuration"
                              >
                                🗑️
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
        </div>
      )}

      {/* Tab Content: Student Configuration */}
      {activeTab === 'student-config' && (
        <div style={{marginBottom: '24px'}}>
          <div className="schools-header" style={{marginBottom: '16px'}}>
            <div className="schools-header-content">
              <h1 style={{fontSize: '20px'}}>Schülerkonfiguration</h1>
              <p>Konfiguration der Studierenden für Praktikumszuweisungen</p>
            </div>
            <div className="header-actions">
              <button 
                className="btn-primary"
                onClick={() => handleOpenStudentConfigModal()}
              >
                Konfigurieren
              </button>
            </div>
          </div>
          <div style={{marginBottom: '20px'}}>
                <input
                  type="text"
                  placeholder="🔍 Schüler suchen..."
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
            <div className="schools-table-container">
              <table className="schools-table">
                <thead>
                  <tr>
                    <th style={{ whiteSpace: 'nowrap' }}>Schülername</th>
                    <th style={{ whiteSpace: 'nowrap' }}>Jahr</th>
                    <th style={{ whiteSpace: 'nowrap' }}>Schultyp</th>
                    <th style={{ whiteSpace: 'nowrap' }}>Hauptfach</th>
                    <th style={{ whiteSpace: 'nowrap' }}>Kurs 1</th>
                    <th style={{ whiteSpace: 'nowrap' }}>Kurs 2</th>
                    <th style={{ whiteSpace: 'nowrap' }}>Kurs 3</th>
                    <th style={{ whiteSpace: 'nowrap' }}>Praktika</th>
                    <th style={{ whiteSpace: 'nowrap' }}>Aktionen</th>
                  </tr>
                </thead>
<tbody>
  {students.filter(student => {
    const fullName = `${student.firstName} ${student.lastName}`.toLowerCase();
    return fullName.includes(studentSearchTerm.toLowerCase());
  }).length === 0 ? (
    <tr>
      <td colSpan={12} className="empty-state">
        {studentSearchTerm ? "No students match your search" : "No students found"}
      </td>
    </tr>
  ) : (
    students
      .filter(student => {
        const fullName = `${student.firstName} ${student.lastName}`.toLowerCase();
        return fullName.includes(studentSearchTerm.toLowerCase());
      })
      .flatMap(student => {
        const studentConfigsForYear = studentConfigs.filter(
          config =>
            config.studentId === student.matriculationNbr &&
            config.year === selectedYear
        );

if (studentConfigsForYear.length === 0) {
  return (
    <tr key={`student-${student.matriculationNbr}`}>
      <td>{student.firstName} {student.lastName}</td>
      <td colSpan={8} className="empty-state">No configurations</td>
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
            <td>{config.mainCourse?.name ?? "-"}</td>
            <td>{config.prefCourse1?.name ?? "-"}</td>
            <td>{config.prefCourse2?.name ?? "-"}</td>
            <td>{config.prefCourse3?.name ?? "-"}</td>
            <td>
              <div className="pill-row">
                {['PDP I', 'PDP II', 'ZSP', 'SFP']
                  .filter(label => {
                    if (label === 'PDP I') return config.pdpI;
                    if (label === 'PDP II') return config.pdpII;
                    if (label === 'ZSP') return config.zsp;
                    return config.sfp;
                  })
                  .map(label => (
                    <span key={label} className="internship-pill">{label}</span>
                  ))}
                {!config.pdpI && !config.pdpII && !config.zsp && !config.sfp && (
                  <span className="muted-dash">-</span>
                )}
              </div>
            </td>
            <td>
              <div className="action-buttons">
                <button
                  className="action-btn edit-btn"
                  onClick={() => handleOpenStudentConfigModal(config)}
                  title="Edit"
                >
                  ✏️
                </button>
                <button
                  className="action-btn delete-btn"
                  onClick={() => config.id && handleDeleteStudentConfig(config.id)}
                  title="Delete"
                >
                  🗑️
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

      {/* Edit Planned Internship Modal */}
      {showEditInternshipModal && editingInternship && (
        <div className="modal-overlay" onClick={handleCloseEditInternshipModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <EditPlannedInternshipForm
              internship={editingInternship}
              onClose={handleCloseEditInternshipModal}
              onSave={handleSaveEditInternship}
            />
          </div>
        </div>
      )}

      {/* Edit Student Assignment Modal */}
      {showEditAssignmentModal && editingAssignment && (
        <div className="modal-overlay" onClick={handleCloseEditAssignmentModal}>
          <div className="modal-content modal-small" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Zuweisung bearbeiten</h2>
            </div>
            <div className="modal-body">


              <div className="form-group">
                <label className="form-label">Schüler</label>
                <input
                  type="text"
                  className="form-input"
                  value={editingAssignment.studentName}
                  disabled
                />
              </div>
              <div className="form-group">
                <label className="form-label">Betreuer</label>
                <select
                  className="form-select"
                  value={editingAssignment.teacherId?.toString() || ''}
                  onChange={(e) => {
                    console.log('Teacher changed, new value:', e.target.value);
                    console.log('Available teachers:', teachers.map(t => ({ id: t.teacherId, name: `${t.firstName} ${t.lastName}` })));
                    const teacherId = e.target.value ? parseInt(e.target.value, 10) : null;
                    const teacher = teachers.find(t => t.teacherId?.toString() === e.target.value);
                    console.log('Parsed teacherId:', teacherId);
                    console.log('Found teacher:', teacher);
                    
                    // Automatically set school based on teacher
                    const updated = {
                      ...editingAssignment, 
                      teacherId,
                      teacherName: teacher ? `${teacher.firstName} ${teacher.lastName}` : null,
                      schoolId: teacher?.schoolId || null,
                      schoolName: teacher?.schoolName || null
                    };
                    console.log('Updating assignment to:', updated);
                    setEditingAssignment(updated);
                  }}
                >
                  <option value="">Kein Betreuer</option>
                  {teachers.length > 0 ? teachers.map((teacher, idx) => (
                    <option key={`teacher-${teacher.teacherId}-${idx}`} value={teacher.teacherId?.toString()}>
                      {teacher.firstName} {teacher.lastName}
                    </option>
                  )) : <option disabled>Loading...</option>}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Praktikumtyp</label>
                <input
                  type="text"
                  className="form-input"
                  value={editingAssignment.praktikumType}
                  disabled
                />
              </div>
              <div className="form-group">
                <label className="form-label">Schule</label>
                <input
                  type="text"
                  className="form-input"
                  value={editingAssignment.schoolName || '—'}
                  disabled
                />
              </div>
              <div className="form-group">
                <label className="form-label">Status</label>
                <select
                  className="form-select"
                  value={editingAssignment.status}
                  onChange={(e) => {
                    console.log('Status changed to:', e.target.value);
                    const updated = {...editingAssignment, status: e.target.value};
                    console.log('Updating assignment to:', updated);
                    setEditingAssignment(updated);
                  }}
                >
                  <option value="PROPOSED">Vorgeschlagen</option>
                  <option value="CANCELLED">Abgesagt</option>
                </select>
                  {validationResult && validationResult.hardValid === false && (
    <div className="error-container" style={{ marginBottom: "12px" }}>
      <strong>Nicht speicherbar:</strong>
      <ul style={{ margin: "8px 0 0 18px" }}>
        {validationResult.hardViolations?.map((v: any, i: number) => (
          <li key={i}>{v.message}</li>
        ))}
      </ul>
    </div>
  )}
              </div>
              
            </div>
            <div className="modal-footer">
              <button
                className="btn-cancel"
                onClick={handleCloseEditAssignmentModal}
              >
                Abbrechen
              </button>
              <button
                className="btn-primary"
                onClick={handleSaveEditAssignment}
                disabled={validationResult && validationResult.hardValid === false}

              >
                Speichern
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
