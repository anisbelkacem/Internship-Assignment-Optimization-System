import { useEffect, useState } from "react";
import type { CompletedInternship } from "../../services/studentService";
import CompletedInternshipsService from "../../services/completedInternshipsService";

interface CompletedInternshipsTableProps {
    studentId: number;
}

const CompletedInternshipsTable: React.FC<CompletedInternshipsTableProps> = ({
    studentId,
}) => {
    const [internships, setInternships] = useState<CompletedInternship[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchInternships = async () => {
            try {
                const data = await CompletedInternshipsService.getByStudentId(studentId);
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
