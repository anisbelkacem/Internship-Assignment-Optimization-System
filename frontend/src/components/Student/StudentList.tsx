import React, { useState, useEffect } from "react";
import type { Student } from "../../services/studentService";
import StudentService from "../../services/studentService";
import StudentListEntry from "./StudentListEntry";
import StudentForm from "./StudentForm";
import StudentModal from "./StudentModal";
import "../../styles/StudentStyles/StudentList.css";

const StudentList: React.FC = () => {
    const [students, setStudents] = useState<Student[]>([]);
    const [loading, setLoading] = useState(true);
    
    const [editMode, setEditMode] = useState(false);
    const [showForm, setShowForm] = useState(false);
    const [selectedStudent, setSelectedStudent] = useState<Student | null>(null);
    

    // Fetch students
    useEffect(() => {
        const load = async () => {
            try {
                const data = await StudentService.getAllStudent();
                setStudents(data);
            } catch (error) {
                console.error("Error fetching students:", error);
            } finally {
                setLoading(false);
            }
        };

        load();
    }, []);


    const handleDelete = async (matric: number) => {
        try {
            await StudentService.deleteStudent(matric);

            setStudents(prev =>
                prev.filter(s => s.matriculationNbr !== matric)
            );
        } catch (error) {
            console.error("Error deleting student:", error);
        }
    };



    if (loading) return <div>Loading students...</div>;

    return (
            <div className="student-list-container">
                <h2 className="student-list-title">Studierendet</h2>

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
