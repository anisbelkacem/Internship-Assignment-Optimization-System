import { useEffect, useMemo, useState } from "react";
import apiService from "../services/api";
import plService, {
  type TeacherDto,
} from "../services/plService";
import "../styles/Pls/Pls.css";
import "../styles/Schools.css";
import SearchFilter, { type FilterConfig } from "../components/SearchFilter";
import courseService, { type Course } from "../services/courseService";
import PlFormModal, {
  type PlFormValues,
  type SchoolOption,
} from "../components/PlFormModal";
import PlImportModal from "../components/PlImportModal";

type Mode = "create" | "edit";



const initialForm: PlFormValues = {
  teacherId: undefined,
  firstName: "",
  lastName: "",
  email: "",
  mainSubjectId: "",
  schoolId: "",
  isPartTime: false,
};

export default function Pls() {
  const [courses, setCourses] = useState<Course[]>([]);

useEffect(() => {
  void courseService.getAllCourses().then(data => {
    setCourses(data.filter(c => c.active));
  });
}, []);

  const [pls, setPls] = useState<TeacherDto[]>([]);
  const [schools, setSchools] = useState<SchoolOption[]>([]);
  const [loading, setLoading] = useState(false);

  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [mode, setMode] = useState<Mode>("create");
  const [form, setForm] = useState<PlFormValues>(initialForm);

  const [showFormModal, setShowFormModal] = useState(false);
  const [showImportModal, setShowImportModal] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [filters, setFilters] = useState<Record<string, string>>({
    school: '',
    subject: '',
    employmentType: '',
  });

  useEffect(() => {
    void loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    setError(null);
    try {
      const [plsData, schoolsData] = await Promise.all([
        plService.getAllPls(),
        apiService.get<SchoolOption[]>("/api/schools/active"),
      ]);
      // Show ALL PLs (both active and inactive) in the Pls dashboard
      setPls(plsData);
      setSchools(
        schoolsData.map((s) => ({
          id: s.id,
          name: s.name,
        }))
      );
    } catch (e) {
      console.error(e);
      setError("Fehler beim Laden der PLs oder Schulen.");
    } finally {
      setLoading(false);
    }
  }

  function showError(msg: string) {
    setError(msg);
    setSuccess(null);
  }

  function showSuccess(msg: string) {
    setSuccess(msg);
    setError(null);
  }

  function resetForm() {
    setForm(initialForm);
    setMode("create");
  }

  function handleOpenFormModal(pl?: TeacherDto) {
    if (pl) {
      setMode("edit");
      setForm({
        teacherId: pl.teacherId,
        firstName: pl.firstName ?? "",
        lastName: pl.lastName ?? "",
        email: pl.email ?? "",
        mainSubjectId: pl.mainSubject ? pl.mainSubject.id : "",
        schoolId: pl.schoolId ?? "",
        isPartTime: pl.isPartTime ?? false,
      });
    } else {
      resetForm();
    }
    setShowFormModal(true);
  }

  function handleCloseFormModal() {
    setShowFormModal(false);
    resetForm();
  }

  function handleOpenImportModal() {
    setShowImportModal(true);
  }

  function handleCloseImportModal() {
    setShowImportModal(false);
  }

  function handleInputChange(
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) {
    const { name, value } = e.target;
    
    // Handle checkbox separately
    if (name === "isPartTime") {
      setForm((prev) => ({
        ...prev,
        isPartTime: typeof value === 'boolean' ? value : (e.target as HTMLInputElement).checked,
      }));
      return;
    }
    
    setForm((prev) => ({
      ...prev,
[name]:
  (name === "schoolId" || name === "mainSubjectId") && value !== ""
    ? Number(value)
    : value,
    }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();

    if (!form.firstName.trim() || !form.lastName.trim()) {
      showError("Vor- und Nachname sind erforderlich.");
      return;
    }
    if (!form.email.trim()) {
      showError("E-Mail ist erforderlich.");
      return;
    }
    if (!form.mainSubjectId) {
      showError("Hauptfach ist erforderlich.");
      return;
    }
    if (!form.schoolId) {
      showError("Schule ist erforderlich.");
      return;
    }

const selectedCourse = courses.find(c => c.id === form.mainSubjectId);

if (!selectedCourse) {
  showError("Hauptfach ist erforderlich.");
  return;
}

const teacherPayload = {
  firstName: form.firstName.trim(),
  lastName: form.lastName.trim(),
  email: form.email.trim(),
  mainSubject: selectedCourse,
  schoolId: { id: form.schoolId as number },
  isPartTime: form.isPartTime,
};


    try {
      if (mode === "create") {
        await plService.createPl(teacherPayload);
      } else {
        if (!form.teacherId) {
          showError("Interner Fehler: teacherId fehlt beim Bearbeiten.");
          return;
        }
        await plService.updatePl(form.teacherId, teacherPayload);
      }

      await loadData();
      showSuccess(
        mode === "create"
          ? "PL erfolgreich angelegt."
          : "PL erfolgreich aktualisiert."
      );
      handleCloseFormModal();
    } catch (err) {
      console.error(err);
      showError("Fehler beim Speichern der PL-Daten.");
    }
  }

  function handleEdit(pl: TeacherDto) {
    handleOpenFormModal(pl);
  }

  async function handleDelete(pl: TeacherDto) {
    const confirmed = window.confirm(
      `PL "${pl.firstName} ${pl.lastName}" wirklich löschen?`
    );
    if (!confirmed) return;

    try {
      await plService.deletePl(pl.teacherId);
      await loadData();
      showSuccess("PL wurde gelöscht.");
    } catch (err) {
      console.error(err);
      showError("Fehler beim Löschen des PLs.");
    }
  }

  async function handleToggleActive(pl: TeacherDto) {
    try {
      if (pl.active) {
        await plService.deactivatePl(pl.teacherId);
      } else {
        await plService.activatePl(pl.teacherId);
      }
      await loadData();
      showSuccess(pl.active ? "PL wurde deaktiviert." : "PL wurde aktiviert.");
    } catch (err) {
      console.error(err);
      showError("Fehler beim Ändern des PL-Status.");
    }
  }

  const hasPls = useMemo(() => pls.length > 0, [pls]);

  // Get unique schools from PLs
  const uniqueSchools = useMemo(() => {
    const schoolSet = new Set(
      pls.map(pl => pl.schoolName).filter((name): name is string => !!name)
    );
    return Array.from(schoolSet).sort();
  }, [pls]);

  // Get unique subjects from PLs
  const uniqueSubjects = useMemo(() => {
    const subjectSet = new Set(
      pls.map(pl => pl.mainSubject.name).filter((name): name is string => !!name)
    );
    return Array.from(subjectSet).sort();
  }, [pls]);

  // Build filter configurations
  const filterConfigs: FilterConfig[] = useMemo(() => [
    {
      field: 'school',
      label: 'Schule',
      options: uniqueSchools.map(school => ({
        label: school,
        value: school,
        field: 'school'
      }))
    },
    {
      field: 'subject',
      label: 'Hauptfach',
      options: uniqueSubjects.map(subject => ({
        label: subject.charAt(0).toUpperCase() + subject.slice(1),
        value: subject,
        field: 'subject'
      }))
    },
    {
      field: 'employmentType',
      label: 'Beschäftigungsart',
      options: [
        { label: 'Vollzeit', value: 'false', field: 'employmentType' },
        { label: 'Teilzeit', value: 'true', field: 'employmentType' }
      ]
    }
  ], [uniqueSchools, uniqueSubjects]);

  const handleFilterChange = (field: string, value: string) => {
    setFilters(prev => ({ ...prev, [field]: value }));
  };

  const handleClearFilters = () => {
    setFilters({ school: '', subject: '', employmentType: '' });
  };

  const filteredPls = useMemo(() => {
    return pls.filter(pl => {
      const fullName = `${pl.firstName} ${pl.lastName}`.toLowerCase();
      const matchesSearch = fullName.includes(searchTerm.toLowerCase()) ||
                           pl.email.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesSchool = !filters.school || pl.schoolName === filters.school;
      const matchesSubject = !filters.subject || pl.mainSubject.name === filters.subject;
      const matchesEmploymentType = !filters.employmentType ||
                                   (filters.employmentType === 'true' && pl.isPartTime) ||
                                   (filters.employmentType === 'false' && !pl.isPartTime);
      
      return matchesSearch && matchesSchool && matchesSubject && matchesEmploymentType;
    });
  }, [pls, searchTerm, filters]);

  return (
    <section className="section-container pls-section">
      <div className="section-header">
        <div>
          <h2>PL-Management</h2>
        </div>
        <div className="section-header-actions">
          <button
            type="button"
            className="btn btn-ghost"
            onClick={handleOpenImportModal}
          >
            Importieren
          </button>
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => handleOpenFormModal()}
            onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
            onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
          >
            PL hinzufügen
          </button>
        </div>
      </div>

      {error && <div className="inline-alert error">{error}</div>}
      {success && <div className="inline-alert success">{success}</div>}

      <SearchFilter
        searchPlaceholder="Suche nach Name oder E-Mail..."
        searchValue={searchTerm}
        onSearchChange={setSearchTerm}
        filters={filterConfigs}
        activeFilters={filters}
        onFilterChange={handleFilterChange}
        onClearFilters={handleClearFilters}
      />

      <div className="table-card">
        <div className="table-card-header">
          <div className="table-card-title">
            <h3>Betreuerliste</h3>
            <span className="table-card-subtitle">
              Übersicht aller Praktikumsleitungen
            </span>
          </div>
        </div>

        {loading && <p className="table-status">Lade Daten...</p>}

        {!loading && !hasPls && (
          <p className="table-empty">
            Es sind noch keine PLs erfasst. Nutzen Sie oben
            <strong> „Neuen PL hinzufügen“</strong>, um einen Betreuer
            anzulegen.
          </p>
        )}

        {!loading && hasPls && (
          <div className="table-container" style={{overflowX: 'auto'}}>
            <table className="schools-table">
              <thead>
                <tr>
                  <th style={{ whiteSpace: 'nowrap' }}>Name</th>
                  <th style={{ whiteSpace: 'nowrap' }}>Hauptfach</th>
                  <th style={{ whiteSpace: 'nowrap' }}>Schule</th>
                  <th style={{ whiteSpace: 'nowrap' }}>E-Mail</th>
                  <th style={{ whiteSpace: 'nowrap' }}>Beschäftigungsart</th>
                  <th style={{ whiteSpace: 'nowrap' }}>Aktionen</th>
                </tr>
              </thead>
              <tbody>
                {filteredPls.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="empty-state">
                      {searchTerm ? "Keine PLs gefunden" : "Es sind keine PLs vorhanden"}
                    </td>
                  </tr>
                ) : (
                  filteredPls.map((pl) => (
                    <tr key={pl.teacherId}>
                      <td>{pl.firstName} {pl.lastName}</td>
                      <td>{pl.mainSubject.name.charAt(0).toUpperCase() + pl.mainSubject.name.slice(1)}</td>
                      <td>{pl.schoolName ?? "-"}</td>
                      <td>{pl.email}</td>
                      <td>
                        {pl.isPartTime ? (
                          <span style={{
                            padding: "6px 16px",
                            backgroundColor: "#fef3c7",
                            color: "#92400e",
                            borderRadius: "20px",
                            fontSize: "0.75rem",
                            fontWeight: "600",
                            display: "inline-block",
                            whiteSpace: "nowrap"
                          }}>
                            Teilzeit
                          </span>
                        ) : (
                          <span style={{
                            padding: "6px 16px",
                            backgroundColor: "#e0f2fe",
                            color: "#075985",
                            borderRadius: "20px",
                            fontSize: "0.75rem",
                            fontWeight: "600",
                            display: "inline-block",
                            whiteSpace: "nowrap"
                          }}>
                            Vollzeit
                          </span>
                        )}
                      </td>
                      <td>
                        <div style={{display: 'flex', gap: '8px', alignItems: 'center'}}>
                          <button
                            type="button"
                            className="action-btn edit-btn"
                            onClick={() => handleEdit(pl)}
                            title="Bearbeiten"
                          >
                            ✏️
                          </button>
                          <button
                            type="button"
                            className="action-btn"
                            onClick={() => handleToggleActive(pl)}
                            title={pl.active ? "Deaktivieren" : "Aktivieren"}
                            style={{
                              backgroundColor: pl.active ? '#f59e0b' : '#10b981',
                              color: 'white',
                              border: 'none',
                              padding: '6px 10px',
                              borderRadius: '4px',
                              cursor: 'pointer',
                              fontSize: '14px',
                              fontWeight: 'bold',
                              transition: 'all 0.2s ease'
                            }}
                          >
                            {pl.active ? '⏸️' : '▶️'}
                          </button>
                          <button
                            type="button"
                            className="action-btn delete-btn"
                            onClick={() => handleDelete(pl)}
                            title="Löschen"
                          >
                            🗑️
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* PL create/edit modal */}
      <PlFormModal
        isOpen={showFormModal}
        mode={mode}
        form={form}
        schools={schools}
        onChange={handleInputChange}
        onClose={handleCloseFormModal}
        onSubmit={handleSubmit}
      />

      {/* Import modal */}
      <PlImportModal
        isOpen={showImportModal}
        onClose={handleCloseImportModal}
        onSuccess={showSuccess}
        onError={showError}
        onImported={loadData}
      />
    </section>
  );
}
