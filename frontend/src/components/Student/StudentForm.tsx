import React, { useEffect, useState } from "react";
import type { Student, Address, SchoolType } from "../../services/studentService";
import courseService from "../../services/courseService";
import type { Course } from "../../services/courseService";
import StudentService from "../../services/studentService";
import "../../styles/StudentStyles/StudentForm.css";


interface Props {
    student?: Student | null;
    onClose: () => void;
    onSave: (student: Student) => void;
}


const emptyAddress: Address = {
    street: "",
    city: "",
    houseNbr: "",
    postalCode: "",
    country: "Deutschland",
};

const StudentForm: React.FC<Props> = ({ student, onClose, onSave }) => {
    const [form, setForm] = useState<Student>({
        matriculationNbr: student?.matriculationNbr || 0,
        firstName: student?.firstName || "",
        lastName: student?.lastName || "",
        email: student?.email || "",
        schoolType: student?.schoolType || "GS",
        mainCourseId: student?.mainCourseId ?? null,
        prefCourse1Id: student?.prefCourse1Id ?? null,
        prefCourse2Id: student?.prefCourse2Id ?? null,
        prefCourse3Id: student?.prefCourse3Id ?? null,
        registred: student?.registred || false,
        oriented: student?.oriented || false,
        address: student?.address || { ...emptyAddress },
        addressSemester: student?.addressSemester || { ...emptyAddress },
        phone: student?.phone || "",
        birthDate: student?.birthDate || "",
        description: student?.description || "",
    });

    const [courses, setCourses] = useState<Course[]>([]);

    useEffect(() => {
        courseService.getAllCourses()
            .then(data => {
                setCourses(data.filter(course => course.active));
            })
            .catch(err => console.error("Failed to load courses", err));
    }, []);
    

    const handleChange = (key: keyof Student, value: any) => {
        setForm(prev => ({ ...prev, [key]: value }));
    };

    const handleAddressChange = (
        key: keyof Address,
        value: string,
        type: "address" | "addressSemester"
    ) => {
        setForm(prev => ({
            ...prev,
            [type]: {
                ...prev[type],
                [key]: value,
            },
        }));
    };

    const handleSubmit = async () => {
        try {
            let saved: Student;

            if (student) {
                saved = await StudentService.updateStudent(
                    student.matriculationNbr,
                    form
                );
            } else {
                saved = await StudentService.createStudent(form);
            }

            onSave(saved);
        } catch (err) {
            console.error(err);
        }

    };

    return (
        <div className="student-form-container">
            <h3 className="student-form-title">
                {student ? "Update Student" : "Create Student"}
            </h3>

            <div className="student-field">
                <label className="required" htmlFor="matriculationNbr">Matriculation Number</label>
                <input
                    id="matriculationNbr"
                    className="student-input"
                    type="number"
                    value={form.matriculationNbr}
                    onChange={e => handleChange("matriculationNbr", Number(e.target.value))}
                    required
                />
            </div>

            <div className="student-field">
                <label className="required" htmlFor="firstName">First Name</label>
                <input
                    id="firstName"
                    className="student-input"
                    value={form.firstName}
                    onChange={e => handleChange("firstName", e.target.value)}
                    required
                />
            </div>

            <div className="student-field">
                <label className="required" htmlFor="lastName">Last Name</label>
                <input
                    id="lastName"
                    className="student-input"
                    value={form.lastName}
                    onChange={e => handleChange("lastName", e.target.value)}
                    required
                />
            </div>

            <div className="student-field">
                <label className="required" htmlFor="email">Email</label>
                <input
                    id="email"
                    className="student-input"
                    value={form.email}
                    onChange={e => handleChange("email", e.target.value)}
                    required
                />
            </div>

            <div className="student-field">
                <label htmlFor="phone">Phone</label>
                <input
                    id="phone"
                    className="student-input"
                    value={form.phone}
                    onChange={e => handleChange("phone", e.target.value)}
                />
            </div>

            <div className="student-field">
                <label htmlFor="birthDate">Birth Date</label>
                <input
                    id="birthDate"
                    className="student-input"
                    type="date"
                    value={form.birthDate}
                    onChange={e => handleChange("birthDate", e.target.value)}
                />
            </div>

            <div className="student-field full-width">
                <label htmlFor="description">Description</label>
                <textarea
                    id="description"
                    className="student-input"
                    value={form.description}
                    onChange={e => handleChange("description", e.target.value)}
                />
            </div>

            <div className="student-field">
                <label className="required" htmlFor="schoolType">School Type</label>
                <select
                    id="schoolType"
                    className="student-input"
                    value={form.schoolType}
                    onChange={e => handleChange("schoolType", e.target.value as SchoolType)}
                    required
                >
                    <option value="GS">GS</option>
                    <option value="HS">MS</option>
                </select>
            </div>

            <div className="student-field">
            <label htmlFor="mainCourse">Main Course</label>
            <select
                id="mainCourse"
                className="student-input"
                value={form.mainCourseId ?? ""}
                onChange={e =>
                handleChange("mainCourseId", Number(e.target.value))
                }
            >
                <option value="">-- Select course --</option>
                {courses.map(course => (
                <option key={course.id} value={course.id}>
                    {course.name}
                </option>
                ))}
            </select>
            </div>

            <div className="student-field">
                <label htmlFor="prefCourse1">Preferred Course 1</label>

            <select
            id="prefCourse1"
            className="student-input"
            value={form.prefCourse1Id ?? ""}
            onChange={e =>
                handleChange("prefCourse1Id", Number(e.target.value))
            }
            >
            <option value="">-- Select course --</option>
            {courses.map(course => (
                <option key={course.id} value={course.id}>
                {course.name}
                </option>
            ))}
            </select>
            </div>

        <div className="student-field">
            <label htmlFor="prefCourse2">Preferred Course 2</label>
            <select
                id="prefCourse2"
                className="student-input"
                value={form.prefCourse2Id ?? ""}
                onChange={e =>
                    handleChange("prefCourse2Id", Number(e.target.value))
                }
            >
                <option value="">-- Select course --</option>
                {courses.map(course => (
                    <option key={course.id} value={course.id}>
                        {course.name}
                    </option>
                ))}
            </select>
        </div>

        <div className="student-field">
            <label htmlFor="prefCourse3">Preferred Course 3</label>
            <select
                id="prefCourse3"
                className="student-input"
                value={form.prefCourse3Id ?? ""}
                onChange={e =>
                    handleChange("prefCourse3Id", Number(e.target.value))
                }
            >
                <option value="">-- Select course --</option>
                {courses.map(course => (
                    <option key={course.id} value={course.id}>
                        {course.name}
                    </option>
                ))}
            </select>
        </div>


            <div className="student-checkbox-row">
                <input
                    id="registred"
                    type="checkbox"
                    checked={form.registred}
                    onChange={e => handleChange("registred", e.target.checked)}
                />
                <label htmlFor="registred">Registered</label>
            </div>

            <div className="student-checkbox-row">
                <input
                    id="oriented"
                    type="checkbox"
                    checked={form.oriented}
                    onChange={e => handleChange("oriented", e.target.checked)}
                />
                <label htmlFor="oriented">Oriented</label>
            </div>

            <h4 className="student-address-title">Address</h4>
            {["street", "city", "houseNbr", "postalCode", "country"].map((key) => (
                <div className="student-field full-width" key={key}>
                    <label htmlFor={`address-${key}`}>{key.charAt(0).toUpperCase() + key.slice(1)}</label>
                    <input
                        id={`address-${key}`}
                        className="student-input"
                        value={(form.address ?? emptyAddress)[key as keyof Address]}
                        onChange={e => handleAddressChange(key as keyof Address, e.target.value, "address")}
                    />
                </div>
            ))}

            <h4 className="student-address-title">Semester Address</h4>
            {["street", "city", "houseNbr", "postalCode", "country"].map((key) => (
                <div className="student-field full-width" key={key}>
                    <label htmlFor={`semester-${key}`}>{key.charAt(0).toUpperCase() + key.slice(1)}</label>
                    <input
                        id={`semester-${key}`}
                        className="student-input"
                        value={(form.addressSemester ?? emptyAddress)[key as keyof Address]}
                        onChange={e => handleAddressChange(key as keyof Address, e.target.value, "addressSemester")}
                    />
                </div>
            ))}

            <div className="student-form-actions">
                <button className="btn btn-ghost" onClick={onClose}>
                    Abbrechen
                </button>
                <button className="btn-primary-filled" onClick={handleSubmit}>
                    {student ? "Aktualisieren" : "Hinzufügen"}
                </button>
            </div>
        </div>
    );
};

export default StudentForm;
