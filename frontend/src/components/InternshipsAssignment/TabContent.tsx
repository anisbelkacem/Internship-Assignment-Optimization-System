import { useState } from "react";
import StudentConfigTable from "./StudentConfigTable";
import InternshipAssignmentModal from "./InternshipAssignmentModal";
import InternshipAssignmentStudentForm from "./InternshipAssignmentStudentForm";

export default function TabContent({ tabName }) {
    const [studentOpen, setStudentOpen] = useState(false);
    const [teacherOpen, setTeacherOpen] = useState(false);

    // Track update mode for each accordion
    const [studentUpdateMode, setStudentUpdateMode] = useState(false);
    const [teacherUpdateMode, setTeacherUpdateMode] = useState(false);

    // Modal state
    const [showStudentModal, setShowStudentModal] = useState(false);
    const [selectedConfig, setSelectedConfig] = useState(null);

    const containerStyle = {
        display: "flex",
        flexDirection: "column",
        gap: "12px",
        padding: "16px",
        borderRadius: "0 4px 4px 4px",
        backgroundColor: "rgb(30, 45, 100)",
        color: "white",
    };

    const accordionStyle = {
        border: "1px solid #517c9e",
        borderRadius: "4px",
        overflow: "hidden",
    };

    const headerStyle = {
        padding: "12px 16px",
        cursor: "pointer",
        fontWeight: 600,
        backgroundColor: "rgb(80, 156, 219)",
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
    };

    const contentStyle = {
        padding: "12px 16px",
        backgroundColor: "rgb(25, 40, 95)",
    };

    const buttonStyle = {
        marginLeft: "8px",
        padding: "4px 8px",
        backgroundColor: "rgb(21, 34, 89)",
        color: "white",
        border: "1px solid #517c9e",
        borderRadius: "4px",
        cursor: "pointer",
        fontSize: "0.85rem",
    };

    return (
        <div style={containerStyle}>
            <h2>Configurations for {tabName}</h2>

            {/* Student Config Accordion */}
            <div style={accordionStyle}>
                <div style={headerStyle} onClick={() => setStudentOpen(!studentOpen)}>
                    <span>Student Config {studentOpen ? "▲" : "▼"}</span>
                    <div>
                        <button
                            style={buttonStyle}
                            onClick={(e) => {
                                e.stopPropagation();
                                setStudentUpdateMode(!studentUpdateMode);
                            }}
                        >
                            {studentUpdateMode ? "Exit Edit config" : "Edit mode"}
                        </button>
                        <button
                            style={buttonStyle}
                            onClick={(e) => {
                                e.stopPropagation();
                                setSelectedConfig(null);
                                setShowStudentModal(true);
                            }}
                        >
                            Add
                        </button>
                    </div>
                </div>
                {studentOpen && (
                    <div style={contentStyle}>
                        <StudentConfigTable
                            tabName={tabName}
                            editMode={studentUpdateMode}
                            onEdit={(cfg) => {
                                setSelectedConfig(cfg);
                                setShowStudentModal(true);
                            }}
                        />
                    </div>
                )}
            </div>

            {/* Teacher Config Accordion */}
            <div style={accordionStyle}>
                <div style={headerStyle} onClick={() => setTeacherOpen(!teacherOpen)}>
                    <span>Teacher Config {teacherOpen ? "▲" : "▼"}</span>
                    <div>
                        <button
                            style={buttonStyle}
                            onClick={(e) => {
                                e.stopPropagation();
                                setTeacherUpdateMode(!teacherUpdateMode);
                            }}
                        >
                            {teacherUpdateMode ? "Exit Edit config" : "Edit mode"}
                        </button>
                        <button
                            style={buttonStyle}
                            onClick={(e) => e.stopPropagation()}
                        >
                            Add
                        </button>
                    </div>
                </div>
                {teacherOpen && (
                    <div style={contentStyle}>
                        <p>Settings related to teachers go here.</p>
                        <p>Update mode is {teacherUpdateMode ? "ON" : "OFF"}</p>
                        {/* Pass teacherUpdateMode to child components */}
                    </div>
                )}
            </div>

            {/* Student Modal */}
            {showStudentModal && (
                <InternshipAssignmentModal onClose={() => setShowStudentModal(false)}>
                    <InternshipAssignmentStudentForm
                        config={selectedConfig}
                        year={tabName}
                        onClose={() => setShowStudentModal(false)}
                        onSave={() => {
                            setShowStudentModal(false);
                        }}
                    />
                </InternshipAssignmentModal>
            )}
        </div>
    );
}
