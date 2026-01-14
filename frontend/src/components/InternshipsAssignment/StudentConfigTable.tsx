import { useEffect, useState } from "react";
import StudentConfigService from "../../services/studentConfigService";
import type { StudentConfigDto } from "../../services/studentConfigService";
import "../../styles/InternshipsAssignment/StudentConfigTable.css";

interface Props {
    tabName: string;
    editMode: boolean;
    onEdit: (cfg: StudentConfigDto) => void;
     refreshKey: number;
}

export default function StudentConfigTable({ tabName, editMode, onEdit ,refreshKey}: Props) {
    const [configs, setConfigs] = useState<StudentConfigDto[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        setLoading(true);
        StudentConfigService.getConfigsByYear(tabName)
            .then((data) => setConfigs(data))
            .finally(() => setLoading(false));
    }, [tabName,refreshKey]);

    const handleDelete = (id: number) => {
        StudentConfigService.deleteConfig(id).then(() => {
            setConfigs(prev => prev.filter(c => c.id !== id));
        });
    };

    if (loading) return <p className="loading-text">Loading configs...</p>;

    const renderInternships = (cfg: StudentConfigDto) => {
        const labels: string[] = [];
        if (cfg.pdpI) labels.push("PDP I");
        if (cfg.pdpII) labels.push("PDP II");
        if (cfg.zsp) labels.push("ZSP");
        if (cfg.sfp) labels.push("SFP");
        if (labels.length === 0) return <span className="muted-dash">-</span>;
        return (
            <div className="pill-row">
                {labels.map(label => (
                    <span key={label} className="internship-pill">{label}</span>
                ))}
            </div>
        );
    };

    return (
        <div className="table-container student-config-table">
            <table className="schools-table">
                <thead>
                    <tr>
                        <th style={{ whiteSpace: 'nowrap' }}>Student ID</th>
                        <th style={{ whiteSpace: 'nowrap' }}>School Type</th>
                        <th style={{ whiteSpace: 'nowrap' }}>Praktika</th>
                        <th style={{ whiteSpace: 'nowrap' }}>Main Course</th>
                        <th style={{ whiteSpace: 'nowrap' }}>Preferred Courses</th>
                        {editMode && <th style={{ whiteSpace: 'nowrap', textAlign: 'center' }}>Actions</th>}
                    </tr>
                </thead>

                <tbody>
                    {configs.map((c) => (
                        <tr key={c.id}>
                            <td>{c.studentId}</td>
                            <td>{c.schoolType}</td>
                            <td>{renderInternships(c)}</td>
                            {/* Fix: Access the name property of Course objects */}
                            <td>{c.mainCourse?.name ?? "-"}</td>
                            <td>
                                {[c.prefCourse1, c.prefCourse2, c.prefCourse3]
                                    .map(course => course?.name ?? "-")
                                    .join(", ")}
                            </td>

                            {editMode && (
                                <td style={{ textAlign: 'center' }}>
                                    <button
                                            className="action-btn edit-btn"
                                            onClick={() => onEdit(c)}
                                            title="Edit"
                                    >
                                            ✏️
                                    </button>

                                    {c.id !== undefined && (
                                        <button
                                                className="action-btn delete-btn"
                                                onClick={() => handleDelete(c.id)}
                                                title="Delete"
                                        >
                                                🗑️
                                        </button>
                                    )}
                                </td>
                            )}
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}