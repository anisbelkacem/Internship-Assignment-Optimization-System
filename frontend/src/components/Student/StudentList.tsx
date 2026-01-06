import React, { useState, useEffect } from "react";
import type { Student } from "../../services/studentService";
import StudentService from "../../services/studentService";
import StudentForm from "./StudentForm";
import StudentModal from "./StudentModal";
import "../../styles/StudentStyles/StudentList.css";

const StudentList: React.FC = () => {
    const [students, setStudents] = useState<Student[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState("");
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
        if (!window.confirm("Delete this student?")) return;
        try {
            await StudentService.deleteStudent(matric);
            setStudents(prev => prev.filter(s => s.matriculationNbr !== matric));
        } catch (error) {
            console.error("Error deleting student:", error);
        }
    };

    const handleSave = (saved: Student) => {
        setStudents((prev) => {
            const exists = prev.some(s => s.matriculationNbr === saved.matriculationNbr);
            if (exists) {
                return prev.map(s => s.matriculationNbr === saved.matriculationNbr ? saved : s);
            }
            return [...prev, saved];
        });
        setShowForm(false);
        setSelectedStudent(null);
    };

    const filteredStudents = students.filter(student => {
        const fullName = `${student.firstName} ${student.lastName}`.toLowerCase();
        return fullName.includes(searchTerm.toLowerCase());
    });

    if (loading) return <div className="assign-root"><p>Loading students...</p></div>;

    return (
        <div className="assign-root">
            {/* Header */}
            <div className="assign-header">
                <h1>Studierende</h1>
                <div className="header-actions">
                    <button
                        className="btn btn-primary"
                        onClick={() => {
                            setSelectedStudent(null);
                            setShowForm(true);
                        }}
                    >
                        Add Student
                    </button>
                </div>
            </div>

            {/* Main Content Section */}
            <section className="section-container">
                {/* Search Bar */}
                <div style={{marginBottom: '20px'}}>
                    <input
                        type="text"
                        placeholder="🔍 Search students..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        style={{
                            width: '100%',
                            padding: '12px 16px',
                            fontSize: '14px',
                            border: '1px solid #e2e8f0',
                            borderRadius: '8px',
                            outline: 'none',
                            transition: 'all 0.2s ease',
                            backgroundColor: '#f8fafc',
                            boxShadow: '0 1px 2px rgba(0, 0, 0, 0.05)',
                            color: '#000000'
                        }}
                        onFocus={(e) => {
                            e.target.style.borderColor = '#3b82f6';
                            e.target.style.backgroundColor = '#ffffff';
                            e.target.style.boxShadow = '0 0 0 3px rgba(59, 130, 246, 0.1)';
                        }}
                        onBlur={(e) => {
                            e.target.style.borderColor = '#e2e8f0';
                            e.target.style.backgroundColor = '#f8fafc';
                            e.target.style.boxShadow = '0 1px 2px rgba(0, 0, 0, 0.05)';
                        }}
                    />
                </div>

                {/* Students Table */}
                <div className="table-container" style={{overflowX: 'auto'}}>
                    <table className="schools-table">
                        <thead>
                            <tr>
                                <th>NAME</th>
                                <th>MATRIC</th>
                                <th>SCHOOL</th>
                                <th>MAIN</th>
                                <th>COURSE 1</th>
                                <th>COURSE 2</th>
                                <th>COURSE 3</th>
                                <th>ACTIONS</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredStudents.length === 0 ? (
                                <tr>
                                    <td colSpan={8} className="empty-state">
                                        {searchTerm ? "No students match your search" : "No students found"}
                                    </td>
                                </tr>
                            ) : (
                                filteredStudents.map((student) => (
                                    <tr key={student.matriculationNbr}>
                                        <td>{student.firstName} {student.lastName}</td>
                                        <td>{student.matriculationNbr}</td>
                                        <td>{student.schoolType}</td>
                                        <td>{student.mainCourseId ?? '-'}</td>
                                        <td>{student.prefCourse1Id ?? '-'}</td>
                                        <td>{student.prefCourse2Id ?? '-'}</td>
                                        <td>{student.prefCourse3Id ?? '-'}</td>
                                        <td>
                                            <div style={{display: 'flex', gap: '8px', alignItems: 'center'}}>
                                                <button
                                                    className="action-btn edit-btn"
                                                    onClick={() => {
                                                        setSelectedStudent(student);
                                                        setShowForm(true);
                                                    }}
                                                    title="Edit student"
                                                >
                                                    ✏️
                                                </button>
                                                <button
                                                    className="action-btn delete-btn"
                                                    onClick={() => handleDelete(student.matriculationNbr)}
                                                    title="Delete student"
                                                >
                                                    🗑️
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </section>

            {/* Modal Form */}
            {showForm && (
                <StudentModal onClose={() => setShowForm(false)}>
                    <StudentForm
                        student={selectedStudent}
                        onClose={() => setShowForm(false)}
                        onSave={handleSave}
                    />
                </StudentModal>
            )}
        </div>
    );
};

export default StudentList;
