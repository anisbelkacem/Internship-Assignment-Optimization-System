import React, { useState, useEffect } from 'react';
import auditLogService from '../services/auditLogService';
import type { AuditLogEntry } from '../services/auditLogService';
import './AuditLogs.css';

const AuditLogs: React.FC = () => {
  const [logs, setLogs] = useState<AuditLogEntry[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [expandedId, setExpandedId] = useState<number | null>(null);

  useEffect(() => {
    fetchLogs();
  }, [page]);

  const fetchLogs = async () => {
    setLoading(true);
    try {
      const data = await auditLogService.getAll(page, 20);
      setLogs(data.content);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error('Error fetching audit logs:', error);
    }
    setLoading(false);
  };

  const parseChanges = (previousValues: string, newValues: string) => {
    try {
      const prev = previousValues ? JSON.parse(previousValues) : {};
      const next = newValues ? JSON.parse(newValues) : {};
      
      // Build changes object with old/new for each field
      const changes: Record<string, {old: any, new: any}> = {};
      
      // Get all unique keys from both objects
      const allKeys = new Set([...Object.keys(prev), ...Object.keys(next)]);
      
      allKeys.forEach(key => {
        if (prev[key] !== next[key]) {
          changes[key] = { old: prev[key], new: next[key] };
        }
      });
      
      return changes;
    } catch {
      return {};
    }
  };

  return (
    <div className="audit-logs-page">
      <div className="schools-header">
        <div className="schools-header-content">
          <h1>Audit Logs</h1>
          <p className="subtitle">Wer hat was, wann und was geändert</p>
        </div>
      </div>

      {loading && logs.length === 0 ? (
        <div className="empty-state">
          <p>Laden...</p>
        </div>
      ) : logs.length === 0 ? (
        <div className="empty-state">
          <p>Keine Audit-Protokolle verfügbar.</p>
        </div>
      ) : (
        <>
          <div className="schools-table-container">
          <table className="audit-table">
            <thead>
              <tr>
                <th>Zeitstempel</th>
                <th>Entität</th>
                <th>Aktion</th>
                <th>Benutzer</th>
                <th>Beschreibung</th>
                <th>Änderungen</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((log) => {
                const changes = parseChanges(log.previousValues, log.newValues);
                const hasChanges = Object.keys(changes).length > 0;

                return (
                  <React.Fragment key={log.id}>
                    <tr>
                      <td>{new Date(log.timestamp).toLocaleString()}</td>
                      <td>
                        <span className="entity-type">{log.entityType}</span>
                        <span className="entity-id">#{log.entityId}</span>
                      </td>
                      <td>
                        <span className={`badge badge-${log.action.toLowerCase()}`}>
                          {log.action}
                        </span>
                      </td>
                      <td>{log.createdBy}</td>
                      <td>{log.description}</td>
                      <td>
                        {hasChanges && (
                          <button
                            className="btn-expand"
                            onClick={() => setExpandedId(expandedId === log.id ? null : log.id)}
                            onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                            onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                          >
                            {expandedId === log.id ? '▼' : '▶'}
                          </button>
                        )}
                      </td>
                    </tr>
                    {expandedId === log.id && hasChanges && (
                      <tr className="details-row">
                        <td colSpan={6}>
                          <div className="details-content">
                            <table className="changes-table">
                              <thead>
                                <tr>
                                  <th>Feld</th>
                                  <th>Vorher</th>
                                  <th>Neu</th>
                                </tr>
                              </thead>
                              <tbody>
                                {Object.entries(changes).map(([field, change]: [string, any]) => (
                                  <tr key={field}>
                                    <td className="field-name">{field}</td>
                                    <td className="field-value">
                                      {change.old !== undefined ? JSON.stringify(change.old) : '—'}
                                    </td>
                                    <td className="field-value">
                                      {change.new !== undefined ? JSON.stringify(change.new) : '—'}
                                    </td>
                                  </tr>
                                ))}
                              </tbody>
                            </table>
                          </div>
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                );
              })}
            </tbody>
          </table>
          </div>

          {totalPages > 1 && (
            <div className="pagination">
              <button
                className="btn btn-secondary"
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={page === 0}
                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
              >
                Zurück
              </button>
              <span>Seite {page + 1} von {totalPages}</span>
              <button
                className="btn btn-secondary"
                onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                disabled={page >= totalPages - 1}
                onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
              >
                Weiter
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default AuditLogs;
