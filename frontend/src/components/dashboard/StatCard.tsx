import type { ReactNode } from "react";

export function StatCard({
  label,
  value,
  icon,
  accent = "blue",
}: {
  label: string;
  value: string;
  icon: ReactNode;
  accent?: "blue" | "teal" | "orange" | "navy";
}) {
  return (
    <article className="stat-card">
      <div className={`stat-icon ${accent}`}>{icon}</div>
      <div>
        <span>{label}</span>
        <strong>{value}</strong>
        <small>Os dados aparecerão após os registros.</small>
      </div>
    </article>
  );
}
