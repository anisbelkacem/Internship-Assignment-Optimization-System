import { useState, useEffect } from 'react';
import userService, { Permission } from '../services/userService';
import type { User, UserCreate } from '../services/userService';

import '../styles/Users.css';

export default function Settings() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');
  const [showModal, setShowModal] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  
  const [formData, setFormData] = useState<UserCreate>({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    roles: [],
    permissions: [],
  });

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await userService.getAllUsers();
      setUsers(data);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Fehler beim Laden der Benutzer';
      console.error('Error fetching users:', error);
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (user?: User) => {
    if (user) {
      setEditingUser(user);
      setFormData({
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        password: '',
        roles: user.roles,
        permissions: user.permissions,
      });
    } else {
      setEditingUser(null);
      setFormData({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        roles: [],
        permissions: [],
      });
    }
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingUser(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (formData.permissions.length === 0) {
      setError('Bitte wählen Sie mindestens eine Berechtigung aus');
      return;
    }

    try {
      if (editingUser) {
        await userService.updateUser(editingUser.id, formData);
      } else {
        await userService.createUser(formData);
      }
      handleCloseModal();
      fetchUsers();
      setError('');
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Fehler beim Speichern des Benutzers';
      setError(errorMessage);    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Möchten Sie diesen Benutzer wirklich löschen?')) {
      return;
    }
    try {
      await userService.deleteUser(id);
      fetchUsers();
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Fehler beim Löschen des Benutzers';
      setError(errorMessage);    }
  };

  const handlePermissionChange = (permission: Permission) => {
    setFormData(prev => ({
      ...prev,
      permissions: prev.permissions.includes(permission)
        ? prev.permissions.filter(p => p !== permission)
        : [...prev.permissions, permission]
    }));
  };

  const getPermissionLabel = (permission: Permission): string => {
    const labels: Record<Permission, string> = {
      [Permission.VIEW]: 'Ansehen',
      [Permission.EDIT]: 'Bearbeiten',
      [Permission.MANAGE_USERS]: 'Benutzer verwalten',
    };
    return labels[permission] || permission;
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p style={{ color: '#666' }}>Benutzer werden geladen...</p>
      </div>
    );
  }

  return (
    <section className="section-container settings-section">
      {error && (
        <div className="inline-alert error">
          <strong>Fehler:</strong> {error}
          <button onClick={fetchUsers} className="btn btn-ghost btn-sm" style={{ marginLeft: 8 }}>
            Erneut versuchen
          </button>
        </div>
      )}

      <div className="section-header">
        <div>
          <h2>Benutzerverwaltung</h2>
          <p className="settings-subtitle">Verwalten Sie alle Benutzer im System.</p>
        </div>
        <div className="section-header-actions">
          <button className="btn btn-primary" onClick={() => handleOpenModal()}>
            Nutzer anlegen
          </button>
        </div>
      </div>

      <div className="table-card">
        <div className="table-card-header">
          <div className="table-card-title">
            <h3>Benutzerliste</h3>
            <span className="table-card-subtitle">Alle Konten mit Berechtigungen</span>
          </div>
          <span className="table-count">{users.length} Nutzer</span>
        </div>

        {users.length === 0 ? (
          <p className="table-empty">Keine Benutzer gefunden. Legen Sie einen neuen Nutzer an, um zu beginnen.</p>
        ) : (
          <div className="table-container">
            <table className="settings-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>E-Mail</th>
                  <th>Berechtigungen</th>
                  <th>Aktionen</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td className="cell-strong">{user.firstName} {user.lastName}</td>
                    <td className="cell-muted">{user.email}</td>
                    <td>
                      <div className="pill-list">
                        {user.permissions.map((perm) => (
                          <span key={perm} className="pill">
                            {getPermissionLabel(perm)}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td>
                      <div className="table-actions">
                        <button className="action-btn edit-btn" onClick={() => handleOpenModal(user)} title="Bearbeiten">
                          ✏️
                        </button>
                        <button className="action-btn delete-btn" onClick={() => handleDelete(user.id)} title="Löschen">
                          🗑️
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="settings-summary">
        <span className="summary-text">
          Gesamt: <strong>{users.length}</strong> Benutzer
        </span>
        <div className="summary-stats">
          {Object.values(Permission).map((permission) => {
            const count = users.filter((u) => u.permissions.includes(permission)).length;
            return count > 0 ? (
              <div key={permission} className="stat-item">
                {getPermissionLabel(permission)}: {count}
              </div>
            ) : null;
          })}
        </div>
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editingUser ? 'Benutzer bearbeiten' : 'Neuer Benutzer'}</h2>
            </div>

            <div className="modal-body settings-form">
              <div className="form-group">
                <label className="form-label required">Vorname</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.firstName}
                  onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label required">Nachname</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.lastName}
                  onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label required">E-Mail</label>
                <input
                  type="email"
                  className="form-input"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label required">
                  Passwort {editingUser ? '(leer lassen, um beizubehalten)' : ''}
                </label>
                <input
                  type="password"
                  className="form-input"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  required={!editingUser}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Berechtigungen auswählen *</label>
                <div className="checkbox-group">
                  {Object.values(Permission).map((permission) => (
                    <div key={permission} className="checkbox-item">
                      <input
                        type="checkbox"
                        id={`permission-${permission}`}
                        checked={formData.permissions.includes(permission)}
                        onChange={() => handlePermissionChange(permission)}
                      />
                      <label htmlFor={`permission-${permission}`}>
                        {getPermissionLabel(permission)}
                      </label>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div className="modal-footer">
              <button className="btn btn-ghost" onClick={handleCloseModal}>
                Abbrechen
              </button>
              <button className="btn btn-primary" onClick={handleSubmit}>
                {editingUser ? 'Aktualisieren' : 'Erstellen'}
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}