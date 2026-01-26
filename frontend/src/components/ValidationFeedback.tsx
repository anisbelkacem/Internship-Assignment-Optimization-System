import "../styles/ValidationFeedback.css";

type ViolationLike =
  | string
  | {
      message: string;
      code?: string;
      fields?: string[];
      severity?: "HARD" | "WARNING";
    };

function normalize(items?: ViolationLike[]) {
  if (!items) return [];
  return items
    .map((it) => (typeof it === "string" ? it : it?.message))
    .filter((m): m is string => typeof m === "string" && m.trim().length > 0);
}

type Props = {
  hardViolations?: ViolationLike[];
  warnings?: ViolationLike[];

  hardTitle?: string;
  warningTitle?: string;

  openHard?: boolean;
  openWarnings?: boolean;

  compact?: boolean;
};

export default function ValidationFeedback({
  hardViolations,
  warnings,
  hardTitle = "Nicht speicherbar",
  warningTitle = "Warnungen",
  openHard = true,
  openWarnings = false,
  compact = false,
}: Props) {
  const hard = normalize(hardViolations);
  const warn = normalize(warnings);

  if (hard.length === 0 && warn.length === 0) return null;

  return (
    <div className={`validation-stack ${compact ? "compact" : ""}`}>
      {hard.length > 0 && (
        <details className="validation-box validation-box--error" open={openHard}>
          <summary className="validation-summary">
            <span className="validation-summary-left">
              <span className="validation-summary-icon" aria-hidden>
                ⛔
              </span>
              <span className="validation-summary-title">{hardTitle}</span>
            </span>
            <span className="validation-summary-count">{hard.length}</span>
          </summary>

          <div className="validation-items">
            {hard.map((m, i) => (
              <div className="validation-item" key={`hard-${i}`}>
                <span className="validation-item-icon" aria-hidden>
                  ⛔
                </span>
                <span className="validation-item-text">{m}</span>
              </div>
            ))}
          </div>
        </details>
      )}

      {warn.length > 0 && (
        <details className="validation-box validation-box--warning" open={openWarnings}>
          <summary className="validation-summary">
            <span className="validation-summary-left">
              <span className="validation-summary-icon" aria-hidden>
                ⚠️
              </span>
              <span className="validation-summary-title">{warningTitle}</span>
            </span>
            <span className="validation-summary-count">{warn.length}</span>
          </summary>

          <div className="validation-items">
            {warn.map((m, i) => (
              <div className="validation-item" key={`warn-${i}`}>
                <span className="validation-item-icon" aria-hidden>
                  ⚠️
                </span>
                <span className="validation-item-text">{m}</span>
              </div>
            ))}
          </div>
        </details>
      )}
    </div>
  );
}
