import React, { useState } from "react";
import type { Student, Address, SchoolType, Course } from "../../services/studentService";
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
    country: "",
};

const StudentForm: React.FC<Props> = ({ student, onClose, onSave }) => {
    const [form, setForm] = useState<Student>({
        matriculationNbr: student?.matriculationNbr || 0,
        firstName: student?.firstName || "",
        lastName: student?.lastName || "",
        email: student?.email || "",
        schoolType: student?.schoolType || "GS",
        mainCourse: student?.mainCourse || "COMPUTER_SCIENCE",
        prefCourse1: student?.prefCourse1 || "COMPUTER_SCIENCE",
        prefCourse2: student?.prefCourse2 || "COMPUTER_SCIENCE",
        prefCourse3: student?.prefCourse3 || "COMPUTER_SCIENCE",
        registred: student?.registred || false,
        oriented: student?.oriented || false,
        address: student?.address || { ...emptyAddress },
        addressSemester: student?.addressSemester || { ...emptyAddress },
        phone: student?.phone || "",
        birthDate: student?.birthDate || "",
        description: student?.description || "",
    });

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
                <label htmlFor="matriculationNbr">Matriculation Number</label>
                <input
                    id="matriculationNbr"
                    className="student-input"
                    type="number"
                    value={form.matriculationNbr}
                    onChange={e => handleChange("matriculationNbr", Number(e.target.value))}
                />
            </div>

            <div className="student-field">
                <label htmlFor="firstName">First Name</label>
                <input
                    id="firstName"
                    className="student-input"
                    value={form.firstName}
                    onChange={e => handleChange("firstName", e.target.value)}
                />
            </div>

            <div className="student-field">
                <label htmlFor="lastName">Last Name</label>
                <input
                    id="lastName"
                    className="student-input"
                    value={form.lastName}
                    onChange={e => handleChange("lastName", e.target.value)}
                />
            </div>

            <div className="student-field">
                <label htmlFor="email">Email</label>
                <input
                    id="email"
                    className="student-input"
                    value={form.email}
                    onChange={e => handleChange("email", e.target.value)}
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
                <label htmlFor="schoolType">School Type</label>
                <select
                    id="schoolType"
                    className="student-input"
                    value={form.schoolType}
                    onChange={e => handleChange("schoolType", e.target.value as SchoolType)}
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
                    value={form.mainCourse}
                    onChange={e => handleChange("mainCourse", e.target.value as Course)}
                >
                    <option value="COMPUTER_SCIENCE">COMPUTER_SCIENCE</option>
                    <option value="ENGINEERING">ENGINEERING</option>
                    <option value="BUSINESS">BUSINESS</option>
                    <option value="MEDICINE">MEDICINE</option>
                    <option value="LAW">LAW</option>
                    <option value="ARTS">ARTS</option>
                    <option value="SCIENCES">SCIENCES</option>
                    <option value="OTHER">OTHER</option>
                </select>
            </div>

            <div className="student-field">
                <label htmlFor="prefCourse1">Preferred Course 1</label>
                <select
                    id="prefCourse1"
                    className="student-input"
                    value={form.prefCourse1}
                    onChange={e => handleChange("prefCourse1", e.target.value as Course)}
                >
                    <option value="COMPUTER_SCIENCE">COMPUTER_SCIENCE</option>
                    <option value="ENGINEERING">ENGINEERING</option>
                    <option value="BUSINESS">BUSINESS</option>
                    <option value="MEDICINE">MEDICINE</option>
                    <option value="LAW">LAW</option>
                    <option value="ARTS">ARTS</option>
                    <option value="SCIENCES">SCIENCES</option>
                    <option value="OTHER">OTHER</option>
                </select>
            </div>

            <div className="student-field">
                <label htmlFor="prefCourse2">Preferred Course 2</label>
                <select
                    id="prefCourse2"
                    className="student-input"
                    value={form.prefCourse2}
                    onChange={e => handleChange("prefCourse2", e.target.value as Course)}
                >
                    <option value="COMPUTER_SCIENCE">COMPUTER_SCIENCE</option>
                    <option value="ENGINEERING">ENGINEERING</option>
                    <option value="BUSINESS">BUSINESS</option>
                    <option value="MEDICINE">MEDICINE</option>
                    <option value="LAW">LAW</option>
                    <option value="ARTS">ARTS</option>
                    <option value="SCIENCES">SCIENCES</option>
                    <option value="OTHER">OTHER</option>
                </select>
            </div>

            <div className="student-field">
                <label htmlFor="prefCourse3">Preferred Course 3</label>
                <select
                    id="prefCourse3"
                    className="student-input"
                    value={form.prefCourse3}
                    onChange={e => handleChange("prefCourse3", e.target.value as Course)}
                >
                    <option value="COMPUTER_SCIENCE">COMPUTER_SCIENCE</option>
                    <option value="ENGINEERING">ENGINEERING</option>
                    <option value="BUSINESS">BUSINESS</option>
                    <option value="MEDICINE">MEDICINE</option>
                    <option value="LAW">LAW</option>
                    <option value="ARTS">ARTS</option>
                    <option value="SCIENCES">SCIENCES</option>
                    <option value="OTHER">OTHER</option>
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
                <button className="student-btn" onClick={handleSubmit}>
                    {student ? "Update" : "Create"}
                </button>
                <button className="student-btn student-btn-cancel" onClick={onClose}>
                    Cancel
                </button>
            </div>
        </div>
    );
};

export default StudentForm;
