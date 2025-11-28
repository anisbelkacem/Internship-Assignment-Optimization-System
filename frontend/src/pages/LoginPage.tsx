import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import '../styles/LoginPage.css';

const LoginPage: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    if (!email || !password) {
      setError('Bitte geben Sie E-Mail und Passwort ein');
      setLoading(false);
      return;
    }

    try {
      const success = await login(email, password);
      if (success) {
        navigate('/dashboard');
      } else {
        setError('Ungültige Anmeldedaten');
      }
    } catch {
      setError('Ein Fehler ist aufgetreten. Bitte versuchen Sie es erneut.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-wrapper">
        <div className="login-header">
          <div className="login-logo-container">
            <div className="login-logo-inner">
              <div className="login-logo-icon">P</div>
              <div className="login-logo-text">
                <div className="login-logo-title">UNIVERSITÄT</div>
                <div className="login-logo-subtitle">PASSAU</div>
              </div>
            </div>
          </div>
          <h1 className="login-main-title">Praktikumsamt</h1>
          <p className="login-subtitle">Universität Passau</p>
        </div>

        <div className="login-card">
          <h2 className="login-card-title">Anmelden</h2>

          <div className="login-form">
            <div className="login-input-group">
              <label htmlFor="email" className="login-label">
                E-Mail
              </label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleSubmit(e)}
                className="login-input"
                placeholder="admin@school.com"
                disabled={loading}
              />
            </div>

            <div className="login-input-group">
              <label htmlFor="password" className="login-label">
                Passwort
              </label>
              <div className="login-password-container">
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSubmit(e)}
                  className="login-input"
                  placeholder="Passwort eingeben"
                  disabled={loading}
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="login-eye-button"
                  disabled={loading}
                >
                  {showPassword ? '👁️' : '👁️‍🗨️'}
                </button>
              </div>
            </div>

            {error && <div className="login-error">{error}</div>}

            <button 
              onClick={handleSubmit} 
              className="login-button"
              disabled={loading}
            >
              {loading ? 'Anmelden...' : 'Anmelden'}
            </button>
          </div>

          <div className="login-links">
            <a href="#" className="login-link">
              Passwort vergessen?
            </a>
            <p className="login-support-text">
              Probleme beim Anmelden? Kontaktieren Sie den Support
            </p>
          </div>
        </div>

        <div className="login-footer">
          <p>© 2025 Universität Passau - Aspd - Team 3</p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;