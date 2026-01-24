import "../styles/InternshipsAssignment/InternshipAssignmentModal.css";

type ForceSaveModalProps = {
  title?: string;
  message?: string;
  hardViolations?: string[];
  warnings?: string[];
  onCancel: () => void;
  onConfirm: () => void;
  confirmText?: string;
};

export default function ForceSaveModal({
  title = "Trotzdem speichern?",
  message = "Diese Änderung verletzt mindestens eine harte Regel. Möchten Sie trotzdem speichern?",
  hardViolations,
  warnings,
  onCancel,
  onConfirm,
  confirmText = "Trotzdem speichern",
}: ForceSaveModalProps) {
  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <h3 className="modal-title">{title}</h3>

        <p style={{ marginTop: 8, marginBottom: 12 }}>{message}</p>

        {hardViolations && hardViolations.length > 0 && (
          <div className="error-container" style={{ marginBottom: 12 }}>
            <strong>Harte Regeln:</strong>
            <ul style={{ margin: "8px 0 0 18px" }}>
              {hardViolations.map((m, i) => <li key={i}>{m}</li>)}
            </ul>
          </div>
        )}

        {warnings && warnings.length > 0 && (
          <div className="warning-container" style={{ marginBottom: 12 }}>
            <strong>Warnungen:</strong>
            <ul style={{ margin: "8px 0 0 18px" }}>
              {warnings.map((m, i) => <li key={i}>{m}</li>)}
            </ul>
          </div>
        )}

        <div className="modal-actions">
          <button
            className="modal-btn modal-btn-cancel"
            onClick={onCancel}
            type="button"
          >
            Abbrechen
          </button>

          <button
            className="modal-btn modal-btn-primary"
            onClick={onConfirm}
            type="button"
            style={{ whiteSpace: "normal", lineHeight: 1.2, textAlign: "center" }}
          >
            {confirmText}
          </button>
        </div>

      </div>
    </div>
  );
}
