/* eslint-disable @typescript-eslint/no-unused-vars */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import studentConfigService from "../services/studentConfigService";
import plService from "../services/plService";
import studentService from "../services/studentService";
import schoolService from "../services/schoolService";
import internshipAssignmentService from "../services/internshipAssignmentService";
import SearchFilter, { type FilterConfig } from "../components/SearchFilter";
import StudentConfigForm from "../components/InternshipsAssignment/InternshipAssignmentStudentForm";
import TeacherConfigForm from "../components/InternshipsAssignment/TeacherConfigForm";
import EditPlannedInternshipForm from "../components/InternshipsAssignment/EditPlannedInternshipForm";
import type { StudentConfigDto } from "../services/studentConfigService";
import type { TeacherPlConfigDto, TeacherDto } from "../services/plService";
import { PraktikumType } from "../services/plService";
import type { Student } from "../services/studentService";
import type { TeacherAssignmentResult, PlannedInternshipDto, StudentAssignmentResult, AssignmentDto } from "../services/internshipAssignmentService";
import "../styles/InternshipsAssignment/InternshipAssignmentModal.css";
import "../styles/InternshipsAssignment/StudentConfigTable.css";
import ForceSaveModal from "../components/ForceSaveModal";

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
  const [showForceSaveAssignment, setShowForceSaveAssignment] = useState(false);

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
  const [editedAssignmentIds, setEditedAssignmentIds] = useState<Set<number>>(new Set());

  // Edit Planned Internship Modal State
  const [showEditInternshipModal, setShowEditInternshipModal] = useState(false);
  const [editingInternship, setEditingInternship] = useState<PlannedInternshipDto | null>(null);

  // Edit Student Assignment Modal State
  const [showEditAssignmentModal, setShowEditAssignmentModal] = useState(false);
  const [editingAssignment, setEditingAssignment] = useState<AssignmentDto | null>(null);

  const [studentSearchTerm, setStudentSearchTerm] = useState('');
  const [teacherSearchTerm, setTeacherSearchTerm] = useState('');
  const [teacherFilters, setTeacherFilters] = useState<Record<string, string>>({
    praktikumType: '',
    course: '',
    school: '',
    zone: '',
    schoolType: '',
    assignmentStatus: '',
  });
  const [studentFilters, setStudentFilters] = useState<Record<string, string>>(({
    praktikumType: '',
    course: '',
    teacher: '',
    school: '',
    status: '',
  }));
  const [studentConfigFilters, setStudentConfigFilters] = useState<Record<string, string>>({
    year: '',
    schoolType: '',
    mainCourse: '',
    praktikum: '',
  });
  const [plConfigFilters, setPlConfigFilters] = useState<Record<string, string>>({
    teacher: '',
    mainSubject: '',
    school: '',
    schoolZone: '',
    employmentType: '',
    praktikumPreference: '',
  });
  const [plConfigSearchTerm, setPlConfigSearchTerm] = useState('');
  const [timeBudget, setTimeBudget] = useState<number>(210);
  const [uncompletedInternships, setUncompletedInternships] = useState<number>(0);

  // New Year Modal State
  const [showNewYearModal, setShowNewYearModal] = useState(false);
  const [newYearInput, setNewYearInput] = useState('');
  const [newSemesterType, setNewSemesterType] = useState('WiSe');

  // Reoptimization state
  const [reoptimizing, setReoptimizing] = useState(false);
  const [copyingConfigs, setCopyingConfigs] = useState(false);
  const [showPreservedStudents, setShowPreservedStudents] = useState(false);
  const [reoptimizationResults, setReoptimizationResults] = useState<{
    studentsAssigned: number;
    totalDemands: number;
    assignmentsPreserved: number;
    preservedStudentNames?: string[];
    winterBudgetUsed: number;
    initialBudget: number;
    finalBudget: number;
    winterTotal?: number;
    summerTotal?: number;
  } | null>(null);

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
    
    // Add window focus listener to refresh years when returning to page
    const handleFocus = () => {
      refreshAvailableYears();
    };
    
    window.addEventListener('focus', handleFocus);
    
    return () => {
      window.removeEventListener('focus', handleFocus);
    };
  }, []);

  useEffect(() => {
    if (selectedYear) {
      fetchStudentConfigsByYear();
      fetchPlannedInternships(); // Load saved teacher assignments
      fetchStudentAssignments(); // Load saved student assignments
      
      // Initialize reoptimization results for summer semester
      if (selectedYear.startsWith('SoSe')) {
        setReoptimizationResults({
          studentsAssigned: 0,
          totalDemands: 0,
          assignmentsPreserved: 0,
          winterBudgetUsed: 0,
          initialBudget: 0,
          finalBudget: 0
        });
      } else {
        // Clear reoptimization results when changing to winter
        setReoptimizationResults(null);
      }
    }
  }, [selectedYear]);

  // Calculate and display preservation for summer semester
  const calculateAndDisplayPreservation = async () => {
    if (!selectedYear || !selectedYear.startsWith('SoSe')) return;
    
    try {
      const winterYear = selectedYear.replace('SoSe', 'WiSe');
      
      // Fetch STUDENT ASSIGNMENTS (Phase 2), not planned internships
      const [winterAssignments, summerAssignments] = await Promise.all([
        internshipAssignmentService.getStudentAssignments(winterYear),
        internshipAssignmentService.getStudentAssignments(selectedYear)
      ]);
      
      // Calculate preservation: how many unique students kept assignments
      const { preserved, preservedStudentNames } = calculateStudentAssignmentPreservation(winterAssignments, summerAssignments);
      
      // Always set results to show preservation
      setReoptimizationResults({
        studentsAssigned: summerAssignments.length,
        totalDemands: summerAssignments.length,
        assignmentsPreserved: preserved,
        preservedStudentNames: preservedStudentNames,
        winterBudgetUsed: 0,
        initialBudget: summerAssignments.length, // Use total summer assignments as denominator
        finalBudget: summerAssignments.length,
        winterTotal: winterAssignments.length,
        summerTotal: summerAssignments.length
      });
    } catch (err) {
      // Set default values even on error
      setReoptimizationResults({
        studentsAssigned: 0,
        totalDemands: 0,
        assignmentsPreserved: 0,
        winterBudgetUsed: 0,
        initialBudget: 0,
        finalBudget: 0,
        winterTotal: 0,
        summerTotal: 0
      });
    }
  };
  
  // Calculate student assignment preservation between winter and summer
  const calculateStudentAssignmentPreservation = (
    winterAssignments: AssignmentDto[],
    summerAssignments: AssignmentDto[]
  ): { preserved: number; preservedStudentNames: string[] } => {
    // Build map of winter assignments by key: student/teacher/course/praktikumType
    const winterAssignmentKeys = new Map<string, AssignmentDto>();
    winterAssignments.forEach(assignment => {
      if (assignment.studentName && assignment.teacherName) {
        // Include praktikumType and handle cases where course might be null
        const courseKey = assignment.course || assignment.praktikumType;
        const key = `${assignment.studentName}/${assignment.teacherName}/${courseKey}`;
        winterAssignmentKeys.set(key, assignment);
      }
    });
    
    console.log('Winter assignments total:', winterAssignments.length);
    console.log('Winter keys created:', winterAssignmentKeys.size);
    console.log('Summer assignments total:', summerAssignments.length);
    
    // For each summer assignment, check if exact match exists in winter
    let preserved = 0;
    const preservedStudentNames: string[] = [];
    summerAssignments.forEach((assignment, idx) => {
      if (assignment.studentName && assignment.teacherName) {
        const courseKey = assignment.course || assignment.praktikumType;
        const key = `${assignment.studentName}/${assignment.teacherName}/${courseKey}`;
        if (winterAssignmentKeys.has(key)) {
          preserved++;
          // Add student name with praktikum type in parentheses
          preservedStudentNames.push(`${assignment.studentName} (${assignment.praktikumType})`);
        } else if (idx < 5) {
          console.log(`Not preserved [${idx}]:`, key);
        }
      }
    });
    
    console.log('Preserved:', preserved, 'out of', summerAssignments.length);
    return { preserved, preservedStudentNames };
  };

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
    // Filter to show only active teachers in pl-config
    setTeachers(teachersData.filter(t => t.active !== false));
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

// Refresh available years from database
const refreshAvailableYears = async (yearToKeep?: string) => {
  try {
    const yearsData = await studentConfigService.getAllYears();
    setAvailableYears(yearsData);
    
    // If a specific year should be kept, ensure it's selected
    if (yearToKeep && yearsData.includes(yearToKeep)) {
      setSelectedYear(yearToKeep);
      return;
    }
    
    // Otherwise, if current selected year is no longer available, select first available
    if (selectedYear && !yearsData.includes(selectedYear)) {
      if (yearsData.length > 0) {
        setSelectedYear(yearsData[0]);
      } else {
        setSelectedYear('');
      }
    }
  } catch (err) {
    console.error('Failed to refresh years:', err);
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
      setSuccess("Studentenkonfiguration erfolgreich gelöscht!");
      fetchStudentConfigsByYear();
      
      // Refresh years list in case the last config for a year was deleted
      // Small delay to ensure database transaction completes
      setTimeout(async () => {
        await refreshAvailableYears();
      }, 150);
      
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
    setSuccess("Lehrerkonfiguration erfolgreich gelöscht!");
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
      setError("Bitte wählen Sie zuerst ein Schuljahr aus");
      setTimeout(() => setError(null), 3000);
      return;
    }

    setAssigningPhase1(true);
    
    try {
      const result = await internshipAssignmentService.optimizePhase1(selectedYear, timeBudget * 2);
      setSuccess(`Phase 1 Optimierung abgeschlossen! ${result.assignedCount}/${result.totalPlannedInternships} Praktika zugewiesen.`);
      setTimeout(() => setSuccess(null), 5000);
      
      // Fetch saved results from database
      await fetchPlannedInternships();
      setShowTeacherAssignments(true);
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : "Optimierung fehlgeschlagen";
      setError(errorMsg);
      setTimeout(() => setError(null), 5000);
    } finally {
      setAssigningPhase1(false);
    }
  };

  // Fetch planned internships from database
  const fetchPlannedInternships = async () => {
    if (!selectedYear) return;
    
    console.log('Fetching planned internships for:', selectedYear);
    
    try {
      const internships = await internshipAssignmentService.getPlannedInternships(selectedYear);
      console.log('Fetched internships:', internships.length, internships);
      
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
      
      // Calculate preservation if this is a summer semester
      if (selectedYear.startsWith('SoSe')) {
        console.log('Summer semester detected, calculating preservation...');
        await calculateAndDisplayPreservation();
      }
    } catch (err) {
      console.error("Failed to fetch planned internships:", err);
    }
  };

  // Delete all assignments for current year
  const handleDeleteAllAssignments = async () => {
    if (!selectedYear) return;
    
    if (!confirm(`Alle Lehrerzuweisungen für ${selectedYear} löschen?`)) {
      return;
    }

    try {
      await internshipAssignmentService.deleteAllPlannedInternships(selectedYear);
      setPhase1Result(null);
      setShowTeacherAssignments(false);
      setSuccess("Alle Zuweisungen erfolgreich gelöscht!");
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete assignments");
      setTimeout(() => setError(null), 3000);
    }
  };

  // Phase 2: Student Assignment Optimization
  const handleAssignPhase2 = async () => {
    if (!selectedYear) {
      setError("Bitte wählen Sie ein Schuljahr aus");
      setTimeout(() => setError(null), 3000);
      return;
    }

    setAssigningPhase2(true);
    
    try {
      const result = await internshipAssignmentService.optimizePhase2(selectedYear);
      setPhase2Result(result);
      setStudentAssignments(result.assignments);
      setSuccess(`Phase 2 Optimierung abgeschlossen! ${result.assignedStudents}/${result.totalStudents} Studierende zugewiesen.`);
      setTimeout(() => setSuccess(null), 5000);
      setShowStudentAssignments(true);
    } catch (err: any) {
      console.error("Phase 2 optimization error:", err);
      let errorMsg = "Phase 2 Optimierung fehlgeschlagen";
      
      if (err.status === 401 || err.status === 403) {
        errorMsg = "Authentifizierungsfehler: Bitte erneut anmelden";
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
    
    if (!confirm(`Alle Studentenzuweisungen für ${selectedYear} löschen?`)) {
      return;
    }

    try {
      await internshipAssignmentService.deleteAllStudentAssignments(selectedYear);
      setPhase2Result(null);
      setStudentAssignments([]);
      setShowStudentAssignments(false);
      setSuccess("Alle Studentenzuweisungen erfolgreich gelöscht!");
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
    setSuccess("Zuweisung erfolgreich aktualisiert!");
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

  const handleSaveEditAssignment = async (force = false) => {
  if (!editingAssignment) return;
    // CANCELLED should always be possible: do status-only update, no validation flow
  if (editingAssignment.status === 'CANCELLED') {
    try {
      await internshipAssignmentService.updateAssignmentStatus(
        editingAssignment.id,
        'CANCELLED'
      );

      // Make sure cancelled assignments are no longer treated as "edited"
      const newEditedIds = new Set(editedAssignmentIds);
      newEditedIds.delete(editingAssignment.id);
      setEditedAssignmentIds(newEditedIds);

      await fetchStudentAssignments();
      setSuccess("Studentenzuweisung erfolgreich abgesagt!");
      setTimeout(() => setSuccess(null), 3000);
      handleCloseEditAssignmentModal();
    } catch (err: any) {
      setError(err?.message || "Zuweisung konnte nicht abgesagt werden");
      setTimeout(() => setError(null), 3000);
    }
    return;
  }

  // If user is not forcing and we already have a hard violation stored -> show force dialog
  if (!force && validationResult && validationResult.hardValid === false) {
    setShowForceSaveAssignment(true);
    return;
  }

  console.log("=== SAVING ASSIGNMENT ===");
  console.log("Full assignment object:", editingAssignment);
  console.log("Saving assignment with data:", {
    id: editingAssignment.id,
    teacherId: editingAssignment.teacherId,
    schoolId: editingAssignment.schoolId,
    status: editingAssignment.status,
  });

  try {
    const originalAssignment = studentAssignments.find(
      (a) => a.id === editingAssignment.id
    );

    const teacherChanged =
      !!originalAssignment &&
      originalAssignment.teacherId !== editingAssignment.teacherId;
    const schoolChanged =
      !!originalAssignment &&
      originalAssignment.schoolId !== editingAssignment.schoolId;
    const statusChanged =
      !!originalAssignment &&
      originalAssignment.status !== editingAssignment.status;

    // 1) PATCH status first (reliable)
    console.log("Updating status to:", editingAssignment.status);
    await internshipAssignmentService.updateAssignmentStatus(
      editingAssignment.id,
      editingAssignment.status
    );

    // 2) If teacher/school changed, attempt PUT update (backend might ignore teacher/school,
    //    but status was already updated via PATCH)
    if (originalAssignment && (teacherChanged || schoolChanged)) {
      const updatePayload = {
        teacherId: editingAssignment.teacherId || null,
        schoolId: editingAssignment.schoolId || null,
        status: editingAssignment.status,
      };
      console.log("Teacher or school changed, attempting full update with payload:", updatePayload);
      await internshipAssignmentService.updateStudentAssignment(
        editingAssignment.id,
        updatePayload,
        { force }
      );
    }

    // 3) Update local UI immediately (so status changes are visible without refresh)
    //    This avoids waiting for fetchStudentAssignments to reflect the change.
    setStudentAssignments((prev) =>
      prev.map((a) =>
        a.id === editingAssignment.id
          ? {
              ...a,
              teacherId: editingAssignment.teacherId,
              schoolId: editingAssignment.schoolId,
              status: editingAssignment.status,
            }
          : a
      )
    );

    // 4) IMPORTANT: Do NOT mark CANCELLED/CONFIRMED as "Edited"
    //    Otherwise your badge logic shows "Edited" instead of "Cancelled".
    const newEditedIds = new Set(editedAssignmentIds);
    const isCancelled = editingAssignment.status === "CANCELLED";
    const isConfirmed = editingAssignment.status === "CONFIRMED";

    const shouldMarkEdited =
      (teacherChanged || schoolChanged || statusChanged) && !isCancelled && !isConfirmed;

    if (shouldMarkEdited) newEditedIds.add(editingAssignment.id);
    else newEditedIds.delete(editingAssignment.id);

    setEditedAssignmentIds(newEditedIds);

    // 5) Clear validation state after a successful save
    setValidationResult(null);
    setShowForceSaveAssignment(false);

    // 6) Refresh from backend (kept as in your code)
    await fetchStudentAssignments();
    console.log("Student assignments refreshed");

    setSuccess("Studentenzuweisung erfolgreich aktualisiert!");
    setTimeout(() => setSuccess(null), 3000);
    handleCloseEditAssignmentModal();
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (err: any) {
    console.error("Update error:", err);
    console.error("Error details:", {
      status: err?.status,
      message: err?.message,
      response: err?.response,
      data: err?.data,
      hardViolations: err?.hardViolations,
      softViolations: err?.softViolations
    });

    // backend validation (AssignmentValidationException -> ValidationResult) comes here
    if (err?.status === 400 && err?.hardViolations) {
      setValidationResult(err); // store full ValidationResult
      setError(null); // don't show generic "Error"
      setShowForceSaveAssignment(true);
      return; // keep modal open
    }

    // fallback generic error
    setValidationResult(null);
    setShowForceSaveAssignment(false);
    const errorMessage = err?.message || err?.response?.data?.message || err?.data?.message || "Failed to update assignment";
    setError(errorMessage);
    setTimeout(() => setError(null), 5000);
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
      // Remove from edited list when confirmed
      const newEditedIds = new Set(editedAssignmentIds);
      newEditedIds.delete(assignmentId);
      setEditedAssignmentIds(newEditedIds);
      
      const result = await internshipAssignmentService.updateAssignmentStatus(assignmentId, 'CONFIRMED');
      console.log('Validation result:', result);
      await fetchStudentAssignments();
      console.log('Student assignments refreshed');
      setSuccess("Zuweisung erfolgreich bestätigt!");
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      console.error('Validation error:', err);
      setError(err instanceof Error ? err.message : "Failed to validate assignment");
      setTimeout(() => setError(null), 3000);
    }
  };

  // New Year Handler
  const handleCreateNewYear = async () => {
    // Refresh the years list to show any configs added manually
    await refreshAvailableYears();
    setShowNewYearModal(true);
  };

const handleConfirmNewYear = () => {
  if (!newYearInput.trim()) {
    setError("Please enter a valid year");
    setTimeout(() => setError(null), 3000);
    return;
  }

  // Create year format with selected semester type
  const fullYear = `${newSemesterType}${newYearInput.trim()}`;

  if (availableYears.includes(fullYear)) {
    setError("This year already exists");
    setTimeout(() => setError(null), 3000);
    return;
  }

  // Add the new year to the list and select it
  const updatedYears = [...availableYears, fullYear].sort();
  setAvailableYears(updatedYears);
  setSelectedYear(fullYear);
  setStudentConfigs([]); // Clear configs for new year
  
  setShowNewYearModal(false);
  setNewYearInput('');
  setNewSemesterType('WiSe');
  setSuccess(`Neues Planungsjahr "${newYearInput.trim()}" erfolgreich erstellt!`);
  setTimeout(() => setSuccess(null), 3000);
};

  // Reoptimization Handler
  const handleReoptimization = async () => {
    if (!selectedYear) {
      setError("Please select a year first");
      setTimeout(() => setError(null), 3000);
      return;
    }

    try {
      setReoptimizing(true);
      
      // Fetch winter student assignments BEFORE reoptimization
      const winterYear = selectedYear.replace('SoSe', 'WiSe');
      const winterAssignments = await internshipAssignmentService.getStudentAssignments(winterYear);
      
      const response = await fetch('http://localhost:8080/api/reoptimization/optimize', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${sessionStorage.getItem('token')}`
        },
        body: JSON.stringify({ 
          schoolYear: selectedYear,
          timeBudget: timeBudget,
          uncompletedInternships: uncompletedInternships
        })
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Reoptimization failed' }));
        throw new Error(errorData.message || 'Reoptimization failed');
      }

      const result = await response.json();
      
      // Refresh Phase 1 and Phase 2 assignments
      await fetchPlannedInternships();
      await fetchStudentAssignments();
      
      // Calculate preservation in frontend using student assignments
      const summerAssignments = await internshipAssignmentService.getStudentAssignments(selectedYear);
      const { preserved, preservedStudentNames } = calculateStudentAssignmentPreservation(winterAssignments, summerAssignments);
      
      // Store results persistently with frontend-calculated preservation
      setReoptimizationResults({
        studentsAssigned: result.studentsAssigned,
        totalDemands: result.totalDemands,
        assignmentsPreserved: preserved,
        preservedStudentNames: preservedStudentNames,
        winterBudgetUsed: result.winterBudgetUsed,
        initialBudget: summerAssignments.length, // Use total summer assignments as denominator
        finalBudget: summerAssignments.length,
        winterTotal: winterAssignments.length,
        summerTotal: summerAssignments.length
      });
      
      setSuccess(`Reoptimization completed successfully! Students: ${result.studentsAssigned}/${result.totalDemands}`);
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Reoptimization failed");
      setTimeout(() => setError(null), 5000);
    } finally {
      setReoptimizing(false);
    }
  };

  // Calculate teacher assignment preservation between winter and summer

  // Copy Semester Configs Handler
  const handleCopySemesterConfigs = async () => {
    if (!selectedYear) {
      setError("Please select a year first");
      setTimeout(() => setError(null), 3000);
      return;
    }

    // Determine source and target years
    let sourceYear = '';
    let targetYear = '';
    
    if (selectedYear.startsWith('SoSe')) {
      // If summer semester is selected, copy from previous winter
      const year = selectedYear.replace('SoSe', '');
      sourceYear = `WiSe${year}`;
      targetYear = selectedYear;
    } else {
      setError("Semester config copy is only available for summer semesters (SoSe)");
      setTimeout(() => setError(null), 3000);
      return;
    }

    try {
      setCopyingConfigs(true);
      const response = await fetch(
        `http://localhost:8080/api/semester-config/copy-to-next-semester?sourceYear=${sourceYear}&targetYear=${targetYear}`,
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${sessionStorage.getItem('token')}`
          }
        }
      );

      if (!response.ok) {
        throw new Error('Failed to copy semester configs');
      }

      const result = await response.json();
      setSuccess(
        `Configs copied successfully! Teachers: ${result.teacherConfigsCopied}, Students: ${result.studentConfigsCopied}`
      );
      
      // Refresh student configs and teacher data without changing selected year
      await fetchStudentConfigsByYear();
      const teachersData = await plService.getAllPls();
      setTeachers(teachersData.filter(t => t.active !== false));
      
      setTimeout(() => setSuccess(null), 5000);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to copy semester configs");
      setTimeout(() => setError(null), 3000);
    } finally {
      setCopyingConfigs(false);
    }
  };

  // Teacher assignments filter configuration
  const filteredTeacherAssignments = phase1Result?.plannedInternships.filter(pi => {
    const matchesSearch = !teacherSearchTerm || 
                         (pi.teacherName?.toLowerCase().includes(teacherSearchTerm.toLowerCase()) ?? false) ||
                         (pi.course?.toLowerCase().includes(teacherSearchTerm.toLowerCase()) ?? false);
    const matchesPraktikumType = !teacherFilters.praktikumType || pi.praktikumType === teacherFilters.praktikumType;
    const matchesCourse = !teacherFilters.course || pi.course === teacherFilters.course;
    const matchesSchool = !teacherFilters.school || pi.schoolName === teacherFilters.school;
    const matchesZone = !teacherFilters.zone || pi.schoolZone === teacherFilters.zone;
    const matchesSchoolType = !teacherFilters.schoolType || pi.schoolType === teacherFilters.schoolType;
    const matchesAssignmentStatus = !teacherFilters.assignmentStatus ||
                                   (teacherFilters.assignmentStatus === 'assigned' && pi.teacherName) ||
                                   (teacherFilters.assignmentStatus === 'unassigned' && !pi.teacherName);
    
    return matchesSearch && matchesPraktikumType && matchesCourse && matchesSchool && 
           matchesZone && matchesSchoolType && matchesAssignmentStatus;
  }) ?? [];

  // Get unique values for teacher filters
  const uniquePraktikumTypes = [...new Set(phase1Result?.plannedInternships.map(pi => pi.praktikumType).filter(Boolean))].sort();
  const uniqueCourses = [...new Set(phase1Result?.plannedInternships.map(pi => pi.course).filter((c): c is string => !!c))].sort();
  const uniqueSchools = [...new Set(phase1Result?.plannedInternships.map(pi => pi.schoolName).filter((s): s is string => !!s))].sort();
  const uniqueZones = [...new Set(phase1Result?.plannedInternships.map(pi => pi.schoolZone).filter((z): z is string => !!z))].sort();
  const uniqueSchoolTypes = [...new Set(phase1Result?.plannedInternships.map(pi => pi.schoolType).filter(Boolean))].sort();

  const teacherFilterConfigs: FilterConfig[] = [
    {
      field: 'praktikumType',
      label: 'Praktikumtyp',
      options: uniquePraktikumTypes.map(type => ({ label: type, value: type, field: 'praktikumType' }))
    },
    {
      field: 'course',
      label: 'Kurs',
      options: uniqueCourses.map(course => ({ 
        label: course.charAt(0).toUpperCase() + course.slice(1), 
        value: course, 
        field: 'course' 
      }))
    },
    {
      field: 'school',
      label: 'Schule',
      options: uniqueSchools.map(school => ({ label: school, value: school, field: 'school' }))
    },
    {
      field: 'zone',
      label: 'Zone',
      options: uniqueZones.map(zone => ({ label: zone, value: zone, field: 'zone' }))
    },
    {
      field: 'schoolType',
      label: 'Schultyp',
      options: uniqueSchoolTypes.map(type => ({ label: type, value: type, field: 'schoolType' }))
    },
    {
      field: 'assignmentStatus',
      label: 'Zuweisungsstatus',
      options: [
        { label: 'Zugewiesen', value: 'assigned', field: 'assignmentStatus' },
        { label: 'Nicht zugewiesen', value: 'unassigned', field: 'assignmentStatus' }
      ]
    }
  ];

  const handleTeacherFilterChange = (field: string, value: string) => {
    setTeacherFilters(prev => ({ ...prev, [field]: value }));
  };

  const handleClearTeacherFilters = () => {
    setTeacherFilters({
      praktikumType: '',
      course: '',
      school: '',
      zone: '',
      schoolType: '',
      assignmentStatus: '',
    });
  };

  // Student assignments filter configuration
  const filteredStudentAssignments = studentAssignments.filter(assignment => {
    const matchesSearch = !studentSearchTerm || 
                         assignment.studentName.toLowerCase().includes(studentSearchTerm.toLowerCase()) ||
                         (assignment.teacherName?.toLowerCase().includes(studentSearchTerm.toLowerCase()) ?? false) ||
                         (assignment.course?.toLowerCase().includes(studentSearchTerm.toLowerCase()) ?? false);
    const matchesPraktikumType = !studentFilters.praktikumType || assignment.praktikumType === studentFilters.praktikumType;
    const matchesCourse = !studentFilters.course || assignment.course === studentFilters.course;
    const matchesTeacher = !studentFilters.teacher || assignment.teacherName === studentFilters.teacher;
    const matchesSchool = !studentFilters.school || assignment.schoolName === studentFilters.school;
    const matchesStatus = !studentFilters.status || 
                         (studentFilters.status === 'EDITED' && editedAssignmentIds.has(assignment.id)) ||
                         assignment.status === studentFilters.status;
    
    return matchesSearch && matchesPraktikumType && matchesCourse && matchesTeacher && 
           matchesSchool && matchesStatus;
  });

  // Get unique values for student filters
  const uniqueStudentPraktikumTypes = [...new Set(studentAssignments.map(a => a.praktikumType).filter(Boolean))].sort();
  const uniqueStudentCourses = [...new Set(studentAssignments.map(a => a.course).filter((c): c is string => !!c))].sort();
  const uniqueTeachers = [...new Set(studentAssignments.map(a => a.teacherName).filter((t): t is string => !!t))].sort();
  const uniqueStudentSchools = [...new Set(studentAssignments.map(a => a.schoolName).filter((s): s is string => !!s))].sort();

  const studentFilterConfigs: FilterConfig[] = [
    {
      field: 'praktikumType',
      label: 'Praktikumtyp',
      options: uniqueStudentPraktikumTypes.map(type => ({ label: type, value: type, field: 'praktikumType' }))
    },
    {
      field: 'course',
      label: 'Kurs',
      options: uniqueStudentCourses.map(course => ({ 
        label: course.charAt(0).toUpperCase() + course.slice(1), 
        value: course, 
        field: 'course' 
      }))
    },
    {
      field: 'teacher',
      label: 'Betreuer',
      options: uniqueTeachers.map(teacher => ({ label: teacher, value: teacher, field: 'teacher' }))
    },
    {
      field: 'school',
      label: 'Schule',
      options: uniqueStudentSchools.map(school => ({ label: school, value: school, field: 'school' }))
    },
    {
      field: 'status',
      label: 'Status',
      options: [
        { label: 'Vorgeschlagen', value: 'PROPOSED', field: 'status' },
        { label: 'Bearbeitet', value: 'EDITED', field: 'status' },
        { label: 'Bestätigt', value: 'CONFIRMED', field: 'status' },
        { label: 'Abgesagt', value: 'CANCELLED', field: 'status' }
      ]
    }
  ];

  const handleStudentFilterChange = (field: string, value: string) => {
    setStudentFilters(prev => ({ ...prev, [field]: value }));
  };

  const handleClearStudentFilters = () => {
    setStudentFilters({
      praktikumType: '',
      course: '',
      teacher: '',
      school: '',
      status: '',
    });
  };

  // Student configuration filter configuration
  const filteredStudentConfigs = studentConfigs.filter(config => {
    const student = students.find(s => s.matriculationNbr === config.studentId);
    if (!student) return false;
    
    const fullName = `${student.firstName} ${student.lastName}`.toLowerCase();
    const matchesSearch = !studentSearchTerm || fullName.includes(studentSearchTerm.toLowerCase());
    const matchesYear = !studentConfigFilters.year || config.year === studentConfigFilters.year;
    const matchesSchoolType = !studentConfigFilters.schoolType || config.schoolType === studentConfigFilters.schoolType;
    const matchesMainCourse = !studentConfigFilters.mainCourse || config.mainCourse?.name === studentConfigFilters.mainCourse;
    const matchesPraktikum = !studentConfigFilters.praktikum ||
                            (studentConfigFilters.praktikum === 'PDP I' && config.pdpI) ||
                            (studentConfigFilters.praktikum === 'PDP II' && config.pdpII) ||
                            (studentConfigFilters.praktikum === 'ZSP' && config.zsp) ||
                            (studentConfigFilters.praktikum === 'SFP' && config.sfp);
    
    return matchesSearch && matchesYear && matchesSchoolType && matchesMainCourse && matchesPraktikum;
  });

  // Get unique values for student config filters
  const uniqueConfigYears = [...new Set(studentConfigs.map(c => c.year).filter(Boolean))].sort();
  const uniqueConfigSchoolTypes = [...new Set(studentConfigs.map(c => c.schoolType).filter(Boolean))].sort();
  const uniqueConfigMainCourses = [...new Set(studentConfigs.map(c => c.mainCourse?.name).filter((c): c is string => !!c))].sort();
  const statusLabelDE = (status?: string) => {
  switch (status) {
    case "CANCELLED":
      return "Abgesagt";
    case "CONFIRMED":
      return "Bestätigt";
    case "PROPOSED":
      return "Vorgeschlagen";
    case "PLANNED":
      return "Geplant";
    case "DONE":
      return "Abgeschlossen";
    default:
      return status ?? "-";
  }
};

  const studentConfigFilterConfigs: FilterConfig[] = [
    {
      field: 'year',
      label: 'Jahr',
      options: uniqueConfigYears.map(year => ({ label: year, value: year, field: 'year' }))
    },
    {
      field: 'schoolType',
      label: 'Schultyp',
      options: uniqueConfigSchoolTypes.map(type => ({ label: type, value: type, field: 'schoolType' }))
    },
    {
      field: 'mainCourse',
      label: 'Hauptfach',
      options: uniqueConfigMainCourses.map(course => ({ 
        label: course.charAt(0).toUpperCase() + course.slice(1), 
        value: course, 
        field: 'mainCourse' 
      }))
    },
    {
      field: 'praktikum',
      label: 'Praktikum',
      options: [
        { label: 'PDP I', value: 'PDP I', field: 'praktikum' },
        { label: 'PDP II', value: 'PDP II', field: 'praktikum' },
        { label: 'ZSP', value: 'ZSP', field: 'praktikum' },
        { label: 'SFP', value: 'SFP', field: 'praktikum' }
      ]
    }
  ];

  const handleStudentConfigFilterChange = (field: string, value: string) => {
    setStudentConfigFilters(prev => ({ ...prev, [field]: value }));
  };

  const handleClearStudentConfigFilters = () => {
    setStudentConfigFilters({
      year: '',
      schoolType: '',
      mainCourse: '',
      praktikum: '',
    });
  };

  // PL Configuration filter logic
  const filteredPlConfigTeachers = teachers.filter(teacher => {
    const fullName = `${teacher.firstName} ${teacher.lastName}`.toLowerCase();
    const matchesSearch = !plConfigSearchTerm || fullName.includes(plConfigSearchTerm.toLowerCase());
    const matchesTeacher = !plConfigFilters.teacher || fullName.includes(plConfigFilters.teacher.toLowerCase());
    const matchesMainSubject = !plConfigFilters.mainSubject || teacher.mainSubject.name === plConfigFilters.mainSubject;
    const matchesSchool = !plConfigFilters.school || teacher.schoolName === plConfigFilters.school;
    const matchesSchoolZone = !plConfigFilters.schoolZone || teacher.schoolZone === plConfigFilters.schoolZone;
    const matchesEmploymentType = !plConfigFilters.employmentType || 
      (plConfigFilters.employmentType === 'Vollzeit' && !teacher.isPartTime) ||
      (plConfigFilters.employmentType === 'Teilzeit' && teacher.isPartTime);
    
    // Check if teacher has configs matching the praktikum preference filter
    const matchesPraktikumPreference = !plConfigFilters.praktikumPreference || 
      teacher.plConfigs.some(config => 
        config.schoolYear === selectedYear && 
        config.internshipPreferences.includes(plConfigFilters.praktikumPreference as PraktikumType)
      );
    
    return matchesSearch && matchesTeacher && matchesMainSubject && matchesSchool && 
           matchesSchoolZone && matchesEmploymentType && matchesPraktikumPreference;
  });

  // Get unique values for PL config filters
  const uniquePlConfigMainSubjects = [...new Set(teachers.map(t => t.mainSubject.name).filter((name): name is string => !!name))].sort();
  const uniquePlConfigSchools = [...new Set(teachers.map(t => t.schoolName).filter((name): name is string => !!name))].sort();
  const uniquePlConfigZones = [...new Set(teachers.map(t => t.schoolZone).filter((zone): zone is string => !!zone))].sort();

  const plConfigFilterConfigs: FilterConfig[] = [
    {
      field: 'mainSubject',
      label: 'Hauptfach',
      options: uniquePlConfigMainSubjects.map(subject => ({ label: subject, value: subject, field: 'mainSubject' }))
    },
    {
      field: 'school',
      label: 'Schule',
      options: uniquePlConfigSchools.map(school => ({ label: school, value: school, field: 'school' }))
    },
    {
      field: 'schoolZone',
      label: 'Zone',
      options: uniquePlConfigZones.map(zone => ({ label: zone, value: zone, field: 'schoolZone' }))
    },
    {
      field: 'employmentType',
      label: 'Beschäftigung',
      options: [
        { label: 'Vollzeit', value: 'Vollzeit', field: 'employmentType' },
        { label: 'Teilzeit', value: 'Teilzeit', field: 'employmentType' }
      ]
    },
    {
      field: 'praktikumPreference',
      label: 'Praktikumspräferenz',
      options: [
        { label: 'PDP I', value: 'PDP I', field: 'praktikumPreference' },
        { label: 'PDP II', value: 'PDP II', field: 'praktikumPreference' },
        { label: 'ZSP', value: 'ZSP', field: 'praktikumPreference' },
        { label: 'SFP', value: 'SFP', field: 'praktikumPreference' }
      ]
    }
  ];

  const handlePlConfigFilterChange = (field: string, value: string) => {
    setPlConfigFilters(prev => ({ ...prev, [field]: value }));
  };

  const handleClearPlConfigFilters = () => {
    setPlConfigFilters({
      teacher: '',
      mainSubject: '',
      school: '',
      schoolZone: '',
      employmentType: '',
      praktikumPreference: '',
    });
    setPlConfigSearchTerm('');
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
          {selectedYear?.startsWith('SoSe') && (
            <button 
              className="btn-primary-filled"
              onClick={handleCopySemesterConfigs}
              disabled={copyingConfigs}
              style={{whiteSpace: 'nowrap'}}
            >
              {copyingConfigs ? "Kopieren läuft..." : "kopieren"}
            </button>
          )}
          <button 
            className="btn-primary-filled"
            onClick={handleCreateNewYear}
            style={{whiteSpace: 'nowrap'}}
          >
            Neues Jahr
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
              <div className="header-actions" style={{display: 'flex', alignItems: 'center', gap: '12px'}}>
                <div style={{display: 'flex', flexDirection: 'column', gap: '4px'}}>
                  <label style={{fontSize: '0.875rem', fontWeight: '500', color: '#374151'}}>Zeitbudget</label>
                  <input
                    type="number"
                    min="1"
                    max="1000"
                    value={timeBudget}
                    onChange={(e) => setTimeBudget(parseInt(e.target.value) || 210)}
                    style={{
                      width: '100px',
                      padding: '8px 12px',
                      fontSize: '0.875rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '6px',
                      outline: 'none',
                      backgroundColor: '#ffffff',
                      color: '#111827',
                      textAlign: 'center'
                    }}
                    onFocus={(e) => {
                      e.target.style.borderColor = '#3b82f6';
                      e.target.style.outline = '2px solid rgba(59, 130, 246, 0.1)';
                    }}
                    onBlur={(e) => {
                      e.target.style.borderColor = '#d1d5db';
                      e.target.style.outline = 'none';
                    }}
                  />
                </div>
                {selectedYear?.startsWith('SoSe') && (
                  <div style={{display: 'flex', flexDirection: 'column', gap: '4px'}}>
                    <label style={{fontSize: '0.875rem', fontWeight: '500', color: '#374151'}}>Uncompleted</label>
                    <input
                      type="number"
                      min="0"
                      max="1000"
                      value={uncompletedInternships}
                      onChange={(e) => setUncompletedInternships(parseInt(e.target.value) || 0)}
                      style={{
                        width: '100px',
                        padding: '8px 12px',
                        fontSize: '0.875rem',
                        border: '1px solid #d1d5db',
                        borderRadius: '6px',
                        outline: 'none',
                        backgroundColor: '#ffffff',
                        color: '#111827',
                        textAlign: 'center'
                      }}
                      onFocus={(e) => {
                        e.target.style.borderColor = '#3b82f6';
                        e.target.style.outline = '2px solid rgba(59, 130, 246, 0.1)';
                      }}
                      onBlur={(e) => {
                        e.target.style.borderColor = '#d1d5db';
                        e.target.style.outline = 'none';
                      }}
                    />
                  </div>
                )}
                <div style={{display: 'flex', gap: '12px', marginTop: '20px'}}>
                  {!selectedYear?.startsWith('SoSe') && (
                    <button 
                      className="btn-primary"
                      onClick={handlePhase1Assignment}
                      disabled={!selectedYear || assigningPhase1}
                      onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                      onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                    >
                      {assigningPhase1 ? "Optimizing..." : "Zuweisen"}
                    </button>
                  )}
                  {selectedYear?.startsWith('SoSe') && (
                    <button 
                      className="btn-primary"
                      onClick={handleReoptimization}
                      disabled={!selectedYear || reoptimizing}
                      onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                      onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                    >
                      {reoptimizing ? "Reoptimierung" : "Reoptimieren"}
                    </button>
                  )}
                </div>
              </div>
            </div>

            {/* Reoptimization Progress Bar */}
            {reoptimizing && (
              <div className="optimization-progress-container">
                <span className="optimization-progress-text">⚙️ Optimierung läuft...</span>
                <div className="progress-bar-wrapper">
                  <div className="progress-bar-animated"></div>
                </div>
              </div>
            )}

            {/* Reoptimization Results Panel */}
            {reoptimizationResults && selectedYear?.startsWith('SoSe') && (
              <div style={{
                marginBottom: '16px',
                padding: '12px 16px',
                backgroundColor: '#f0f9ff',
                border: '1px solid #bae6fd',
                borderRadius: '8px'
              }}>
                <div style={{
                  display: 'flex',
                  gap: '24px',
                  fontSize: '0.9em',
                  flexWrap: 'wrap',
                  alignItems: 'center'
                }}>
                  <div style={{display: 'flex', alignItems: 'center', gap: '8px'}}>
                    {reoptimizationResults.preservedStudentNames && reoptimizationResults.preservedStudentNames.length > 0 && (
                      <button
                        onClick={() => setShowPreservedStudents(!showPreservedStudents)}
                        style={{
                          background: 'none',
                          border: 'none',
                          outline: 'none',
                          cursor: 'pointer',
                          padding: '4px',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          color: '#3b82f6',
                          borderRadius: '3px',
                          transition: 'all 0.2s',
                          backgroundColor: showPreservedStudents ? '#dbeafe' : 'transparent'
                        }}
                        title={showPreservedStudents ? 'Ausblenden' : 'Anzeigen'}
                        onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#dbeafe'}
                        onMouseLeave={(e) => e.currentTarget.style.backgroundColor = showPreservedStudents ? '#dbeafe' : 'transparent'}
                      >
                        <svg 
                          width="16" 
                          height="16" 
                          viewBox="0 0 16 16" 
                          fill="none" 
                          style={{
                            transition: 'transform 0.2s',
                            transform: showPreservedStudents ? 'rotate(90deg)' : 'rotate(0deg)'
                          }}
                        >
                          <path 
                            d="M6 4L10 8L6 12" 
                            stroke="currentColor" 
                            strokeWidth="2" 
                            strokeLinecap="round" 
                            strokeLinejoin="round"
                          />
                        </svg>
                      </button>
                    )}
                    <span style={{color: '#3b82f6'}}>
                      <strong>Preserved:</strong> {reoptimizationResults.assignmentsPreserved}/{reoptimizationResults.initialBudget}
                    </span>
                  </div>
                  {reoptimizationResults.winterBudgetUsed > 0 && (
                    <>
                      <span style={{color: '#0ea5e9'}}>
                        <strong>Winter Budget:</strong> {reoptimizationResults.initialBudget}
                      </span>
                      <span style={{color: '#f59e0b'}}>
                        <strong>Winter Used:</strong> {reoptimizationResults.winterBudgetUsed}
                      </span>
                    </>
                  )}
                </div>
                {showPreservedStudents && reoptimizationResults.preservedStudentNames && reoptimizationResults.preservedStudentNames.length > 0 && (
                  <div style={{
                    marginTop: '12px',
                    padding: '12px',
                    backgroundColor: '#ffffff',
                    border: '1px solid #e0f2fe',
                    borderRadius: '6px',
                    fontSize: '0.85em',
                    animation: 'fadeIn 0.2s ease-in'
                  }}>
                    <strong style={{color: '#0369a1', marginBottom: '8px', display: 'block'}}>
                      Erhaltene Studierende ({reoptimizationResults.preservedStudentNames.length}):
                    </strong>
                    <div style={{
                      maxHeight: '150px',
                      overflowY: 'auto',
                      display: 'flex',
                      flexWrap: 'wrap',
                      gap: '6px'
                    }}>
                      {reoptimizationResults.preservedStudentNames.map((name, index) => (
                        <span key={index} style={{
                          padding: '4px 8px',
                          backgroundColor: '#e0f2fe',
                          color: '#075985',
                          borderRadius: '4px',
                          fontSize: '0.85em',
                          whiteSpace: 'nowrap'
                        }}>
                          {name}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )}

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
                        <span><strong>Praktika insgesamt:</strong> {phase1Result.totalPlannedInternships}</span>
                        <span style={{color: '#15803d'}}><strong>Zugewiesen:</strong> {phase1Result.assignedCount}</span>
                        <span style={{color: '#dc2626'}}><strong>Nicht zugewiesen:</strong> {phase1Result.unassignedCount}</span>
                      </div>
                    </div>
                  )}

                  <div style={{marginLeft: '16px'}}>
                    <SearchFilter
                      searchPlaceholder="Suche nach Lehrer, Kurs oder Schule..."
                      searchValue={teacherSearchTerm}
                      onSearchChange={setTeacherSearchTerm}
                      filters={teacherFilterConfigs}
                      activeFilters={teacherFilters}
                      onFilterChange={handleTeacherFilterChange}
                      onClearFilters={handleClearTeacherFilters}
                    />
                  </div>

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
                        {filteredTeacherAssignments.length === 0 ? (
                          <tr>
                            <td colSpan={7} className="table-empty">
                              {teacherSearchTerm || Object.values(teacherFilters).some(v => v) ? 
                                'Keine Zuweisungen entsprechen den Filterkriterien' : 
                                'No assignments generated'}
                            </td>
                          </tr>
                        ) : (
                          filteredTeacherAssignments.map((pi) => (
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
                {!selectedYear?.startsWith('SoSe') && (
                  <button 
                    className="btn-primary"
                    onClick={handleAssignPhase2}
                    disabled={assigningPhase2 || !phase1Result}
                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                  >
                    {assigningPhase2 ? "Läuft..." : "Zuweisen"}
                  </button>
                )}
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
                  <div style={{marginLeft: '16px'}}>
                    <SearchFilter
                      searchPlaceholder="Suche nach Schüler, Betreuer oder Schule..."
                      searchValue={studentSearchTerm}
                      onSearchChange={setStudentSearchTerm}
                      filters={studentFilterConfigs}
                      activeFilters={studentFilters}
                      onFilterChange={handleStudentFilterChange}
                      onClearFilters={handleClearStudentFilters}
                    />
                  </div>

                  <table className="schools-table">
                    <thead>
                      <tr>
                        <th style={{minWidth: '300px'}}>Praktikum</th>
                        <th>Schülername</th>
                        <th>Status</th>
                        <th>Aktionen</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredStudentAssignments.length === 0 ? (
                        <tr>
                          <td colSpan={4} className="table-empty">
                            {studentSearchTerm || Object.values(studentFilters).some(v => v) ? 
                              'Keine Zuweisungen entsprechen den Filterkriterien' : 
                              'No student assignments generated'}
                          </td>
                        </tr>
                      ) : (
                        // Group filtered assignments by internship (teacher + school + course + type)
                        Object.entries(
                          filteredStudentAssignments.reduce((groups: any, assignment) => {
                            const key = `${assignment.teacherName ?? 'Unassigned'} | ${assignment.schoolName ?? 'No School'} | ${assignment.course ?? 'No Course'} | ${assignment.praktikumType}`;
                            if (!groups[key]) {
                              groups[key] = [];
                            }
                            groups[key].push(assignment);
                            return groups;
                          }, {})
                        ).map(([_, assignments]: [string, any]) => (
                          assignments.map((assignment: any, idx: number) => (
                            <tr key={assignment.id}>
                              {idx === 0 && (
                                <td rowSpan={assignments.length} style={{
                                  background: 'linear-gradient(135deg, #f8fafc 0%, #ffffff 100%)',
                                  fontWeight: '500',
                                  minWidth: '320px',
                                  padding: '24px',
                                  position: 'relative'
                                }}>
                                  <div style={{fontSize: '1.05em', lineHeight: '1.9'}}>
                                    <div style={{
                                      fontWeight: '700', 
                                      color: '#1e293b', 
                                      fontSize: '1.15em',
                                      marginBottom: '8px'
                                    }}>
                                      {assignment.teacherName ?? 'Unassigned'}
                                    </div>
                                    <div style={{marginBottom: '12px'}}>
                                      <span style={{
                                        display: 'inline-block',
                                        padding: '3px 10px',
                                        background: 'transparent',
                                        border: '1.5px solid #3b82f6',
                                        borderRadius: '12px',
                                        fontSize: '0.75em',
                                        fontWeight: '600',
                                        color: '#3b82f6',
                                        letterSpacing: '0.3px'
                                      }}>
                                        {assignment.praktikumType}
                                      </span>
                                    </div>
                                    <div style={{
                                      fontSize: '0.95em', 
                                      color: '#64748b', 
                                      paddingLeft: '16px',
                                      borderLeft: '2px solid #e2e8f0',
                                      marginLeft: '4px'
                                    }}>
                                      <div style={{marginBottom: '6px', display: 'flex', alignItems: 'center', gap: '6px'}}>
                                        <span style={{color: '#94a3b8', fontSize: '0.9em'}}>🏫</span>
                                        <span style={{color: '#475569'}}>{assignment.schoolName ?? 'No School'}</span>
                                      </div>
                                      <div style={{marginBottom: '6px', display: 'flex', alignItems: 'center', gap: '6px'}}>
                                        <span style={{color: '#94a3b8', fontSize: '0.9em'}}>📚</span>
                                        <span style={{color: '#475569'}}>{assignment.course ?? '—'}</span>
                                      </div>
                                    </div>
                                  </div>
                                </td>
                              )}
                              <td>
                                <span style={{fontWeight: '500'}}>
                                  {assignment.studentName}
                                </span>
                              </td>
                              <td>
                                <span
                                className={`status-badge ${
                                  assignment.status === "CANCELLED"
                                    ? "status-cancelled"
                                    : assignment.status === "CONFIRMED"
                                    ? "status-confirmed"
                                    : editedAssignmentIds.has(assignment.id)
                                    ? "status-edited"
                                    : assignment.status === "PROPOSED"
                                    ? "status-proposed"
                                    : "status-other"
                                }`}
                              >
                                {assignment.status === "CANCELLED"
                                  ? "Abgesagt"
                                  : assignment.status === "CONFIRMED"
                                  ? "Bestätigt"
                                  : editedAssignmentIds.has(assignment.id)
                                  ? "Bearbeitet"
                                  : statusLabelDE(assignment.status)}
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
          <div style={{marginLeft: '16px'}}>
            <SearchFilter
              searchPlaceholder="🔍 Search teachers..."
              searchValue={plConfigSearchTerm}
              onSearchChange={setPlConfigSearchTerm}
              filters={plConfigFilterConfigs}
              activeFilters={plConfigFilters}
              onFilterChange={handlePlConfigFilterChange}
              onClearFilters={handleClearPlConfigFilters}
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
                  {filteredPlConfigTeachers.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="empty-state">
                        {plConfigSearchTerm || Object.values(plConfigFilters).some(v => v) ? 'No teachers match your filters' : 'No teachers found'}
                      </td>
                    </tr>
                  ) : (
                    filteredPlConfigTeachers.flatMap((teacher) => {
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

          <div style={{marginLeft: '16px'}}>
            <SearchFilter
              searchPlaceholder="Suche nach Schülername..."
              searchValue={studentSearchTerm}
              onSearchChange={setStudentSearchTerm}
              filters={studentConfigFilterConfigs}
              activeFilters={studentConfigFilters}
              onFilterChange={handleStudentConfigFilterChange}
              onClearFilters={handleClearStudentConfigFilters}
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
  {/* Get unique student IDs from filtered configs */}
  {(() => {
    const uniqueStudentIds = [...new Set(filteredStudentConfigs.map(c => c.studentId))];
    const filteredStudents = students.filter(s => uniqueStudentIds.includes(s.matriculationNbr));
    
    if (filteredStudents.length === 0) {
      return (
        <tr>
          <td colSpan={12} className="empty-state">
            {studentSearchTerm || Object.values(studentConfigFilters).some(v => v) ? 
              'Keine Konfigurationen entsprechen den Filterkriterien' : 
              'No students found'}
          </td>
        </tr>
      );
    }
    
    return filteredStudents.flatMap(student => {
      const studentConfigsForYear = filteredStudentConfigs.filter(
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
      });
    })()}
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
            <p>Select semester type and enter the year (e.g., WiSe2026 or SoSe2025)</p>
            <div style={{marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '10px'}}>
              <select
                className="form-select-small"
                value={newSemesterType}
                onChange={(e) => setNewSemesterType(e.target.value)}
                style={{padding: '10px', fontSize: '1rem', width: '120px'}}
              >
                <option value="WiSe">WiSe</option>
                <option value="SoSe">SoSe</option>
              </select>
              <input
                type="text"
                className="student-config-input"
                placeholder="2026"
                value={newYearInput}
                onChange={(e) => setNewYearInput(e.target.value)}
                onKeyPress={(e) => {
                  if (e.key === 'Enter') {
                    handleConfirmNewYear();
                  }
                }}
                autoFocus
                style={{flex: 1, padding: '10px', fontSize: '1rem', minWidth: '150px'}}
              />
            </div>
            <div className="modal-actions">
              <button
                className="btn btn-ghost"
                onClick={() => {
                  setShowNewYearModal(false);
                  setNewYearInput('');
                  setNewSemesterType('WiSe');
                }}
              >
                Cancel
              </button>
              <button
                className="btn-primary-filled"
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
                const wasEditing = !!editingStudentConfig;
                const yearBeingSaved = selectedYear; // Preserve the year being saved
                handleCloseStudentConfigModal();
                
                // Small delay to ensure database transaction completes
                await new Promise(resolve => setTimeout(resolve, 200));
                
                // Refresh years list ensuring the saved year stays selected
                await refreshAvailableYears(yearBeingSaved);
                
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
                    
                    // Automatically set school based on teacher and mark as edited
                    const updated = {
                      ...editingAssignment, 
                      teacherId,
                      teacherName: teacher ? `${teacher.firstName} ${teacher.lastName}` : null,
                      schoolId: teacher?.schoolId || null,
                      schoolName: teacher?.schoolName || null
                    };
                    console.log('Updating assignment to:', updated);
                    setEditingAssignment(updated);
                    setValidationResult(null);
                    setShowForceSaveAssignment(false);

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
  const nextStatus = e.target.value;

  // Clear any previous validation results / force-save UI
  setValidationResult(null);
  setShowForceSaveAssignment(false);

  if (nextStatus === 'CANCELLED') {
    // revert other pending edits to the original assignment
    const original = studentAssignments.find(a => a.id === editingAssignment.id);

    setEditingAssignment({
      ...editingAssignment,
      status: nextStatus,
      teacherId: original?.teacherId ?? editingAssignment.teacherId,
      teacherName: original?.teacherName ?? editingAssignment.teacherName,
      schoolId: original?.schoolId ?? editingAssignment.schoolId,
      schoolName: original?.schoolName ?? editingAssignment.schoolName,
    });
    return;
  }

  setEditingAssignment({ ...editingAssignment, status: nextStatus });

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
                className="btn btn-ghost"
                onClick={handleCloseEditAssignmentModal}
              >
                Abbrechen
              </button>

<button className="btn-save" onClick={() => handleSaveEditAssignment(false)}>
    Speichern
  </button>

  {showForceSaveAssignment && (
    <ForceSaveModal
      title="Trotzdem speichern?"
      message="Diese Änderung verletzt mindestens eine harte Regel. Möchten Sie trotzdem speichern?"
      hardViolations={(validationResult?.hardViolations || []).map((v: any) => v.message)}
      warnings={(validationResult?.warnings || []).map((v: any) => v.message)}
      onCancel={() => setShowForceSaveAssignment(false)}
      onConfirm={async () => {
        setShowForceSaveAssignment(false);
        await handleSaveEditAssignment(true);
      }}
    />
  )}



            </div>
          </div>
        </div>
      )}
    </div>
  );
}
