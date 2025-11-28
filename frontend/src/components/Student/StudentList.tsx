import React, { useState, useEffect } from "react";
import type { Student } from "../../types/interfaces";
import StudentListEntry from "./StudentListEntry";
import StudentForm from "./StudentForm";
import StudentModal from "./StudentModal";
import "./StudentList.css";

const StudentList: React.FC = () => {
    const [students, setStudents] = useState<Student[]>([]);
    const [loading, setLoading] = useState(true);
    
    const [editMode, setEditMode] = useState(false);
    const [showForm, setShowForm] = useState(false);
    const [selectedStudent, setSelectedStudent] = useState<Student | null>(null);
    
    const TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiRURJVCJ9LHsiYXV0aG9yaXR5IjoiTUFOQUdFX1VTRVJTIn0seyJhdXRob3JpdHkiOiJST0xFX0FETUlOIn0seyJhdXRob3JpdHkiOiJWSUVXIn1dLCJzdWIiOiJhZG1pbkBzY2hvb2wuY29tIiwiaWF0IjoxNzY0MzU5MTM2LCJleHAiOjE3NjQ0NDU1MzZ9.0IAfemkdN9vGFCsLq02O0kntSZUbvr6M8nKt2daR8-Y";

    // Fetch students
    useEffect(() => {
        const fetchStudents = async () => {
            try {
                const response = await fetch("/api/students", {
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${TOKEN}`,
                    },
                });
                if (!response.ok) throw new Error("Failed to fetch students");
                const data = await response.json();
                setStudents(data);
            } catch (error) {
                console.error("Error fetching students:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchStudents();
    }, []);

    const handleDelete = async (matric: number) => {
        try {
            const response = await fetch(`/api/students/${matric}`, {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${TOKEN}`,
                },
            });

            if (!response.ok) throw new Error("Failed to delete student");

            setStudents((prev) => prev.filter((s) => s.matriculationNbr !== matric));
        } catch (error) {
            console.error("Error deleting student:", error);
        }
    };


    if (loading) return <div>Loading students...</div>;

    return (
        <div className="student-list-container">
            <h2 className="student-list-title">Student List</h2>

            <div className="student-list-controls">
                <button className="student-btn" onClick={() => setEditMode(!editMode)}>
                    {editMode ? "Exit Edit Mode" : "Edit Students"}
                </button>

                <button
                    className="student-btn student-btn-create"
                    onClick={() => {
                        setSelectedStudent(null);
                        setShowForm(true);
                    }}
                >
                    Create Student
                </button>
            </div>

            {students.map((student) => (
                <StudentListEntry
                    key={student.matriculationNbr}
                    student={student}
                    editMode={editMode}
                    onEdit={(s) => {
                        setSelectedStudent(s);
                        setShowForm(true);
                    }}
                    onDelete={handleDelete} // works with number now
                />
            ))}

            {showForm && (
                <StudentModal onClose={() => setShowForm(false)}>
                    <StudentForm
                        student={selectedStudent}
                        onClose={() => setShowForm(false)}
                        onSave={(saved) => {
                            setStudents((prev) => {
                                const exists = prev.some(
                                    (s) => s.matriculationNbr === saved.matriculationNbr
                                );
                                if (exists) {
                                    return prev.map((s) =>
                                        s.matriculationNbr === saved.matriculationNbr ? saved : s
                                    );
                                } else {
                                    return [...prev, saved];
                                }
                            });
                            setShowForm(false);
                        }}
                    />
                </StudentModal>
            )}
        </div>
    );
};

export default StudentList;
