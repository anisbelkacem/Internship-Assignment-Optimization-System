import React, { useState } from "react";
import type { Student } from "../../types/interfaces";
import CompletedInternshipsTable from "./CompletedInternshipsTable";
import "./StudentListEntry.css";
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
        <div className="student-entry">
            <div
                className="student-entry-header"
                onClick={() => setExpanded(!expanded)}
            >
                {student.firstName} {student.lastName} ({student.matriculationNbr})
            </div>

            {expanded && (
                <div className="student-entry-content">
                    {/* Basic info */}
                    <p>Email: {student.email}</p>
                    <p>Phone: {student.phone || "N/A"}</p>
                    <p>Birth Date: {student.birthDate || "N/A"}</p>
                    <p>School Type: {student.schoolType}</p>
                    <p>Main Course: {student.mainCourse}</p>
                    <p>Preferences: {student.prefCourse1}, {student.prefCourse2}, {student.prefCourse3}</p>
                    <p>Registered: {student.registred ? "Yes" : "No"}</p>
                    <p>Oriented: {student.oriented ? "Yes" : "No"}</p>
                    <p>Description: {student.description || "N/A"}</p>

                    {/* Address */}
                    <h5>Address</h5>
                    <p>Street: {student.address?.street || "N/A"}</p>
                    <p>City: {student.address?.city || "N/A"}</p>
                    <p>House Number: {student.address?.houseNbr || "N/A"}</p>
                    <p>Postal Code: {student.address?.postalCode || "N/A"}</p>
                    <p>Country: {student.address?.country || "N/A"}</p>

                    {/* Semester Address */}
                    <h5>Semester Address</h5>
                    <p>Street: {student.addressSemester?.street || "N/A"}</p>
                    <p>City: {student.addressSemester?.city || "N/A"}</p>
                    <p>House Number: {student.addressSemester?.houseNbr || "N/A"}</p>
                    <p>Postal Code: {student.addressSemester?.postalCode || "N/A"}</p>
                    <p>Country: {student.addressSemester?.country || "N/A"}</p>

                    {/* Completed internships */}
                    <h5>Completed Internships</h5>
                    <div className="completed-internships">
                        <CompletedInternshipsTable studentId={student.matriculationNbr} />
                    </div>
                    {/* Edit/Delete actions */}
                    {editMode && (
                        <div className="student-entry-actions">
                            <button
                                className="student-btn student-btn-update"
                                onClick={() => onEdit && onEdit(student)}
                            >
                                Update
                            </button>

                            <button
                                className="student-btn student-btn-delete"
                                onClick={() => onDelete && onDelete(student.matriculationNbr)}
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
