import { useEffect, useState } from "react";
import StudentConfigService from "../../services/studentConfigService";
import type { StudentConfigDto } from "../../services/studentConfigService";
import "../../styles/InternshipsAssignment/StudentConfigTable.css";

interface Props {
    tabName: string;
    editMode: boolean;
    onEdit: (cfg: StudentConfigDto) => void;
}

export default function StudentConfigTable({ tabName, editMode, onEdit }: Props) {
    const [configs, setConfigs] = useState<StudentConfigDto[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        setLoading(true);
        StudentConfigService.getConfigsByYear(tabName)
            .then((data) => setConfigs(data))
            .finally(() => setLoading(false));
    }, [tabName]);

    const handleDelete = (id: number) => {
        StudentConfigService.deleteConfig(id).then(() => {
            setConfigs(prev => prev.filter(c => c.id !== id));
        });
    };

    if (loading) return <p className="loading-text">Loading configs...</p>;

    return (
        <div className="table-container student-config-table">
            <table className="schools-table">
                <thead>
                    <tr>
                        <th style={{ whiteSpace: 'nowrap' }}>Student ID</th>
                        <th style={{ whiteSpace: 'nowrap' }}>School Type</th>
                        <th style={{ whiteSpace: 'nowrap' }}>PDP I</th>
                        <th style={{ whiteSpace: 'nowrap' }}>PDP II</th>
                        <th style={{ whiteSpace: 'nowrap' }}>ZSP</th>
                        <th style={{ whiteSpace: 'nowrap' }}>SFP</th>
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
                            <td>{c.pdpI ? "✔" : "✖"}</td>
                            <td>{c.pdpII ? "✔" : "✖"}</td>
                            <td>{c.zsp ? "✔" : "✖"}</td>
                            <td>{c.sfp ? "✔" : "✖"}</td>
                            <td>{c.mainCourse}</td>
                            <td>{c.prefCourse1}, {c.prefCourse2}, {c.prefCourse3}</td>

                            {editMode && (
                                <td style={{ textAlign: 'center' }}>
                                    <button
                                        className="btn btn-sm"
                                        onClick={() => onEdit(c)}
                                    >
                                        Edit
                                    </button>

                                    {c.id !== undefined && (
                                        <button
                                            className="btn btn-sm"
                                            style={{ marginLeft: '8px', backgroundColor: '#d14d4d' }}
                                            onClick={() => handleDelete(c.id)}
                                        >
                                            Delete
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
