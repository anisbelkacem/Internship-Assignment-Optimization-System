import { useState, useEffect } from 'react';
import schoolService, { SchoolType } from '../services/schoolService';
import type { School, SchoolCreate } from '../services/schoolService';
import '../styles/Schools.css';


type SchoolFormData = SchoolCreate;

interface PreviewSchool extends SchoolCreate {
  rowNumber: number;
  isValid: boolean;
  errors: string[];
}

export default function Schools() {
  const [schools, setSchools] = useState<School[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [showImportModal, setShowImportModal] = useState(false);
  const [showPreviewModal, setShowPreviewModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [editingSchool, setEditingSchool] = useState<School | null>(null);
  const [deletingSchoolId, setDeletingSchoolId] = useState<number | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [importResult, setImportResult] = useState<string | null>(null);
  const [previewSchools, setPreviewSchools] = useState<PreviewSchool[]>([]);
  const [formData, setFormData] = useState<SchoolFormData>({
    name: '',
    address: '',
    zone: '',
    oepnv: false,
    type: SchoolType.GS,
  });

  useEffect(() => {
    fetchSchools();
  }, []);

  const fetchSchools = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await schoolService.getAllSchools();
      setSchools(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenModal = (school?: School) => {
    if (school) {
      setEditingSchool(school);
      setFormData({
        name: school.name,
        address: school.address,
        zone: school.zone,
        oepnv: school.oepnv,
        type: school.type,
      });
    } else {
      setEditingSchool(null);
      setFormData({
        name: '',
        address: '',
        zone: '',
        oepnv: false,
        type: SchoolType.GS,
      });
    }
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingSchool(null);
    setFormData({
      name: '',
      address: '',
      zone: '',
      oepnv: false,
      type: SchoolType.GS,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    try {
      if (editingSchool) {
        await schoolService.updateSchool(editingSchool.id, formData);
        setSuccess('School updated successfully!');
      } else {
        await schoolService.createSchool(formData);
        setSuccess('School created successfully!');
      }

      handleCloseModal();
      fetchSchools();
      
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    }
  };

  const handleDelete = async (id: number) => {
    setDeletingSchoolId(id);
    setShowDeleteModal(true);
  };

  const handleConfirmDelete = async () => {
    if (!deletingSchoolId) return;

    try {
      await schoolService.deleteSchool(deletingSchoolId);
      setSuccess('School deleted successfully!');
      fetchSchools();
      
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setShowDeleteModal(false);
      setDeletingSchoolId(null);
    }
  };

  const handleCancelDelete = () => {
    setShowDeleteModal(false);
    setDeletingSchoolId(null);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    
    if (type === 'checkbox') {
      const checked = (e.target as HTMLInputElement).checked;
      setFormData(prev => ({ ...prev, [name]: checked }));
    } else {
      setFormData(prev => ({ ...prev, [name]: value }));
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      setError(null);
    }
  };

  const parseExcelFile = async (file: File): Promise<PreviewSchool[]> => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      
      reader.onload = async (e) => {
        try {
          const data = e.target?.result;
          const XLSX = await import('xlsx');
          const workbook = XLSX.read(data, { type: 'binary' });
          const sheetName = workbook.SheetNames[0];
          const worksheet = workbook.Sheets[sheetName];
          
          // Use defval to include empty cells
          const jsonData = XLSX.utils.sheet_to_json(worksheet, { 
            raw: false,
            defval: '' // This ensures empty cells are included as empty strings
          });
          
          console.log('Parsed Excel data:', jsonData);
          
          const schools: PreviewSchool[] = jsonData.map((row: any, index: number) => {
            const errors: string[] = [];
            
            // Try multiple possible column names
            const name = String(row['Name'] || row['name'] || row['NAME'] || '').trim();
            const address = String(row['Adresse'] || row['address'] || row['ADDRESS'] || '').trim();
            const zone = String(row['Zone'] || row['zone'] || row['ZONE'] || '').trim();
            const oepnvRaw = String(row['ÖPNV'] || row['oepnv'] || row['OEPNV'] || row['Oepnv'] || '').toLowerCase().trim();
            const typeRaw = String(row['Typ'] || row['type'] || row['TYPE'] || row['Type'] || '').toUpperCase().trim();
            
            console.log(`Row ${index + 1}:`, { name, address, zone, oepnvRaw, typeRaw });
            
            // Validate required fields
            if (!name) errors.push('Name fehlt');
            if (!address) errors.push('Adresse fehlt');
            if (!zone) errors.push('Zone fehlt');
            
            // Parse ÖPNV
            let oepnv = false;
            if (['true', 'yes', 'ja', '1'].includes(oepnvRaw)) {
              oepnv = true;
            } else if (['false', 'no', 'nein', '0', ''].includes(oepnvRaw)) {
              oepnv = false;
            } else {
              errors.push('ÖPNV ungültig');
            }
            
            // Parse Type
            let type: SchoolType | '' = '';
            if (typeRaw === 'GS') {
              type = SchoolType.GS;
            } else if (typeRaw === 'MS') {
              type = SchoolType.MS;
            } else if (typeRaw === '') {
              errors.push('Type fehlt');
              type = ''; // Keep it empty to show it's missing
            } else {
              errors.push('Type ungültig');
              type = ''; // Keep it empty to show it's invalid
            }
            
            const school: PreviewSchool = {
              rowNumber: index + 1,
              name: name,
              address: address,
              zone: zone,
              oepnv,
              type: type as SchoolType,
              isValid: errors.length === 0,
              errors
            };
            
            console.log(`Row ${index + 1} result:`, school);
            
            return school;
          });
          
          console.log('All parsed schools:', schools);
          console.log('Valid:', schools.filter(s => s.isValid).length);
          console.log('Invalid:', schools.filter(s => !s.isValid).length);
          
          resolve(schools);
        } catch (error) {
          console.error('Parse error:', error);
          reject(error);
        }
      };
      
      reader.onerror = () => reject(new Error('Fehler beim Lesen der Datei'));
      reader.readAsBinaryString(file);
    });
  };

  const handleImportSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedFile) {
      setError('Please select a file');
      return;
    }

    setError(null);
    setLoading(true);

    try {
      const parsed = await parseExcelFile(selectedFile);
      
      // Check for duplicates against existing schools
      const existingSchoolNames = schools.map(s => s.name.toLowerCase().trim());
      const parsedWithDuplicateCheck = parsed.map(school => {
        const isDuplicate = existingSchoolNames.includes(school.name.toLowerCase().trim());
        if (isDuplicate && !school.errors.includes('Schule existiert bereits')) {
          return {
            ...school,
            isValid: false,
            errors: [...school.errors, 'Schule existiert bereits']
          };
        }
        return school;
      });
      
      // Check for duplicates within the import file itself
      const nameCount = new Map<string, number>();
      parsedWithDuplicateCheck.forEach(school => {
        const normalizedName = school.name.toLowerCase().trim();
        nameCount.set(normalizedName, (nameCount.get(normalizedName) || 0) + 1);
      });
      
      const finalParsed = parsedWithDuplicateCheck.map(school => {
        const normalizedName = school.name.toLowerCase().trim();
        if (normalizedName && nameCount.get(normalizedName)! > 1 && !school.errors.includes('Duplikat in der Datei')) {
          return {
            ...school,
            isValid: false,
            errors: [...school.errors, 'Duplikat in der Datei']
          };
        }
        return school;
      });
      
      setPreviewSchools(finalParsed);
      setShowImportModal(false);
      setShowPreviewModal(true);
    } catch (err) {
      console.error('Parse error:', err);
      setError('Fehler beim Parsen der Excel-Datei. Stellen Sie sicher, dass es eine gültige .xlsx-Datei ist.');
    } finally {
      setLoading(false);
    }
  };

  const handlePreviewInputChange = (index: number, field: keyof SchoolCreate, value: any) => {
    setPreviewSchools(prev => {
      const updated = [...prev];
      updated[index] = { ...updated[index], [field]: value };
      
      const errors: string[] = [];
      if (!updated[index].name.trim()) errors.push('Name fehlt');
      if (!updated[index].address.trim()) errors.push('Adresse fehlt');
      if (!updated[index].zone.trim()) errors.push('Zone fehlt');
      if (!updated[index].type) errors.push('Type fehlt');
      
      // Check for duplicates with existing schools
      const existingSchoolNames = schools.map(s => s.name.toLowerCase().trim());
      if (existingSchoolNames.includes(updated[index].name.toLowerCase().trim())) {
        errors.push('Schule existiert bereits');
      }
      
      // Check for duplicates within the import file
      const duplicatesInFile = updated.filter((s, i) => 
        i !== index && 
        s.name.toLowerCase().trim() === updated[index].name.toLowerCase().trim() &&
        s.name.trim() !== ''
      );
      if (duplicatesInFile.length > 0) {
        errors.push('Duplikat in der Datei');
      }
      
      updated[index].isValid = errors.length === 0;
      updated[index].errors = errors;
      
      return updated;
    });
  };

  const handleConfirmImport = async () => {
    const validSchools = previewSchools.filter(s => s.isValid);
    
    if (validSchools.length === 0) {
      setError('Keine gültigen Schulen zum Importieren');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      let successCount = 0;
      let failCount = 0;
      const errors: string[] = [];

      for (const school of validSchools) {
        try {
          await schoolService.createSchool({
            name: school.name,
            address: school.address,
            zone: school.zone,
            oepnv: school.oepnv,
            type: school.type
          });
          successCount++;
        } catch (err) {
          failCount++;
          errors.push(`Zeile ${school.rowNumber}: ${err instanceof Error ? err.message : 'Fehler'}`);
        }
      }

      setSuccess(`${successCount} Schulen erfolgreich importiert!`);
      if (failCount > 0) {
        setError(`${failCount} Schulen konnten nicht importiert werden. ${errors.join(', ')}`);
      }
      
      fetchSchools();
      setShowPreviewModal(false);
      setShowImportModal(false);
      setPreviewSchools([]);
      setSelectedFile(null);
      
      setTimeout(() => {
        setSuccess(null);
        setError(null);
      }, 5000);
    } catch (err) {
      setError('Import fehlgeschlagen');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelPreview = () => {
    setShowPreviewModal(false);
    setPreviewSchools([]);
    setShowImportModal(true);
  };

  const handleCloseImportModal = () => {
    setShowImportModal(false);
    setSelectedFile(null);
    setImportResult(null);
  };

  if (loading) {
    return (
      <div className="schools-container">
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Loading schools...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="schools-container">
      <div className="schools-header">
        <div className="schools-header-content">
          <h1>Schulen</h1>
          <p>Übersicht der Praktikumsschulen und Zonen</p>
        </div>
        <div className="header-actions">
          <button className="btn-import" onClick={() => setShowImportModal(true)}>
             Excel importieren
          </button>
          <button className="btn-primary" onClick={() => handleOpenModal()}>
            <span>+</span> Schule hinzufügen
          </button>
        </div>
      </div>

      {error && (
        <div className="error-container">
          <strong>Error:</strong> {error}
        </div>
      )}

      {success && (
        <div className="success-container">
          <strong>Success:</strong> {success}
        </div>
      )}

      {schools.length === 0 ? (
        <div className="empty-state">
          <h3>Keine Schulen gefunden</h3>
          <p>Fügen Sie Ihre erste Schule hinzu, um loszulegen</p>
        </div>
      ) : (
        <>
          <div className="schools-table-container">
            <table className="schools-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Adresse</th>
                  <th>Zone</th>
                  <th>ÖPNV</th>
                  <th>Type</th>
                  <th>Aktionen</th>
                </tr>
              </thead>
              <tbody>
                {schools.map((school) => (
                  <tr key={school.id}>
                    <td className="school-name">{school.name}</td>
                    <td>{school.address}</td>
                    <td>{school.zone}</td>
                    <td>
                      <span className={school.oepnv ? 'badge badge-oepnv' : 'badge badge-no-oepnv'}>
                        {school.oepnv ? 'Ja' : 'Nein'}
                      </span>
                    </td>
                    <td>
                      <span className={school.type === SchoolType.GS ? 'badge badge-gs' : 'badge badge-ms'}>
                        {school.type === SchoolType.GS ? 'Grundschule' : 'Mittelschule'}
                      </span>
                    </td>
                    <td>
                      <div className="action-buttons">
                        <button 
                          className="btn-secondary" 
                          onClick={() => handleOpenModal(school)}
                        >
                          Bearbeiten
                        </button>
                        <button 
                          className="btn-danger" 
                          onClick={() => handleDelete(school.id)}
                        >
                          Löschen
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="schools-summary">
            <div className="summary-text">
              Gesamt: {schools.length} Schule{schools.length !== 1 ? 'n' : ''}
            </div>
            <div className="summary-stats">
              <div className="stat-item">
                Grundschule: {schools.filter(s => s.type === SchoolType.GS).length}
              </div>
              <div className="stat-item">
                Mittelschule: {schools.filter(s => s.type === SchoolType.MS).length}
              </div>
              <div className="stat-item">
                ÖPNV: {schools.filter(s => s.oepnv).length}
              </div>
            </div>
          </div>
        </>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editingSchool ? 'Schule bearbeiten' : 'Neue Schule hinzufügen'}</h2>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label required" htmlFor="name">
                    Name
                  </label>
                  <input
                    type="text"
                    id="name"
                    name="name"
                    className="form-input"
                    value={formData.name}
                    onChange={handleInputChange}
                    required
                    placeholder="z.B. Grundschule am Stadtpark"
                  />
                </div>

                <div className="form-group">
                  <label className="form-label required" htmlFor="address">
                    Adresse
                  </label>
                  <input
                    type="text"
                    id="address"
                    name="address"
                    className="form-input"
                    value={formData.address}
                    onChange={handleInputChange}
                    required
                    placeholder="z.B. Musterstraße 123, 12345 Musterstadt"
                  />
                </div>

                <div className="form-group">
                  <label className="form-label required" htmlFor="zone">
                    Zone
                  </label>
                  <input
                    type="text"
                    id="zone"
                    name="zone"
                    className="form-input"
                    value={formData.zone}
                    onChange={handleInputChange}
                    required
                    placeholder="z.B. Nord, Süd, Ost, West"
                  />
                </div>

                <div className="form-group">
                  <label className="form-label required" htmlFor="type">
                    Schultyp
                  </label>
                  <select
                    id="type"
                    name="type"
                    className="form-select"
                    value={formData.type}
                    onChange={handleInputChange}
                    required
                  >
                    <option value={SchoolType.GS}>GS - Grundschule (Primary School)</option>
                    <option value={SchoolType.MS}>MS - Mittelschule (Secondary School)</option>
                  </select>
                </div>

                <div className="form-group">
                  <div className="form-checkbox-group">
                    <input
                      type="checkbox"
                      id="oepnv"
                      name="oepnv"
                      className="form-checkbox"
                      checked={formData.oepnv}
                      onChange={handleInputChange}
                    />
                    <label className="form-label" htmlFor="oepnv" style={{ marginBottom: 0 }}>
                      ÖPNV-Anbindung vorhanden
                    </label>
                  </div>
                </div>
              </div>

              <div className="modal-footer">
                <button type="button" className="btn-cancel" onClick={handleCloseModal}>
                  Abbrechen
                </button>
                <button type="submit" className="btn-primary">
                  {editingSchool ? 'Aktualisieren' : 'Hinzufügen'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showImportModal && (
        <div className="modal-overlay" onClick={handleCloseImportModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Schulen aus Excel importieren</h2>
            </div>
            <form onSubmit={handleImportSubmit}>
              <div className="modal-body">
                <div className="form-group">
                  <label className="form-label required" htmlFor="file">
                    Excel-Datei (.xlsx)
                  </label>
                  <input
                    type="file"
                    id="file"
                    name="file"
                    className="form-input"
                    accept=".xlsx"
                    onChange={handleFileChange}
                    required
                  />
                  {selectedFile && (
                    <p style={{ fontSize: '0.875rem', color: '#059669', marginTop: '0.5rem', fontWeight: 500 }}>
                      Ausgewählte Datei: {selectedFile.name}
                    </p>
                  )}
                  <p style={{ fontSize: '0.75rem', color: '#6b7280', marginTop: '0.5rem' }}>
                    Die Excel-Datei sollte folgende Spalten enthalten: Name, Adresse, Zone, ÖPNV (true/false), Type (GS/MS)
                  </p>
                </div>

                {importResult && (
                  <div className="success-container" style={{ whiteSpace: 'pre-line' }}>
                    {importResult}
                  </div>
                )}
              </div>

              <div className="modal-footer">
                <button type="button" className="btn-cancel" onClick={handleCloseImportModal}>
                  Abbrechen
                </button>
                <button type="submit" className="btn-primary" disabled={!selectedFile}>
                  Importieren
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showPreviewModal && (
        <div className="modal-overlay" onClick={(e) => e.stopPropagation()}>
          <div className="modal-content preview-modal" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '1200px' }}>
            <div className="modal-header">
              <h2>Import-Vorschau - Daten überprüfen</h2>
              <p style={{ fontSize: '0.875rem', color: '#6b7280', marginTop: '0.5rem' }}>
                Überprüfen Sie die Daten und bearbeiten Sie ungültige Einträge. Gültige Einträge sind grün markiert, ungültige rot.
              </p>
            </div>
            <div className="modal-body" style={{ maxHeight: '60vh', overflowY: 'auto' }}>
              <div className="preview-table-container">
                <table className="schools-table">
                  <thead>
                    <tr>
                      <th>Zeile</th>
                      <th>Status</th>
                      <th>Name</th>
                      <th>Adresse</th>
                      <th>Zone</th>
                      <th>ÖPNV</th>
                      <th>Type</th>
                    </tr>
                  </thead>
                  <tbody>
                    {previewSchools.map((school, index) => (
                      <tr key={index} className={school.isValid ? 'preview-row-valid' : 'preview-row-invalid'}>
                        <td>{school.rowNumber}</td>
                        <td>
                          {school.isValid ? (
                            <span className="badge badge-oepnv">✓ Gültig</span>
                          ) : (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                              {school.errors.map((error, errorIndex) => (
                                <span key={errorIndex} className="badge badge-no-oepnv" style={{ whiteSpace: 'nowrap' }}>
                                  ✗ {error}
                                </span>
                              ))}
                            </div>
                          )}
                        </td>
                        <td>
                          <input
                            type="text"
                            className="form-input-small"
                            value={school.name}
                            onChange={(e) => handlePreviewInputChange(index, 'name', e.target.value)}
                            placeholder="Name erforderlich"
                          />
                        </td>
                        <td>
                          <input
                            type="text"
                            className="form-input-small"
                            value={school.address}
                            onChange={(e) => handlePreviewInputChange(index, 'address', e.target.value)}
                            placeholder="Adresse erforderlich"
                          />
                        </td>
                        <td>
                          <input
                            type="text"
                            className="form-input-small"
                            value={school.zone}
                            onChange={(e) => handlePreviewInputChange(index, 'zone', e.target.value)}
                            placeholder="Zone erforderlich"
                          />
                        </td>
                        <td>
                          <input
                            type="checkbox"
                            checked={school.oepnv}
                            onChange={(e) => handlePreviewInputChange(index, 'oepnv', e.target.checked)}
                            style={{ width: '1.25rem', height: '1.25rem', cursor: 'pointer' }}
                          />
                        </td>
                        <td>
                          <select
                            className="form-select-small"
                            value={school.type || ''}
                            onChange={(e) => handlePreviewInputChange(index, 'type', e.target.value as SchoolType)}
                            style={{ backgroundColor: '#374151', color: 'white', border: '1px solid #4b5563' }}
                          >
                            <option value="">-- Bitte wählen --</option>
                            <option value={SchoolType.GS}>GS</option>
                            <option value={SchoolType.MS}>MS</option>
                          </select>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div className="preview-summary" style={{ marginTop: '1rem', padding: '1rem', backgroundColor: '#f9fafb', borderRadius: '0.5rem' }}>
                <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
                  <div className="stat-item">
                    Gesamt: {previewSchools.length}
                  </div>
                  <div className="stat-item" style={{ backgroundColor: '#d1fae5', borderColor: '#a7f3d0' }}>
                    Gültig: {previewSchools.filter(s => s.isValid).length}
                  </div>
                  <div className="stat-item" style={{ backgroundColor: '#fee2e2', borderColor: '#fecaca' }}>
                    Ungültig: {previewSchools.filter(s => !s.isValid).length}
                  </div>
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn-cancel" onClick={handleCancelPreview}>
                Zurück
              </button>
              <button 
                type="button" 
                className="btn-primary" 
                onClick={handleConfirmImport}
                disabled={previewSchools.filter(s => s.isValid).length === 0}
              >
                Gültige Einträge importieren ({previewSchools.filter(s => s.isValid).length})
              </button>
            </div>
          </div>
        </div>
      )}

      {showDeleteModal && (
        <div className="modal-overlay" onClick={handleCancelDelete}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '500px' }}>
            <div className="modal-header">
              <h2>Schule löschen</h2>
            </div>
            <div className="modal-body">
              <p style={{ fontSize: '1rem', color: '#374151', lineHeight: '1.5' }}>
                Sind Sie sicher, dass Sie diese Schule löschen möchten? Diese Aktion kann nicht rückgängig gemacht werden.
              </p>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn-cancel" onClick={handleCancelDelete}>
                Abbrechen
              </button>
              <button type="button" className="btn-danger" onClick={handleConfirmDelete}>
                Löschen
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
