import React, { useEffect, useState } from "react";
import type { CompletedInternship } from "../../types/interfaces";

interface CompletedInternshipsTableProps {
    studentId: number;
}

const CompletedInternshipsTable: React.FC<CompletedInternshipsTableProps> = ({
    studentId,
}) => {
    const TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiRURJVCJ9LHsiYXV0aG9yaXR5IjoiTUFOQUdFX1VTRVJTIn0seyJhdXRob3JpdHkiOiJST0xFX0FETUlOIn0seyJhdXRob3JpdHkiOiJWSUVXIn1dLCJzdWIiOiJhZG1pbkBzY2hvb2wuY29tIiwiaWF0IjoxNzY0MzU5MTM2LCJleHAiOjE3NjQ0NDU1MzZ9.0IAfemkdN9vGFCsLq02O0kntSZUbvr6M8nKt2daR8-Y";

    const [internships, setInternships] = useState<CompletedInternship[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchInternships = async () => {
            try {
                const response = await fetch(`/api/completed-internships/student/${studentId}`, {
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${TOKEN}`,
                    },
                });
                if (!response.ok) throw new Error("Failed to fetch internships");
                const data: CompletedInternship[] = await response.json();
                setInternships(data);
            } catch (err) {
                console.error(err);
                setError("Error fetching internships");
            } finally {
                setLoading(false);
            }
        };

        fetchInternships();
    }, [studentId]);

    if (loading) return <div>Loading internships...</div>;
    if (error) return <div>{error}</div>;
    if (internships.length === 0) return <div>No completed internships.</div>;

    return (
        <table style={{ width: "100%", borderCollapse: "collapse", marginTop: "10px" }}>
            <thead>
                <tr>
                    <th style={{ border: "1px solid gray", padding: "4px" }}>Type</th>
                    <th style={{ border: "1px solid gray", padding: "4px" }}>Course</th>
                    <th style={{ border: "1px solid gray", padding: "4px" }}>Start Date</th>
                    <th style={{ border: "1px solid gray", padding: "4px" }}>End Date</th>
                    <th style={{ border: "1px solid gray", padding: "4px" }}>Description</th>
                </tr>
            </thead>
            <tbody>
                {internships.map((internship) => (
                    <tr key={internship.id}>
                        <td style={{ border: "1px solid gray", padding: "4px" }}>{internship.type || "PDP_I"}</td>
                        <td style={{ border: "1px solid gray", padding: "4px" }}>{internship.course}</td>
                        <td style={{ border: "1px solid gray", padding: "4px" }}>{internship.startDate}</td>
                        <td style={{ border: "1px solid gray", padding: "4px" }}>{internship.endDate}</td>
                        <td style={{ border: "1px solid gray", padding: "4px" }}>{internship.description || "N/A"}</td>
                    </tr>
                ))}
            </tbody>
        </table>
    );

};

export default CompletedInternshipsTable;
