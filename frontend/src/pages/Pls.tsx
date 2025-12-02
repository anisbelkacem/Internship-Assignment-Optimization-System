import { useEffect, useMemo, useState } from "react";
import apiService from "../services/api";
import plService, {
  Course,
  PraktikumType,
  type TeacherDto,
  type TeacherPlConfigDto,
  type TeacherPlConfigRequest,
  type TeacherRequest,
  type TeacherImportResult,
} from "../services/plService";

interface SchoolOption {
  id: number;
  name: string;
}

interface PlFormState {
  teacherId?: number;
  configId?: number;
  firstName: string;
  lastName: string;
  email: string;
  mainSubject: Course | "";
  schoolId: number | "";
  schoolYear: string;
  maxPraktikaPerYear: number | "";
  totalHoursCredit: number | "";
  subjectSpecializationsInput: string;
  internshipPreferences: PraktikumType[];
}

type Mode = "create" | "edit";

const PRAKTIKUM_TYPE_OPTIONS: { value: PraktikumType; label: string }[] = [
  { value: PraktikumType.PDP_I, label: "PDP I" },
  { value: PraktikumType.PDP_II, label: "PDP II" },
  { value: PraktikumType.ZSP, label: "ZSP" },
  { value: PraktikumType.SFP, label: "SFP" },
];

const COURSE_OPTIONS: { value: Course; label: string }[] = [
  { value: Course.COMPUTER_SCIENCE, label: "Informatik" },
  { value: Course.ENGINEERING, label: "Ingenieurwesen" },
  { value: Course.BUSINESS, label: "Wirtschaft" },
  { value: Course.MEDICINE, label: "Medizin" },
  { value: Course.LAW, label: "Recht" },
  { value: Course.ARTS, label: "Kunst" },
  { value: Course.SCIENCES, label: "Naturwissenschaften" },
  { value: Course.OTHER, label: "Sonstiges" },
];

const initialForm: PlFormState = {
  firstName: "",
  lastName: "",
  email: "",
  mainSubject: "",
  schoolId: "",
  schoolYear: "",
  maxPraktikaPerYear: "",
  totalHoursCredit: "",
  subjectSpecializationsInput: "",
  internshipPreferences: [],
};

function getActiveConfig(pl: TeacherDto): TeacherPlConfigDto | undefined {
  if (!pl.plConfigs || pl.plConfigs.length === 0) return undefined;
  const sorted = [...pl.plConfigs].sort((a, b) =>
    (a.schoolYear || "").localeCompare(b.schoolYear || "")
  );
  return sorted[sorted.length - 1];
}

export default function Pls() {
  const [pls, setPls] = useState<TeacherDto[]>([]);
  const [schools, setSchools] = useState<SchoolOption[]>([]);
  const [loading, setLoading] = useState(false);

  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [mode, setMode] = useState<Mode>("create");
  const [form, setForm] = useState<PlFormState>(initialForm);

  const [importFile, setImportFile] = useState<File | null>(null);
  const [importResult, setImportResult] = useState<TeacherImportResult | null>(
    null
  );
  const [importing, setImporting] = useState(false);

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

  function handleInputChange(
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) {
    const { name, value } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]:
        name === "maxPraktikaPerYear" || name === "totalHoursCredit"
          ? value === ""
            ? ""
            : Number(value)
          : value,
    }));
  }

  function handleSubjectSpecializationsChange(
    e: React.ChangeEvent<HTMLInputElement>
  ) {
    setForm((prev) => ({
      ...prev,
      subjectSpecializationsInput: e.target.value,
    }));
  }

  function handlePreferenceToggle(type: PraktikumType) {
    setForm((prev) => {
      const exists = prev.internshipPreferences.includes(type);
      return {
        ...prev,
        internshipPreferences: exists
          ? prev.internshipPreferences.filter((t) => t !== type)
          : [...prev.internshipPreferences, type],
      };
    });
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

    const subjectSpecializations = form.subjectSpecializationsInput
      .split(",")
      .map((s) => s.trim())
      .filter((s) => s.length > 0);

    const teacherPayload: TeacherRequest = {
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      email: form.email.trim(),
      mainSubject: form.mainSubject as Course,
      schoolId: { id: form.schoolId as number },
    };

    const hasConfigData =
      form.schoolYear.trim() ||
      form.maxPraktikaPerYear !== "" ||
      form.totalHoursCredit !== "" ||
      subjectSpecializations.length > 0 ||
      form.internshipPreferences.length > 0;

    let teacher: TeacherDto | null = null;

    try {
      if (mode === "create") {
        teacher = await plService.createPl(teacherPayload);
      } else {
        if (!form.teacherId) {
          showError("Interner Fehler: teacherId fehlt beim Bearbeiten.");
          return;
        }
        teacher = await plService.updatePl(form.teacherId, teacherPayload);
      }

      if (teacher && hasConfigData) {
        const cfgPayload: TeacherPlConfigRequest = {
          schoolYear: form.schoolYear || "",
          maxPraktikaPerYear:
            form.maxPraktikaPerYear === ""
              ? 0
              : Number(form.maxPraktikaPerYear),
          totalHoursCredit:
            form.totalHoursCredit === "" ? 0 : Number(form.totalHoursCredit),
          subjectSpecializations,
          internshipPreferences: form.internshipPreferences,
        };

        if (mode === "create" || !form.configId) {
          await plService.createConfig(teacher.teacherId, cfgPayload);
        } else {
          await plService.updateConfig(
            teacher.teacherId,
            form.configId,
            cfgPayload
          );
        }
      }

      await loadData();
      showSuccess(
        mode === "create"
          ? "PL erfolgreich angelegt."
          : "PL erfolgreich aktualisiert."
      );
      resetForm();
    } catch (err) {
      console.error(err);
      showError("Fehler beim Speichern der PL-Daten.");
    }
  }

  function handleEdit(pl: TeacherDto) {
    const activeConfig = getActiveConfig(pl);

    setMode("edit");
    setForm({
      teacherId: pl.teacherId,
      configId: activeConfig?.id,
      firstName: pl.firstName ?? "",
      lastName: pl.lastName ?? "",
      email: pl.email ?? "",
      mainSubject: pl.mainSubject ?? "",
      schoolId: pl.school?.id ?? "",
      schoolYear: activeConfig?.schoolYear ?? "",
      maxPraktikaPerYear: activeConfig?.maxPraktikaPerYear ?? "",
      totalHoursCredit: activeConfig?.totalHoursCredit ?? "",
      subjectSpecializationsInput: activeConfig
        ? (activeConfig.subjectSpecializations || []).join(", ")
        : "",
      internshipPreferences: activeConfig?.internshipPreferences ?? [],
    });
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

  function handleImportFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0] || null;
    setImportFile(file);
    setImportResult(null);
    setError(null);
    setSuccess(null);
  }

  async function handleImportClick() {
    if (!importFile) {
      showError("Bitte zuerst eine .xlsx-Datei auswählen.");
      return;
    }

    setImporting(true);
    setError(null);
    setSuccess(null);
    setImportResult(null);

    try {
      const result = await plService.importFromExcel(importFile);
      setImportResult(result);
      await loadData();
      showSuccess(
        `Import abgeschlossen: ${result.importedCount} von ${result.totalRows} Zeilen importiert.`
      );
    } catch (err) {
      console.error(err);
      showError("Fehler beim Import der PLs aus Excel.");
    } finally {
      setImporting(false);
    }
  }

  const hasPls = useMemo(() => pls.length > 0, [pls]);

  const inputStyle: React.CSSProperties = {
    width: "100%",
    marginTop: 4,
    padding: "7px 9px",
    borderRadius: 8,
    border: "1px solid #d1d5db",
    fontSize: 13,
    outline: "none",
  };

  const labelStyle: React.CSSProperties = {
    fontSize: 12,
    color: "#374151",
    display: "block",
  };

  return (
    <section className="pls-table-section">
      {/* Page header */}
      <div className="pls-header">
        <div>
          <h2>PL-Management</h2>
          <p style={{ fontSize: 13, color: "#6f7276", marginTop: 4 }}>
            Betreuer mit Fächerspezialisierungen, Arbeitslast und
            Praktikumspräferenzen verwalten.
          </p>
        </div>
        <div className="pls-actions">
          <button
            type="button"
            className="btn-ghost"
            onClick={resetForm}
          >
            Neuer PL
          </button>
        </div>
      </div>

      {/* Messages */}
      {error && (
        <div
          style={{
            padding: "10px 12px",
            backgroundColor: "#fee2e2",
            borderRadius: 8,
            color: "#b91c1c",
            fontSize: 13,
            marginBottom: 12,
          }}
        >
          {error}
        </div>
      )}

      {success && (
        <div
          style={{
            padding: "10px 12px",
            backgroundColor: "#ecfdf3",
            borderRadius: 8,
            color: "#15803d",
            fontSize: 13,
            marginBottom: 12,
          }}
        >
          {success}
        </div>
      )}

      {/* Main content: table + right panel */}
      <div className="cards">
        {/* Left: PL table */}
        <div className="card pl-table">
          <div className="card-header">
            <h3>Betreuerliste</h3>
            <span className="card-count">
              {pls.length} {pls.length === 1 ? "Eintrag" : "Einträge"}
            </span>
          </div>

          {loading && <p>Lade Daten...</p>}

          {!loading && !hasPls && (
            <p style={{ fontSize: 14, color: "#6f7276" }}>
              Es sind noch keine PLs erfasst. Legen Sie rechts einen neuen PL
              an.
            </p>
          )}

          {!loading && hasPls && (
            <div className="pl-table">
              <table>
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Hauptfach</th>
                    <th>Schule</th>
                    <th>E-Mail</th>
                    <th>Schuljahr</th>
                    <th>Max. Praktika</th>
                    <th>Ermäßigung</th>
                    <th>Präferenzen</th>
                    <th>Aktionen</th>
                  </tr>
                </thead>
                <tbody>
                  {pls.map((pl) => {
                    const cfg = getActiveConfig(pl);
                    return (
                      <tr key={pl.teacherId}>
                        <td>
                          {pl.firstName} {pl.lastName}
                        </td>
                        <td>{pl.mainSubject}</td>
                        <td>{pl.school?.name ?? "-"}</td>
                        <td>{pl.email}</td>
                        <td>{cfg?.schoolYear ?? "-"}</td>
                        <td>{cfg?.maxPraktikaPerYear ?? "-"}</td>
                        <td>{cfg?.totalHoursCredit ?? "-"}</td>
                        <td>
                          {cfg?.internshipPreferences &&
                          cfg.internshipPreferences.length > 0
                            ? cfg.internshipPreferences.join(", ")
                            : "-"}
                        </td>
                        <td>
                          <button
                            type="button"
                            className="btn-ghost"
                            onClick={() => handleEdit(pl)}
                          >
                            Bearbeiten
                          </button>
                          {"  "}
                          <button
                            type="button"
                            className="btn-ghost"
                            style={{ color: "#b91c1c" }}
                            onClick={() => handleDelete(pl)}
                          >
                            Löschen
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Right: nicer form + import */}
        <div className="right-panel">
          {/* PL form */}
          <div className="card" style={{ background: "#f3f4f6" }}>
            <div className="card-header">
              <h3>
                {mode === "create"
                  ? "PL-Stammdaten"
                  : "PL bearbeiten"}
            </h3>
          </div>

          <form id="pl-form" onSubmit={handleSubmit}>
            {/* Stammdaten block */}
            <div
              style={{
                background: "#ffffff",
                borderRadius: 10,
                padding: "12px 14px 14px",
                marginBottom: 12,
                border: "1px solid #e5e7eb",
              }}
            >
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                  marginBottom: 10,
                }}
              >
                <div>
                  <div
                    style={{
                      fontSize: 11,
                      textTransform: "uppercase",
                      letterSpacing: 0.5,
                      color: "#6b7280",
                      marginBottom: 2,
                    }}
                  >
                    Stammdaten
                  </div>
                  <div
                    style={{
                      fontSize: 13,
                      color: "#4b5563",
                    }}
                  >
                    Basisinformationen zum Betreuer
                  </div>
                </div>
                <span
                  style={{
                    fontSize: 11,
                    padding: "2px 8px",
                    borderRadius: 999,
                    background: "#eef6ff",
                    color: "#0b63a8",
                    fontWeight: 600,
                  }}
                >
                  Pflichtfelder *
                </span>
              </div>

              <div
                style={{
                  display: "grid",
                  gridTemplateColumns: "1fr 1fr",
                  gap: 10,
                }}
              >
                <div>
                  <label style={labelStyle}>
                    Vorname*
                    <input
                      type="text"
                      name="firstName"
                      value={form.firstName}
                      onChange={handleInputChange}
                      style={inputStyle}
                    />
                  </label>
                </div>
                <div>
                  <label style={labelStyle}>
                    Nachname*
                    <input
                      type="text"
                      name="lastName"
                      value={form.lastName}
                      onChange={handleInputChange}
                      style={inputStyle}
                    />
                  </label>
                </div>
                <div>
                  <label style={labelStyle}>
                    E-Mail*
                    <input
                      type="email"
                      name="email"
                      value={form.email}
                      onChange={handleInputChange}
                      style={inputStyle}
                    />
                  </label>
                </div>
                <div>
                  <label style={labelStyle}>
                    Hauptfach*
                    <select
                      name="mainSubject"
                      value={form.mainSubject}
                      onChange={handleInputChange}
                      style={inputStyle}
                    >
                      <option value="">Bitte wählen</option>
                      {COURSE_OPTIONS.map((c) => (
                        <option key={c.value} value={c.value}>
                          {c.label}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>
                <div style={{ gridColumn: "1 / span 2" }}>
                  <label style={labelStyle}>
                    Schule*
                    <select
                      name="schoolId"
                      value={form.schoolId}
                      onChange={handleInputChange}
                      style={inputStyle}
                    >
                      <option value="">Bitte wählen</option>
                      {schools.map((s) => (
                        <option key={s.id} value={s.id}>
                          {s.name}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>
              </div>
            </div>

            {/* Konfig-Block */}
            <div
              style={{
                background: "#ffffff",
                borderRadius: 10,
                padding: "12px 14px 14px",
                border: "1px solid #e5e7eb",
              }}
            >
              <div
                style={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                  marginBottom: 10,
                }}
              >
                <div>
                  <div
                    style={{
                      fontSize: 11,
                      textTransform: "uppercase",
                      letterSpacing: 0.5,
                      color: "#6b7280",
                      marginBottom: 2,
                    }}
                  >
                    Schuljahres-Konfiguration
                  </div>
                  <div
                    style={{
                      fontSize: 13,
                      color: "#4b5563",
                    }}
                  >
                    Arbeitslast & Praktikumspräferenzen
                  </div>
                </div>
                <span
                  style={{
                    fontSize: 11,
                    padding: "2px 8px",
                    borderRadius: 999,
                    background: "#e0f2fe",
                    color: "#0369a1",
                    fontWeight: 600,
                  }}
                >
                  Optional pro Jahr
                </span>
              </div>

              <div
                style={{
                  display: "grid",
                  gridTemplateColumns: "1fr 1fr",
                  gap: 10,
                  marginBottom: 8,
                }}
              >
                <div>
                  <label style={labelStyle}>
                    Schuljahr
                    <input
                      type="text"
                      name="schoolYear"
                      placeholder="z.B. 2024/25"
                      value={form.schoolYear}
                      onChange={handleInputChange}
                      style={inputStyle}
                    />
                  </label>
                </div>
                <div>
                  <label style={labelStyle}>
                    Max. Praktika pro Jahr
                    <input
                      type="number"
                      name="maxPraktikaPerYear"
                      value={
                        form.maxPraktikaPerYear === ""
                          ? ""
                          : form.maxPraktikaPerYear
                      }
                      onChange={handleInputChange}
                      style={inputStyle}
                      min={0}
                    />
                  </label>
                </div>
                <div>
                  <label style={labelStyle}>
                    Ermäßigungsstunden gesamt
                    <input
                      type="number"
                      name="totalHoursCredit"
                      value={
                        form.totalHoursCredit === ""
                          ? ""
                          : form.totalHoursCredit
                      }
                      onChange={handleInputChange}
                      style={inputStyle}
                      min={0}
                    />
                  </label>
                </div>
                <div style={{ gridColumn: "1 / span 2" }}>
                  <label style={labelStyle}>
                    Fächerspezialisierungen
                    <input
                      type="text"
                      value={form.subjectSpecializationsInput}
                      onChange={handleSubjectSpecializationsChange}
                      placeholder="Mit Komma trennen, z.B. Deutsch, Mathematik"
                      style={inputStyle}
                    />
                  </label>
                </div>
              </div>

              <div style={{ marginTop: 4 }}>
                <span
                  style={{
                    fontSize: 12,
                    color: "#374151",
                    display: "block",
                    marginBottom: 6,
                  }}
                >
                  Praktikumspräferenzen
                </span>
                <div
                  style={{
                    display: "flex",
                    flexWrap: "wrap",
                    gap: 6,
                  }}
                >
                  {PRAKTIKUM_TYPE_OPTIONS.map((opt) => {
                    const active = form.internshipPreferences.includes(
                      opt.value
                    );
                    return (
                      <button
                        key={opt.value}
                        type="button"
                        onClick={() => handlePreferenceToggle(opt.value)}
                        style={{
                          padding: "4px 10px",
                          borderRadius: 999,
                          border: active
                            ? "1px solid #509CDB"
                            : "1px solid #d1d5db",
                          background: active ? "#eff6ff" : "#ffffff",
                          color: active ? "#1d4ed8" : "#4b5563",
                          fontSize: 12,
                          fontWeight: 500,
                          cursor: "pointer",
                        }}
                      >
                        {opt.label}
                      </button>
                    );
                  })}
                </div>
              </div>
            </div>

            <div
              className="form-actions"
              style={{
                marginTop: 12,
                display: "flex",
                gap: 8,
                justifyContent: "flex-end",
              }}
            >
              <button type="submit" className="btn-primary">
                {mode === "create" ? "PL anlegen" : "Speichern"}
              </button>
              {mode === "edit" && (
                <button
                  type="button"
                  className="btn-cancel"
                  onClick={resetForm}
                >
                  Abbrechen
                </button>
              )}
            </div>
          </form>
          </div>

          {/* Excel import card */}
          <div className="card">
            <div className="card-header">
              <h3>PLs aus Excel importieren</h3>
            </div>
            <p style={{ fontSize: 13, color: "#6f7276", marginBottom: 8 }}>
              Unterstütztes Format: <code>.xlsx</code>. Gültige Einträge werden
              gespeichert, Fehler werden unten angezeigt.
            </p>

            <div style={{ marginBottom: 8 }}>
              <input
                type="file"
                accept=".xlsx"
                onChange={handleImportFileChange}
              />
            </div>

            <div
              style={{
                display: "flex",
                justifyContent: "flex-start",
                gap: 8,
              }}
            >
              <button
                type="button"
                className="btn-primary"
                onClick={handleImportClick}
                disabled={importing}
              >
                {importing ? "Importiere..." : "Import starten"}
              </button>
            </div>

            {importResult && (
              <div
                style={{
                  marginTop: 10,
                  paddingTop: 8,
                  borderTop: "1px solid #d1d5db",
                  fontSize: 13,
                }}
              >
                <p style={{ fontWeight: 600 }}>Import-Ergebnis</p>
                <ul style={{ marginLeft: 18, marginTop: 4 }}>
                  <li>Gesamtzeilen: {importResult.totalRows}</li>
                  <li>Importiert: {importResult.importedCount}</li>
                  <li>Fehler: {importResult.errorCount}</li>
                </ul>
                {importResult.errors && importResult.errors.length > 0 && (
                  <>
                    <p style={{ marginTop: 6 }}>Fehlerprotokoll:</p>
                    <ul
                      style={{
                        marginLeft: 18,
                        marginTop: 4,
                        maxHeight: 140,
                        overflowY: "auto",
                      }}
                    >
                      {importResult.errors.map((err, idx) => (
                        <li key={idx}>{err}</li>
                      ))}
                    </ul>
                  </>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}
