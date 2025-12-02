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
        <div className="config-table-wrapper">
            <table className="config-table">
                <thead>
                    <tr>
                        <th>Student ID</th>
                        <th>School Type</th>
                        <th>PDP I</th>
                        <th>PDP II</th>
                        <th>ZSP</th>
                        <th>SFP</th>
                        <th>Main Course</th>
                        <th>Preferred Courses</th>
                        {editMode && <th>Actions</th>}
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
                                <td>
                                    <button
                                        className="edit-btn"
                                        onClick={() => onEdit(c)}
                                    >
                                        Edit
                                    </button>

                                    {c.id !== undefined && (
                                        <button
                                            className="delete-btn"
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
