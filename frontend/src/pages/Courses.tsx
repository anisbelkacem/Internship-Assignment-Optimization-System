import { useState, useEffect } from 'react';
import courseService from '../services/courseService';
import type { Course, CourseCreate } from '../services/courseService';

import '../styles/Users.css';

export default function Courses() {
  const [courses, setCourses] = useState<Course[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string>('');
  const [showModal, setShowModal] = useState(false);
  const [editingCourse, setEditingCourse] = useState<Course | null>(null);
  
  const [formData, setFormData] = useState<CourseCreate>({
    name: '',
  });

  useEffect(() => {
    fetchCourses();
  }, []);

  const fetchCourses = async () => {
    try {
      setLoading(true);
      setError('');
      const data = await courseService.getAllCourses();
      setCourses(data);
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Fehler beim Laden der Kurse';
      console.error('Error fetching courses:', error);
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (course?: Course) => {
    if (course) {
      setEditingCourse(course);
      setFormData({
        name: course.name,
      });
    } else {
      setEditingCourse(null);
      setFormData({
        name: '',
      });
    }
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingCourse(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.name.trim()) {
      setError('Bitte geben Sie einen Kursnamen ein');
      return;
    }

    try {
      if (editingCourse) {
        await courseService.updateCourse(editingCourse.id, formData);
      } else {
        await courseService.createCourse(formData);
      }
      handleCloseModal();
      fetchCourses();
      setError('');
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Fehler beim Speichern des Kurses';
      setError(errorMessage);
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Möchten Sie diesen Kurs wirklich löschen?')) {
      return;
    }
    try {
      await courseService.deleteCourse(id);
      fetchCourses();
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Fehler beim Löschen des Kurses';
      setError(errorMessage);
    }
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p style={{ color: '#666' }}>Kurse werden geladen...</p>
      </div>
    );
  }

  return (
    <div className="users-container">
      {error && (
        <div className="error-container">
          <strong>Fehler:</strong> {error}
          <button onClick={fetchCourses} className="btn-secondary" style={{ marginLeft: '1rem' }}>
            Erneut versuchen
          </button>
        </div>
      )}

      <div className="users-header">
        <div className="users-header-content">
          <h1>Kursverwaltung</h1>
          <p>Verwalten Sie alle Kurse im System</p>
        </div>
        <button className="btn-primary" onClick={() => handleOpenModal()}>
          + Neuer Kurs
        </button>
      </div>

      {courses.length === 0 ? (
        <div className="empty-state">
          <p style={{ color: '#6b7280', fontSize: '1.125rem', marginBottom: '0.5rem' }}>
            Keine Kurse gefunden
          </p>
          <p style={{ color: '#9ca3af', fontSize: '0.875rem' }}>
            Fügen Sie einen neuen Kurs hinzu, um zu beginnen
          </p>
        </div>
      ) : (
        <div className="users-table-container">
          <table className="users-table">
            <thead>
              <tr>
                <th>Kursname</th>
                <th style={{ textAlign: 'right' }}>Aktionen</th>
              </tr>
            </thead>
            <tbody>
              {courses.map((course) => (
                <tr key={course.id}>
                  <td>
                    <div className="user-name">{course.name}</div>
                  </td>
                  <td>
                    <div className="action-buttons" style={{ justifyContent: 'flex-end' }}>
                      <button className="btn-secondary" onClick={() => handleOpenModal(course)}>
                        Bearbeiten
                      </button>
                      <button className="btn-danger" onClick={() => handleDelete(course.id)}>
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
          Gesamt: <strong>{courses.length}</strong> Kurse
        </span>
      </div>

      {/* Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editingCourse ? 'Kurs bearbeiten' : 'Neuer Kurs'}</h2>
            </div>
            
            <div className="modal-body">
              <div className="form-group">
                <label className="form-label">Kursname *</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.name}
                  onChange={(e) => setFormData({ name: e.target.value })}
                  required
                  placeholder="z.B. Mathematik, Deutsch, Englisch"
                />
              </div>
            </div>

            <div className="modal-footer">
              <button className="btn-cancel" onClick={handleCloseModal}>
                Abbrechen
              </button>
              <button className="btn-primary" onClick={handleSubmit}>
                {editingCourse ? 'Aktualisieren' : 'Erstellen'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}