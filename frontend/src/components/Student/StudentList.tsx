import React, { useState, useEffect } from "react";
import type { Student } from "../../types/interfaces";
import StudentListEntry from "./StudentListEntry";
import StudentForm from "./StudentForm";
import StudentModal from "./StudentModal";

const StudentList: React.FC = () => {
    const [students, setStudents] = useState<Student[]>([]);
    const [loading, setLoading] = useState(true);

    const [editMode, setEditMode] = useState(false);
    const [showForm, setShowForm] = useState(false);
    const [selectedStudent, setSelectedStudent] = useState<Student | null>(null);

    const TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiRURJVCJ9LHsiYXV0aG9yaXR5IjoiTUFOQUdFX1VTRVJTIn0seyJhdXRob3JpdHkiOiJST0xFX0FETUlOIn0seyJhdXRob3JpdHkiOiJWSUVXIn1dLCJzdWIiOiJhZG1pbkBzY2hvb2wuY29tIiwiaWF0IjoxNzY0MDI2ODA2LCJleHAiOjE3NjQxMTMyMDZ9.DeHuEwv9FWS2L3l62YQ_cteXHTx7khG-CCctzMLQqOY";

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

    if (loading) return <div>Loading students...</div>;

    return (
        <div>
            <h2>Student List</h2>

            <div style={{ marginBottom: "10px" }}>
                <button onClick={() => setEditMode(!editMode)}>
                    {editMode ? "Exit Edit Mode" : "Edit Students"}
                </button>

                <button
                    style={{ marginLeft: "10px" }}
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
                    onDelete={(matric) => {
                        setStudents(students.filter((s) => s.matriculationNbr !== matric));
                    }}
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
                                        s.matriculationNbr === saved.matriculationNbr
                                            ? saved
                                            : s
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
