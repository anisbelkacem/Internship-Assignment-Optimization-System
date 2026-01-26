import "../styles/InternshipsAssignment/InternshipAssignmentModal.css";
import ValidationFeedback from "./ValidationFeedback";

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

        <ValidationFeedback
          hardViolations={hardViolations}
          warnings={warnings}
          hardTitle="Harte Regeln"
          warningTitle="Warnungen"
          openHard={true}
          openWarnings={warnings && warnings.length > 0}
          compact
        />


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
