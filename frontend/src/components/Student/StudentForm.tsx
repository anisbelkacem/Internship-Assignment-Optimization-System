import React, { useState } from "react";
import type { Student } from "../../types/interfaces";
import type { SchoolType, Course } from "../../types/enums";

interface Props {
    student?: Student | null;
    onClose: () => void;
    onSave: (student: Student) => void;
}

const TOKEN = "YOUR_TOKEN_HERE";

const StudentForm: React.FC<Props> = ({ student, onClose, onSave }) => {
    const [form, setForm] = useState<Student>({
        matriculationNbr: student?.matriculationNbr || 0,
        firstName: student?.firstName || "",
        lastName: student?.lastName || "",
        email: student?.email || "",
        schoolType: student?.schoolType || "GS",
        mainCourse: student?.mainCourse || "MATH",
        prefCourse1: student?.prefCourse1 || "MATH",
        prefCourse2: student?.prefCourse2 || "MATH",
        prefCourse3: student?.prefCourse3 || "MATH",
        registred: student?.registred || false,
        oriented: student?.oriented || false,
        address: student?.address,
        addressSemester: student?.addressSemester,
        phone: student?.phone,
        birthDate: student?.birthDate,
        description: student?.description,
    });

    const handleChange = (key: keyof Student, value: any) => {
        setForm((prev) => ({ ...prev, [key]: value }));
    };

    const handleSubmit = async () => {
        try {
            const url = student ? `/api/students/${student.matriculationNbr}` : "/api/students";
            const method = student ? "PUT" : "POST";
            const response = await fetch(url, {
                method,
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${TOKEN}`,
                },
                body: JSON.stringify(form),
            });
            if (!response.ok) throw new Error("Failed to save student");
            const saved: Student = await response.json();
            onSave(saved);
        } catch (err) {
            console.error(err);
        }
    };

    return (
        <div style={{ border: "1px solid gray", padding: "10px", marginTop: "10px" }}>
            <h3>{student ? "Update Student" : "Create Student"}</h3>
            <input
                placeholder="First Name"
                value={form.firstName}
                onChange={(e) => handleChange("firstName", e.target.value)}
            />
            <input
                placeholder="Last Name"
                value={form.lastName}
                onChange={(e) => handleChange("lastName", e.target.value)}
            />
            <input
                placeholder="Email"
                value={form.email}
                onChange={(e) => handleChange("email", e.target.value)}
            />
            <input
                placeholder="Phone"
                value={form.phone || ""}
                onChange={(e) => handleChange("phone", e.target.value)}
            />
            <input
                placeholder="Description"
                value={form.description || ""}
                onChange={(e) => handleChange("description", e.target.value)}
            />
            <div style={{ marginTop: "10px" }}>
                <button onClick={handleSubmit}>{student ? "Update" : "Create"}</button>
                <button onClick={onClose} style={{ marginLeft: "5px" }}>
                    Cancel
                </button>
            </div>
        </div>
    );
};

export default StudentForm;
