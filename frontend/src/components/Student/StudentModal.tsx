import React from "react";

interface Props {
    children: React.ReactNode;
    onClose: () => void;
}

const StudentModal: React.FC<Props> = ({ children, onClose }) => {
    return (
        <div
            style={{
                position: "fixed",
                top: 0,
                left: 0,
                width: "100vw",
                height: "100vh",
                background: "rgba(0,0,0,0.5)",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                zIndex: 1000,
            }}
            onClick={onClose}
        >
            <div
                style={{
                    background: "white",
                    padding: "20px",
                    borderRadius: "8px",
                    minWidth: "400px",
                }}
                onClick={(e) => e.stopPropagation()} // prevent closing on form click
            >
                {children}
            </div>
        </div>
    );
};

export default StudentModal;
