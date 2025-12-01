import React from "react";
import "../../styles/InternshipsAssignment/InternshipAssignmentModal.css";

export default function InternshipAssignmentModal({ title, children, onClose }) {
    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                {title && <h3 className="modal-title">{title}</h3>}
                {children}
            </div>
        </div>
    );
}
