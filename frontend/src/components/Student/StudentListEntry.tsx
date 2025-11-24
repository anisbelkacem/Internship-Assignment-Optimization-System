import React, { useState } from "react";
import type { Student } from "../../types/interfaces";

interface Props {
    student: Student;
    editMode?: boolean;
    onEdit?: (student: Student) => void;
    onDelete?: (matricNbr: number) => void;
}

const StudentListEntry: React.FC<Props> = ({
    student,
    editMode = false,
    onEdit,
    onDelete,
}) => {
    const [expanded, setExpanded] = useState(false);

    return (
        <div
            style={{
                border: "1px solid gray",
                borderRadius: "4px",
                marginBottom: "10px",
                padding: "10px",
            }}
        >
            <div
                style={{ cursor: "pointer", fontWeight: "bold" }}
                onClick={() => setExpanded(!expanded)}
            >
                {student.firstName} {student.lastName} ({student.matriculationNbr})
            </div>

            {expanded && (
                <div style={{ marginTop: "10px", paddingLeft: "10px" }}>
                    <p>Email: {student.email}</p>
                    <p>Phone: {student.phone || "N/A"}</p>
                    <p>Birth Date: {student.birthDate || "N/A"}</p>
                    <p>School Type: {student.schoolType}</p>
                    <p>Main Course: {student.mainCourse}</p>
                    <p>
                        Preferences: {student.prefCourse1}, {student.prefCourse2},{" "}
                        {student.prefCourse3}
                    </p>
                    <p>Registered: {student.registred ? "Yes" : "No"}</p>
                    <p>Oriented: {student.oriented ? "Yes" : "No"}</p>
                    <p>Description: {student.description || "N/A"}</p>
                    {editMode && (
                        <div style={{ marginTop: "10px" }}>
                            <button onClick={() => onEdit && onEdit(student)}>Update</button>
                            <button
                                onClick={() => onDelete && onDelete(student.matriculationNbr)}
                                style={{ marginLeft: "5px" }}
                            >
                                Delete
                            </button>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default StudentListEntry;
