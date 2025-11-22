import type { FC } from "react";

type Item = { label: string; needed: string; assigned?: string };
type Props = { title: string; items: Item[] };

const PlanningCard: FC<Props> = ({ title, items }) => {
  return (
    <div className="planning-card">
      <div className="pc-title">{title}</div>
      <div className="pc-items">
        {items.map((it, idx) => (
          <div key={idx} className="pc-item">
            <div className="pc-item-label">{it.label}</div>
            <div className="pc-item-info">
              {it.needed} <span className="pc-assigned">Assigned: {it.assigned ?? "-"}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default PlanningCard;
