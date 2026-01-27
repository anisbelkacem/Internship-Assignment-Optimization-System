import type { FC } from "react";

const PLTable: FC = () => {
  const rows = [
    { name: "Dr. Anna Müller", school: "GS St. Nikola", fach: "Deutsch", prefs: "PDP I, SFP", zone: 1, opnv: "Yes", hours: 6 },
    { name: "Herr Markus Klein", school: "MS Hohenau", fach: "Mathematik", prefs: "PDP II", zone: 3, opnv: "No", hours: 4 },
  ];

  return (
    <div className="pl-table card">
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Schule</th>
            <th>Fach</th>
            <th>Preferierte Praktika</th>
            <th>Zone</th>
            <th>ÖPNV</th>
            <th>Stunden</th>
            <th>Aktionen</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((r, i) => (
            <tr key={i}>
              <td>{r.name}</td>
              <td>{r.school}</td>
              <td>{r.fach}</td>
              <td>{r.prefs}</td>
              <td>{r.zone}</td>
              <td>{r.opnv}</td>
              <td>{r.hours}</td>
              <td>
                <button className="link">Bearbeiten</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default PLTable;
