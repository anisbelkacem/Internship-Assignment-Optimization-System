import React, { useState, useEffect } from 'react';
import auditLogService from '../../services/auditLogService';
import type { AuditLogEntry, AuditLogPage } from '../../services/auditLogService';
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

    const renderValueChange = (prev: any, next: any) => {
        if (prev === undefined || prev === null) {
            return <span className="value-new">+ {JSON.stringify(next)}</span>;
        }
        if (next === undefined || next === null) {
            return <span className="value-removed">- {JSON.stringify(prev)}</span>;
        }
        return (
            <span>
                <span className="value-old">{JSON.stringify(prev)}</span> → <span className="value-new">{JSON.stringify(next)}</span>
            </span>
        );
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
                                    {log.createdBy.firstName} {log.createdBy.lastName}
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
                            {expandedId === log.id && (log.previousValues || log.newValues) && (
                                <tr className="audit-details-row">
                                    <td colSpan={5}>
                                        <div className="audit-details">
                                            {log.previousValues && log.newValues && (
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
                                                            {Object.keys(log.newValues || {}).map((key) => (
                                                                <tr key={key}>
                                                                    <td className="field-name">{key}</td>
                                                                    <td className="field-value">
                                                                        {log.previousValues?.[key] !== undefined
                                                                            ? JSON.stringify(log.previousValues[key])
                                                                            : '—'}
                                                                    </td>
                                                                    <td className="field-value">
                                                                        {log.newValues?.[key] !== undefined
                                                                            ? JSON.stringify(log.newValues[key])
                                                                            : '—'}
                                                                    </td>
                                                                </tr>
                                                            ))}
                                                        </tbody>
                                                    </table>
                                                </div>
                                            )}
                                            {log.previousValues && !log.newValues && (
                                                <div className="deleted-data">
                                                    <h4>Deleted Data:</h4>
                                                    <pre>{JSON.stringify(log.previousValues, null, 2)}</pre>
                                                </div>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            )}
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
