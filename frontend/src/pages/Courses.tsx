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
    active: true,
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
      active: course.active,
    });
  } else {
    setEditingCourse(null);
    setFormData({
      name: '',
      active: true, // default new course = active
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

const handleDeactivate = async (id: number) => {
  if (!window.confirm('Möchten Sie diesen Kurs wirklich deaktivieren?')) {
    return;
  }

  try {
    await courseService.deactivateCourse(id);
    fetchCourses();
  } catch (error) {
    const errorMessage =
      error instanceof Error ? error.message : 'Fehler beim Deaktivieren des Kurses';
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
    <div className="section-container settings-section">
      <div style={{ marginBottom: '14px' }}>
        <h2 style={{ margin: 0, fontSize: '22px' }}>Kurse</h2>
        <span className="table-card-subtitle">Übersicht der Kurse</span>
      </div>

      {error && (
        <div className="error-container">
          <strong>Fehler:</strong> {error}
          <button onClick={fetchCourses} className="btn-secondary" style={{ marginLeft: '1rem' }}>
            Erneut versuchen
          </button>
        </div>
      )}

      <div className="table-card">
        <div className="table-card-header">
          <div className="table-card-title" aria-hidden="true"></div>
          <div className="table-card-actions">
            <button className="btn-primary" onClick={() => handleOpenModal()}>
              Neuer Kurs
            </button>
          </div>
        </div>

        {courses.length === 0 ? (
          <div className="table-empty">Keine Kurse gefunden. Fügen Sie einen neuen Kurs hinzu, um zu beginnen.</div>
        ) : (
          <div className="table-container">
            <table className="settings-table">
              <thead>
                <tr>
                  <th>Kursname</th>
                  <th>Status</th>
                  <th style={{ textAlign: 'right' }}>Aktionen</th>
                </tr>
              </thead>
              <tbody>
                {courses.map((course) => (
                  <tr key={course.id}>
                    <td>
                      <div className="cell-strong">{course.name}</div>
                    </td>

                    <td>
                      <span className={`pill ${course.active ? 'pill-success' : 'pill-danger'}`}>
                        {course.active ? 'Aktiv' : 'Inaktiv'}
                      </span>
                    </td>

                    <td>
                      <div className="table-actions" style={{ justifyContent: 'flex-end' }}>
                        <button
                          className="action-btn edit-btn"
                          onClick={() => handleOpenModal(course)}
                          title="Bearbeiten"
                          type="button"
                        >
                          ✏️
                        </button>

                        {course.active && (
                          <button
                            className="action-btn delete-btn"
                            onClick={() => handleDeactivate(course.id)}
                            title="Deaktivieren"
                            type="button"
                          >
                            ❌
                          </button>
                        )}
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
          Gesamt: <strong>{courses.length}</strong> Kurse | Aktiv: <strong>{courses.filter(c => c.active).length}</strong>
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
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                  placeholder="z.B. Mathematik, Deutsch, Englisch"
                />
              </div>
            </div>

            {/* Aktiv / Inaktiv */}
            <div className="form-group">
                <label className="form-checkbox">
                <input
                    type="checkbox"
                    checked={formData.active}
                    onChange={(e) =>
                    setFormData({ ...formData, active: e.target.checked })
                    }
                />
                <span style={{ marginLeft: '0.5rem' }}>
                    Kurs ist aktiv
                </span>
                </label>
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