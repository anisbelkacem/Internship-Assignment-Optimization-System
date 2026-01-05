import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import uniLogo from '../assets/Uni.png';
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
      <header className="login-topbar" aria-label="Universität Passau">
        <div className="login-topbar-right">
          <img src={uniLogo} alt="Universität Passau" className="login-topbar-logo" />
        </div>
      </header>

      <div className="login-wrapper">
        <div className="login-card">
          <h2 className="login-card-title">Anmelden</h2>

          <div className="login-form">
            <div className="login-input-group">
              <div className="login-input-wrapper">
                <div className="login-input-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                  </svg>
                </div>
                <input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSubmit(e)}
                  className="login-input"
                  placeholder="Email or Phone"
                  disabled={loading}
                />
              </div>
            </div>

            <div className="login-input-group">
              <div className="login-input-wrapper">
                <div className="login-input-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zM9 6c0-1.66 1.34-3 3-3s3 1.34 3 3v2H9V6zm9 14H6V10h12v10zm-6-3c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2z"/>
                  </svg>
                </div>
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSubmit(e)}
                  className="login-input login-input-with-eye"
                  placeholder="Password"
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

      </div>

      <div className="login-footer">
        <p>© 2025 Universität Passau - Aspd - Team 3</p>
      </div>
    </div>
  );
};

export default LoginPage;