import React from "react";
import "../../styles/StudentStyles/StudentModal.css";
interface Props {
    children: React.ReactNode;
    onClose: () => void;
}

const StudentModal: React.FC<Props> = ({ children, onClose }) => {
    return (
        <div className="student-modal-overlay" onClick={onClose}>
            <div
                className="student-modal-content"
                onClick={(e) => e.stopPropagation()}
            >
                {children}
            </div>
        </div>
    );
};

export default StudentModal;
