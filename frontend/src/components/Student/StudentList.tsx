import React, { useEffect, useMemo, useState } from "react";
import type { Student } from "../../services/studentService";
import StudentService from "../../services/studentService";
import type { Course } from "../../services/courseService";
import CourseService from "../../services/courseService";
import SearchFilter, { type FilterConfig } from "../SearchFilter";
import StudentForm from "./StudentForm";
import StudentModal from "./StudentModal";
import "../../styles/StudentStyles/StudentList.css";

const StudentList: React.FC = () => {
    const [students, setStudents] = useState<Student[]>([]);
    const [courses, setCourses] = useState<Course[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState("");
    const [filters, setFilters] = useState<Record<string, string>>({
        schoolType: '',
        mainCourse: '',
        registred: '',
        oriented: '',
    });
    const [showForm, setShowForm] = useState(false);
    const [selectedStudent, setSelectedStudent] = useState<Student | null>(null);

    // Fetch students and courses together
    useEffect(() => {
        const load = async () => {
            try {
                const [studentsData, coursesData] = await Promise.all([
                    StudentService.getAllStudent(),
                    CourseService.getAllCourses()
                ]);
                setStudents(studentsData);
                setCourses(coursesData);
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

    const filteredStudents = useMemo(() => {
        return students.filter(student => {
            const fullName = `${student.firstName} ${student.lastName}`.toLowerCase();
            const matchesSearch = fullName.includes(searchTerm.toLowerCase()) ||
                                 student.matriculationNbr.toString().includes(searchTerm);
            const matchesSchoolType = !filters.schoolType || student.schoolType === filters.schoolType;
            const matchesMainCourse = !filters.mainCourse || 
                                     (student.mainCourseId?.toString() === filters.mainCourse);
            const matchesRegistred = !filters.registred ||
                                    (filters.registred === 'true' && student.registred) ||
                                    (filters.registred === 'false' && !student.registred);
            const matchesOriented = !filters.oriented ||
                                   (filters.oriented === 'true' && student.oriented) ||
                                   (filters.oriented === 'false' && !student.oriented);
            
            return matchesSearch && matchesSchoolType && matchesMainCourse && 
                   matchesRegistred && matchesOriented;
        });
    }, [students, searchTerm, filters]);

    const courseNameById = useMemo(() => {
        const map = new Map<number, string>();
        courses.forEach(course => {
            map.set(course.id, course.name);
        });
        return map;
    }, [courses]);

    const renderCourse = (courseId?: number | null) => {
        if (!courseId) return "-";
        const courseName = courseNameById.get(courseId);
        if (!courseName) return courseId;
        return courseName.charAt(0).toUpperCase() + courseName.slice(1);
    };

    // Build filter configurations
    const filterConfigs: FilterConfig[] = useMemo(() => [
        {
            field: 'schoolType',
            label: 'Schultyp',
            options: [
                { label: 'Grundschule (GS)', value: 'GS', field: 'schoolType' },
                { label: 'Mittelschule (MS)', value: 'MS', field: 'schoolType' }
            ]
        },
        {
            field: 'mainCourse',
            label: 'Hauptfach',
            options: courses
                .filter(c => c.active)
                .map(course => ({
                    label: course.name.charAt(0).toUpperCase() + course.name.slice(1),
                    value: course.id.toString(),
                    field: 'mainCourse'
                }))
        },
        {
            field: 'registred',
            label: 'Registriert',
            options: [
                { label: 'Ja', value: 'true', field: 'registred' },
                { label: 'Nein', value: 'false', field: 'registred' }
            ]
        },
        {
            field: 'oriented',
            label: 'Orientiert',
            options: [
                { label: 'Ja', value: 'true', field: 'oriented' },
                { label: 'Nein', value: 'false', field: 'oriented' }
            ]
        }
    ], [courses]);

    const handleFilterChange = (field: string, value: string) => {
        setFilters(prev => ({ ...prev, [field]: value }));
    };

    const handleClearFilters = () => {
        setFilters({ schoolType: '', mainCourse: '', registred: '', oriented: '' });
    };

    if (loading) return <div className="assign-root"><p>Loading students...</p></div>;

    return (
        <div className="assign-root">
            {/* Header */}
            <div className="assign-header">
                <h1>Studierende</h1>
                <div className="header-actions">
                    <button
                        className="btn-primary-filled"
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
                {/* Search and Filter */}
                <SearchFilter
                    searchPlaceholder="Suche nach Name oder Matrikelnummer..."
                    searchValue={searchTerm}
                    onSearchChange={setSearchTerm}
                    filters={filterConfigs}
                    activeFilters={filters}
                    onFilterChange={handleFilterChange}
                    onClearFilters={handleClearFilters}
                />

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
                                        <td>{renderCourse(student.mainCourseId)}</td>
                                        <td>{renderCourse(student.prefCourse1Id)}</td>
                                        <td>{renderCourse(student.prefCourse2Id)}</td>
                                        <td>{renderCourse(student.prefCourse3Id)}</td>
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
