import type { FC } from "react";

type Props = { title: string; value: string; small?: string };

const StatCard: FC<Props> = ({ title, value, small }) => {
  return (
    <div className="stat-card">
      <div className="stat-title">{title}</div>
      <div className="stat-value">{value}</div>
      {small && <div className="stat-small">{small}</div>}
    </div>
  );
};

export default StatCard;
