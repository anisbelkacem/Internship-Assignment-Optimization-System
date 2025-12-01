import { useEffect, useState } from "react";
import StudentConfigService from "../../services/studentConfigService";
import type { StudentConfigDto } from "../../services/studentConfigService";
import type { SchoolType, Course } from "../../services/studentService";


export default function StudentConfigTable({ tabName, editMode, onEdit }) {
    const [configs, setConfigs] = useState<StudentConfigDto[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        setLoading(true);
        StudentConfigService.getConfigsByYear(tabName)
            .then((data) => setConfigs(data))
            .finally(() => setLoading(false));
    }, [tabName]);
    const handleEdit = (config: StudentConfigDto) => {
        console.log("Edit clicked", config);
        // later open a modal or side panel
    };

    const handleDelete = (id: number) => {
        StudentConfigService.deleteConfig(id).then(() => {
            setConfigs(prev => prev.filter(c => c.id !== id));
        });
    };

    if (loading) return <p style={{ color: "white" }}>Loading configs...</p>;

    return (
        <div style={{ overflowX: "auto", backgroundColor: "rgb(30, 45, 100)", padding: "8px", borderRadius: "4px" }}>
            <table style={{ width: "100%", borderCollapse: "collapse", color: "white" }}>
                <thead>
                    <tr>
                        <th style={thStyle}>Student ID</th>
                        <th style={thStyle}>School Type</th>
                        <th style={thStyle}>PDP I</th>
                        <th style={thStyle}>PDP II</th>
                        <th style={thStyle}>ZSP</th>
                        <th style={thStyle}>SFP</th>
                        <th style={thStyle}>Main Course</th>
                        <th style={thStyle}>Preferred Courses</th>
                        {editMode && <th style={thStyle}>Actions</th>}

                    </tr>
                </thead>
                <tbody>
                    {configs.map((c) => (
                        <tr key={c.id} style={trStyle}>
                            <td style={tdStyle}>{c.studentId}</td>
                            <td style={tdStyle}>{c.schoolType}</td>
                            <td style={tdStyle}>{c.pdpI ? "✔" : "✖"}</td>
                            <td style={tdStyle}>{c.pdpII ? "✔" : "✖"}</td>
                            <td style={tdStyle}>{c.zsp ? "✔" : "✖"}</td>
                            <td style={tdStyle}>{c.sfp ? "✔" : "✖"}</td>
                            <td style={tdStyle}>{c.mainCourse}</td>
                            <td style={tdStyle}>
                                {c.prefCourse1}, {c.prefCourse2}, {c.prefCourse3}
                            </td>
                            {editMode && (
                                <td style={tdStyle}>
                                    <button style={editBtn} onClick={() => onEdit(c)}>Edit</button>
                                    <button style={deleteBtn} onClick={() => handleDelete(c.id)}>Delete</button>
                                </td>
                            )}

                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

const thStyle: React.CSSProperties = {
    padding: "8px",
    textAlign: "left",
    borderBottom: "2px solid #517c9e",
};

const tdStyle: React.CSSProperties = {
    padding: "8px",
    borderBottom: "1px solid #517c9e",
};

const trStyle: React.CSSProperties = {
    // Optional: highlight row on hover
    transition: "background 0.2s",
};

const editBtn: React.CSSProperties = {
    marginRight: "8px",
    padding: "4px 8px",
    background: "#2b8df0",
    color: "white",
    border: "none",
    borderRadius: "4px",
    cursor: "pointer",
};

const deleteBtn: React.CSSProperties = {
    padding: "4px 8px",
    background: "#d14d4d",
    color: "white",
    border: "none",
    borderRadius: "4px",
    cursor: "pointer",
};
