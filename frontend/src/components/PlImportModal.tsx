import React, { useState } from "react";
import plService,{ type TeacherImportResult } from "../services/plService";

export interface PlImportModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (msg: string) => void;
  onError: (msg: string) => void;
  onImported: () => Promise<void> | void; // reload PLs
}

const PlImportModal: React.FC<PlImportModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
  onError,
  onImported,
}) => {
  const [file, setFile] = useState<File | null>(null);
  const [importResult, setImportResult] = useState<TeacherImportResult | null>(
    null
  );
  const [importing, setImporting] = useState(false);

  if (!isOpen) return null;

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const f = e.target.files?.[0] ?? null;
    setFile(f);
    setImportResult(null);
  }

  async function handleImportClick() {
    if (!file) {
      onError("Bitte zuerst eine .xlsx-Datei auswählen.");
      return;
    }

    setImporting(true);
    setImportResult(null);

    try {
      const result = await plService.importFromExcel(file);
      setImportResult(result);

      await onImported();
      onSuccess(
        `Import abgeschlossen: ${result.importedCount} von ${result.totalRows} Zeilen importiert.`
      );
    } catch (err) {
      console.error(err);
      onError("Fehler beim Import der PLs aus Excel.");
    } finally {
      setImporting(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div
        className="modal-content"
        onClick={(e) => e.stopPropagation()}
        style={{ maxWidth: "640px" }}
      >
        <div className="modal-header">
          <h2>PLs aus Excel importieren</h2>
        </div>

        <div className="modal-body">
          <div className="form-group">
            <label className="form-label required" htmlFor="pl-import-file">
              Excel-Datei (.xlsx)
            </label>
            <input
              id="pl-import-file"
              type="file"
              className="form-input"
              accept=".xlsx"
              onChange={handleFileChange}
              required
            />
            {file && (
              <p style={{ fontSize: '0.875rem', color: '#059669', marginTop: '0.5rem', fontWeight: 500 }}>
                Ausgewählte Datei: {file.name}
              </p>
            )}
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginTop: '0.5rem' }}>
              Die Excel-Datei sollte folgende Spalten enthalten: Vorname, Nachname, E-Mail, Hauptfach, Schulname
            </p>
          </div>

          <div
            style={{
              display: "flex",
              justifyContent: "flex-end",
              gap: 8,
              marginTop: 12,
            }}
          >
            <button
              type="button"
              className="btn-cancel"
              onClick={onClose}
              disabled={importing}
            >
              Schließen
            </button>
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
                marginTop: 14,
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
  );
};

export default PlImportModal;
