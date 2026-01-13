import React, { useState, useEffect } from 'react';
import auditLogService from '../../services/auditLogService';
import type { AuditLogEntry } from '../../services/auditLogService';
import './AuditHistoryTable.css';

interface AuditHistoryTableProps {
    entityType: string;
    entityId: number;
}

const AuditHistoryTable: React.FC<AuditHistoryTableProps> = ({ entityType, entityId }) => {
    const [auditLogs, setAuditLogs] = useState<AuditLogEntry[]>([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [expandedId, setExpandedId] = useState<number | null>(null);

    useEffect(() => {
        fetchAuditHistory();
    }, [entityType, entityId, page]);

    const fetchAuditHistory = async () => {
        setLoading(true);
        try {
            const data = await auditLogService.getEntityHistory(entityType, entityId, page, 10);
            setAuditLogs(data.content);
            setTotalPages(data.totalPages);
        } catch (error) {
            console.error('Error fetching audit history:', error);
        }
        setLoading(false);
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleString();
    };

    const getActionBadgeClass = (action: string) => {
        switch (action) {
            case 'CREATE':
                return 'badge-create';
            case 'UPDATE':
                return 'badge-update';
            case 'DELETE':
                return 'badge-delete';
            default:
                return '';
        }
    };



    if (loading && auditLogs.length === 0) {
        return <div className="audit-loading">Loading audit history...</div>;
    }

    if (auditLogs.length === 0) {
        return <div className="audit-empty">No audit history available</div>;
    }

    return (
        <div className="audit-history-table">
            <h3>Audit History (Read-Only)</h3>
            <table>
                <thead>
                    <tr>
                        <th>Timestamp</th>
                        <th>Action</th>
                        <th>Modified By</th>
                        <th>Description</th>
                        <th>Details</th>
                    </tr>
                </thead>
                <tbody>
                    {auditLogs.map((log) => (
                        <React.Fragment key={log.id}>
                            <tr>
                                <td className="audit-timestamp">{formatDate(log.timestamp)}</td>
                                <td>
                                    <span className={`badge ${getActionBadgeClass(log.action)}`}>
                                        {log.action}
                                    </span>
                                </td>
                                <td className="audit-user">
                                    {log.createdBy}
                                </td>
                                <td className="audit-description">{log.description}</td>
                                <td className="audit-actions">
                                    {(log.previousValues || log.newValues) && (
                                        <button
                                            className="btn-expand"
                                            onClick={() =>
                                                setExpandedId(expandedId === log.id ? null : log.id)
                                            }
                                        >
                                            {expandedId === log.id ? '▼' : '▶'}
                                        </button>
                                    )}
                                </td>
                            </tr>
                            {expandedId === log.id && (log.previousValues || log.newValues) && (() => {
                                const prevValues = log.previousValues ? JSON.parse(log.previousValues) : {};
                                const newValues = log.newValues ? JSON.parse(log.newValues) : {};
                                const allKeys = new Set([...Object.keys(prevValues), ...Object.keys(newValues)]);
                                return (
                                    <tr className="audit-details-row">
                                        <td colSpan={5}>
                                            <div className="audit-details">
                                                {(log.previousValues || log.newValues) && (
                                                    <div className="changes-table">
                                                        <h4>Changes:</h4>
                                                        <table className="inner-table">
                                                            <thead>
                                                                <tr>
                                                                    <th>Field</th>
                                                                    <th>Previous Value</th>
                                                                    <th>New Value</th>
                                                                </tr>
                                                            </thead>
                                                            <tbody>
                                                                {Array.from(allKeys).map((key) => (
                                                                    <tr key={key}>
                                                                        <td className="field-name">{key}</td>
                                                                        <td className="field-value">
                                                                            {prevValues[key] !== undefined
                                                                                ? JSON.stringify(prevValues[key])
                                                                                : '—'}
                                                                        </td>
                                                                        <td className="field-value">
                                                                            {newValues[key] !== undefined
                                                                                ? JSON.stringify(newValues[key])
                                                                                : '—'}
                                                                        </td>
                                                                    </tr>
                                                                ))}
                                                            </tbody>
                                                        </table>
                                                </div>
                                            )}
                                            {log.newValues && !log.previousValues && (() => {
                                                const newVals = JSON.parse(log.newValues);
                                                return (
                                                    <div className="created-data">
                                                        <h4>Created Data:</h4>
                                                        <pre>{JSON.stringify(newVals, null, 2)}</pre>
                                                    </div>
                                                );
                                            })()}
                                            {log.previousValues && !log.newValues && (() => {
                                                const prevVals = JSON.parse(log.previousValues);
                                                return (
                                                    <div className="deleted-data">
                                                        <h4>Deleted Data:</h4>
                                                        <pre>{JSON.stringify(prevVals, null, 2)}</pre>
                                                    </div>
                                                );
                                            })()}
                                        </div>
                                    </td>
                                </tr>
                                );
                            })()}
                        </React.Fragment>
                    ))}
                </tbody>
            </table>

            {totalPages > 1 && (
                <div className="pagination">
                    <button onClick={() => setPage(Math.max(0, page - 1))} disabled={page === 0}>
                        Previous
                    </button>
                    <span>
                        Page {page + 1} of {totalPages}
                    </span>
                    <button onClick={() => setPage(Math.min(totalPages - 1, page + 1))} disabled={page >= totalPages - 1}>
                        Next
                    </button>
                </div>
            )}
        </div>
    );
};

export default AuditHistoryTable;
