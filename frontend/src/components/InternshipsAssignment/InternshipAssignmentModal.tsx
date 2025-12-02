import "../../styles/InternshipsAssignment/InternshipAssignmentModal.css";

interface InternshipAssignmentModalProps {
    title?: string;
    children: React.ReactNode;
    onClose: () => void;
}

export default function InternshipAssignmentModal({
    title,
    children,
    onClose,
}: InternshipAssignmentModalProps) {
    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                {title && <h3 className="modal-title">{title}</h3>}
                {children}
            </div>
        </div>
    );
}
