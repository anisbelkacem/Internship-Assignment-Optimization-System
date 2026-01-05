import { useEffect, useMemo, useState } from "react";
import apiService from "../services/api";
import plService, {
  Course,
  type TeacherDto,
} from "../services/plService";
import "../styles/Pls/Pls.css";

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
  mainSubject: "",
  schoolId: "",
};

export default function Pls() {
  const [pls, setPls] = useState<TeacherDto[]>([]);
  const [schools, setSchools] = useState<SchoolOption[]>([]);
  const [loading, setLoading] = useState(false);

  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [mode, setMode] = useState<Mode>("create");
  const [form, setForm] = useState<PlFormValues>(initialForm);

  const [showFormModal, setShowFormModal] = useState(false);
  const [showImportModal, setShowImportModal] = useState(false);

  useEffect(() => {
    void loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    setError(null);
    try {
      const [plsData, schoolsData] = await Promise.all([
        plService.getAllPls(),
        apiService.get<SchoolOption[]>("/api/schools"),
      ]);

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
        mainSubject: (pl.mainSubject as Course) ?? "",
        schoolId: (pl).schoolId ?? "",
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
    setForm((prev) => ({
      ...prev,
      [name]:
        name === "schoolId" && value !== "" ? Number(value) : (value ),
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
    if (!form.mainSubject) {
      showError("Hauptfach ist erforderlich.");
      return;
    }
    if (!form.schoolId) {
      showError("Schule ist erforderlich.");
      return;
    }

    const teacherPayload = {
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      email: form.email.trim(),
      mainSubject: form.mainSubject as Course,
      schoolId: { id: form.schoolId as number },
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

  const hasPls = useMemo(() => pls.length > 0, [pls]);

  return (
    <section className="section-container pls-section">
      <div className="section-header">
        <div>
          <h2>PL-Management</h2>
          <p className="pls-subtitle">
            Betreuer mit Fächerspezialisierungen, Arbeitslast und
            Praktikumspräferenzen verwalten.
          </p>
        </div>
        <div className="section-header-actions">
          <button
            type="button"
            className="btn btn-ghost"
            onClick={handleOpenImportModal}
          >
            PLs importieren
          </button>
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => handleOpenFormModal()}
          >
            PL hinzufügen
          </button>
        </div>
      </div>

      {error && <div className="inline-alert error">{error}</div>}
      {success && <div className="inline-alert success">{success}</div>}

      <div className="table-card">
        <div className="table-card-header">
          <div className="table-card-title">
            <h3>Betreuerliste</h3>
            <span className="table-card-subtitle">
              Übersicht aller Praktikumsleitungen
            </span>
          </div>
          <span className="table-count">
            {pls.length} {pls.length === 1 ? "Eintrag" : "Einträge"}
          </span>
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
          <div className="table-container">
            <table className="pls-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Hauptfach</th>
                  <th>Schule</th>
                  <th>E-Mail</th>
                  <th>Aktionen</th>
                </tr>
              </thead>
              <tbody>
                {pls.map((pl) => (
                  <tr key={pl.teacherId}>
                    <td>
                      {pl.firstName} {pl.lastName}
                    </td>
                    <td>{pl.mainSubject}</td>
                    <td>{pl.schoolName ?? "-"}</td>
                    <td>{pl.email}</td>
                    <td>
                      <div className="table-actions">
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
                          className="action-btn delete-btn"
                          onClick={() => handleDelete(pl)}
                          title="Löschen"
                        >
                          🗑️
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
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
