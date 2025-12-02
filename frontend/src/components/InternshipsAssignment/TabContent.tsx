import { useState } from "react";
import StudentConfigTable from "./StudentConfigTable";
import InternshipAssignmentModal from "./InternshipAssignmentModal";
import InternshipAssignmentStudentForm from "./InternshipAssignmentStudentForm";
import "../../styles/InternshipsAssignment/TabContent.css";
import type { StudentConfigDto } from "../../services/studentConfigService";

interface TabContentProps {
    tabName: string;
}

export default function TabContent({ tabName }: TabContentProps) {
    const [studentOpen, setStudentOpen] = useState(false);
    const [teacherOpen, setTeacherOpen] = useState(false);

    const [studentUpdateMode, setStudentUpdateMode] = useState(false);
    const [teacherUpdateMode, setTeacherUpdateMode] = useState(false);

    const [showStudentModal, setShowStudentModal] = useState(false);
    const [selectedConfig, setSelectedConfig] = useState<StudentConfigDto | undefined>(undefined);

    return (
        <div className="tab-container">
            <h2>Configurations for {tabName}</h2>

            {/* Student Config Accordion */}
            <div className="accordion">
                <div className="accordion-header" onClick={() => setStudentOpen(!studentOpen)}>
                    <span>Student Config {studentOpen ? "▲" : "▼"}</span>
                    <div className="header-buttons">
                        <button
                            className="header-button"
                            onClick={(e) => {
                                e.stopPropagation();
                                setStudentUpdateMode(!studentUpdateMode);
                            }}
                        >
                            {studentUpdateMode ? "Exit Edit config" : "Edit mode"}
                        </button>

                        <button
                            className="header-button"
                            onClick={(e) => {
                                e.stopPropagation();
                                setSelectedConfig(undefined);
                                setShowStudentModal(true);
                            }}
                        >
                            Add
                        </button>
                    </div>
                </div>

                {studentOpen && (
                    <div className="accordion-content">
                        <StudentConfigTable
                            tabName={tabName}
                            editMode={studentUpdateMode}
                            onEdit={(cfg: StudentConfigDto) => {
                                setSelectedConfig(cfg);
                                setShowStudentModal(true);
                            }}
                        />
                    </div>
                )}
            </div>

            {/* Teacher Config Accordion */}
            <div className="accordion">
                <div className="accordion-header" onClick={() => setTeacherOpen(!teacherOpen)}>
                    <span>Teacher Config {teacherOpen ? "▲" : "▼"}</span>
                    <div className="header-buttons">
                        <button
                            className="header-button"
                            onClick={(e) => {
                                e.stopPropagation();
                                setTeacherUpdateMode(!teacherUpdateMode);
                            }}
                        >
                            {teacherUpdateMode ? "Exit Edit config" : "Edit mode"}
                        </button>

                        <button className="header-button" onClick={(e) => e.stopPropagation()}>
                            Add
                        </button>
                    </div>
                </div>

                {teacherOpen && (
                    <div className="accordion-content">
                        <p>Settings related to teachers go here.</p>
                        <p>Update mode is {teacherUpdateMode ? "ON" : "OFF"}</p>
                    </div>
                )}
            </div>

            {/* Student Modal */}
            {showStudentModal && (
                <InternshipAssignmentModal
                    title={selectedConfig ? "Edit Student" : "Add Student"}
                    onClose={() => setShowStudentModal(false)}
                >
                    <InternshipAssignmentStudentForm
                        config={selectedConfig}
                        year={tabName}
                        onClose={() => setShowStudentModal(false)}
                        onSave={() => setShowStudentModal(false)}
                    />
                </InternshipAssignmentModal>
            )}
        </div>
    );
}
