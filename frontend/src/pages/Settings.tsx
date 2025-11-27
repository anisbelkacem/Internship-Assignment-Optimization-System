import { useState, useEffect } from 'react';
import userService, { Permission } from '../services/userService';
import type { User, UserCreate} from '../services/userService';

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
    <div className="users-container">
      {error && (
        <div className="error-container">
          <strong>Fehler:</strong> {error}
          <button onClick={fetchUsers} className="btn-secondary" style={{ marginLeft: '1rem' }}>
            Erneut versuchen
          </button>
        </div>
      )}

      <div className="users-header">
        <div className="users-header-content">
          <h1>Benutzerverwaltung</h1>
          <p>Verwalten Sie alle Benutzer im System</p>
        </div>
        <button className="btn-primary" onClick={() => handleOpenModal()}>
          + Neuer Benutzer
        </button>
      </div>

      {users.length === 0 ? (
        <div className="empty-state">
          <p style={{ color: '#6b7280', fontSize: '1.125rem', marginBottom: '0.5rem' }}>
            Keine Benutzer gefunden
          </p>
          <p style={{ color: '#9ca3af', fontSize: '0.875rem' }}>
            Fügen Sie einen neuen Benutzer hinzu, um zu beginnen
          </p>
        </div>
      ) : (
        <div className="users-table-container">
          <table className="users-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>E-Mail</th>
                <th>Berechtigungen</th>
                <th style={{ textAlign: 'right' }}>Aktionen</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td>
                    <div className="user-name">{user.firstName} {user.lastName}</div>
                  </td>
                  <td>
                    <div className="user-email">{user.email}</div>
                  </td>
                  <td>
                    {user.permissions.map(perm => (
                      <span key={perm} className="badge badge-permission">
                        {getPermissionLabel(perm)}
                      </span>
                    ))}
                  </td>
                  <td>
                    <div className="action-buttons" style={{ justifyContent: 'flex-end' }}>
                      <button className="btn-secondary" onClick={() => handleOpenModal(user)}>
                        Bearbeiten
                      </button>
                      <button className="btn-danger" onClick={() => handleDelete(user.id)}>
                        Löschen
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div className="users-summary">
        <span className="summary-text">
          Gesamt: <strong>{users.length}</strong> Benutzer
        </span>
        <div className="summary-stats">
          {Object.values(Permission).map(permission => {
            const count = users.filter(u => u.permissions.includes(permission)).length;
            return count > 0 ? (
              <div key={permission} className="stat-item">
                {getPermissionLabel(permission)}: {count}
              </div>
            ) : null;
          })}
        </div>
      </div>

      {/* Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editingUser ? 'Benutzer bearbeiten' : 'Neuer Benutzer'}</h2>
            </div>
            
            <div className="modal-body">
              <div className="form-group">
                <label className="form-label">Vorname *</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.firstName}
                  onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Nachname *</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.lastName}
                  onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">E-Mail *</label>
                <input
                  type="email"
                  className="form-input"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">
                  Passwort {editingUser ? '(leer lassen, um beizubehalten)' : '*'}
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
                  {Object.values(Permission).map(permission => (
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
              <button className="btn-cancel" onClick={handleCloseModal}>
                Abbrechen
              </button>
              <button className="btn-primary" onClick={handleSubmit}>
                {editingUser ? 'Aktualisieren' : 'Erstellen'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}