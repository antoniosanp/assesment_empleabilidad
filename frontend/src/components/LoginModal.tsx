import React, { useState } from 'react';
import { useTranslation } from '../i18n/useTranslation';
import { authService } from '../services/authService';
import { User } from '../types/user';
import { KeyRound, Mail, UserCheck, User as UserIcon, Briefcase } from 'lucide-react';

interface LoginModalProps {
  onLoginSuccess: (user: User) => void;
}

export const LoginModal: React.FC<LoginModalProps> = ({ onLoginSuccess }) => {
  const { t } = useTranslation();
  const [mode, setMode] = useState<'login' | 'register'>('login');

  // Login state
  const [email, setEmail] = useState('admin@riwi.io');
  const [password, setPassword] = useState('123456');

  // Register state
  const [regFullName, setRegFullName] = useState('');
  const [regJobTitle, setRegJobTitle] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      if (mode === 'login') {
        const response = await authService.login(email, password);
        onLoginSuccess(response.user);
      } else {
        if (!regFullName.trim() || !regJobTitle.trim() || !regEmail.trim() || !regPassword.trim()) {
          setError(t('all_fields_required'));
          setLoading(false);
          return;
        }
        const response = await authService.register(regEmail, regPassword, regFullName, regJobTitle);
        onLoginSuccess(response.user);
      }
    } catch (err: any) {
      setError(err.message || (mode === 'login' ? t('login_error') : t('register_error')));
    } finally {
      setLoading(false);
    }
  };

  const selectSeedUser = (seedEmail: string) => {
    setMode('login');
    setEmail(seedEmail);
    setPassword('123456');
  };

  return (
    <div style={styles.overlay}>
      <div style={styles.card}>
        <div style={styles.header}>
          <img src="/riwi_logo.png" alt="Riwi Logo" style={styles.logo} />
          
          {/* Mode Switch Tabs */}
          <div style={styles.tabContainer}>
            <button
              style={{
                ...styles.tabBtn,
                borderBottom: mode === 'login' ? '2px solid var(--color-primary)' : '2px solid transparent',
                color: mode === 'login' ? 'var(--color-primary)' : 'var(--color-text-muted)',
                fontWeight: mode === 'login' ? 600 : 400,
              }}
              onClick={() => {
                setMode('login');
                setError('');
              }}
            >
              {t('login')}
            </button>
            <button
              style={{
                ...styles.tabBtn,
                borderBottom: mode === 'register' ? '2px solid var(--color-primary)' : '2px solid transparent',
                color: mode === 'register' ? 'var(--color-primary)' : 'var(--color-text-muted)',
                fontWeight: mode === 'register' ? 600 : 400,
              }}
              onClick={() => {
                setMode('register');
                setError('');
              }}
            >
              {t('register')}
            </button>
          </div>
        </div>

        {error && <div style={styles.errorBox}>{error}</div>}

        <form onSubmit={handleSubmit} style={styles.form}>
          {mode === 'register' && (
            <>
              <div style={styles.inputGroup}>
                <label style={styles.label}>{t('full_name')}</label>
                <div style={styles.inputWrapper}>
                  <UserIcon size={18} color="#A7A2B0" />
                  <input
                    type="text"
                    placeholder="Ej. Carlos Mendoza"
                    value={regFullName}
                    onChange={(e) => setRegFullName(e.target.value)}
                    required
                    style={styles.input}
                  />
                </div>
              </div>

              <div style={styles.inputGroup}>
                <label style={styles.label}>{t('job_title')}</label>
                <div style={styles.inputWrapper}>
                  <Briefcase size={18} color="#A7A2B0" />
                  <input
                    type="text"
                    placeholder="Ej. FullStack Developer"
                    value={regJobTitle}
                    onChange={(e) => setRegJobTitle(e.target.value)}
                    required
                    style={styles.input}
                  />
                </div>
              </div>
            </>
          )}

          <div style={styles.inputGroup}>
            <label style={styles.label}>{t('email')}</label>
            <div style={styles.inputWrapper}>
              <Mail size={18} color="#A7A2B0" />
              <input
                type="email"
                placeholder="ejemplo@riwi.io"
                value={mode === 'login' ? email : regEmail}
                onChange={(e) => (mode === 'login' ? setEmail(e.target.value) : setRegEmail(e.target.value))}
                required
                style={styles.input}
              />
            </div>
          </div>

          <div style={styles.inputGroup}>
            <label style={styles.label}>{t('password')}</label>
            <div style={styles.inputWrapper}>
              <KeyRound size={18} color="#A7A2B0" />
              <input
                type="password"
                placeholder="••••••••"
                value={mode === 'login' ? password : regPassword}
                onChange={(e) => (mode === 'login' ? setPassword(e.target.value) : setRegPassword(e.target.value))}
                required
                style={styles.input}
              />
            </div>
          </div>

          <button type="submit" style={styles.submitBtn} disabled={loading}>
            {loading ? '...' : mode === 'login' ? t('login') : t('register')}
          </button>
        </form>

        {mode === 'login' && (
          <div style={styles.seedSection}>
            <p style={styles.seedTitle}>{t('select_user')}</p>
            <div style={styles.seedGrid}>
              <button
                style={styles.seedBtn}
                onClick={() => selectSeedUser('admin@riwi.io')}
              >
                <UserCheck size={14} /> Admin Sistema
              </button>
              <button
                style={styles.seedBtn}
                onClick={() => selectSeedUser('maria.gomez@riwi.io')}
              >
                <UserCheck size={14} /> Maria Gomez (Backend)
              </button>
              <button
                style={styles.seedBtn}
                onClick={() => selectSeedUser('juan.perez@riwi.io')}
              >
                <UserCheck size={14} /> Juan Perez (Frontend)
              </button>
              <button
                style={styles.seedBtn}
                onClick={() => selectSeedUser('pedro.soporte@riwi.io')}
              >
                <UserCheck size={14} /> Pedro Soporte (IT)
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

const styles: Record<string, React.CSSProperties> = {
  overlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(22, 25, 44, 0.85)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 1000,
    backdropFilter: 'blur(4px)',
  },
  card: {
    width: '420px',
    backgroundColor: '#FFFFFF',
    borderRadius: '12px',
    padding: '28px 32px',
    boxShadow: '0 20px 40px rgba(0,0,0,0.2)',
  },
  header: {
    textAlign: 'center',
    marginBottom: '20px',
  },
  logo: {
    height: '40px',
    marginBottom: '12px',
  },
  tabContainer: {
    display: 'flex',
    justifyContent: 'center',
    gap: '24px',
    borderBottom: '1px solid var(--color-border-light)',
    paddingBottom: '8px',
  },
  tabBtn: {
    background: 'none',
    border: 'none',
    fontSize: '15px',
    padding: '6px 12px',
    cursor: 'pointer',
  },
  errorBox: {
    backgroundColor: '#FEE2E2',
    color: '#DC2626',
    padding: '10px 14px',
    borderRadius: '6px',
    fontSize: '13px',
    marginBottom: '16px',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '14px',
  },
  inputGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '4px',
  },
  label: {
    fontSize: '12px',
    fontWeight: 600,
    color: 'var(--color-text-main)',
  },
  inputWrapper: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    border: '1px solid var(--color-border)',
    borderRadius: '8px',
    padding: '0 12px',
    backgroundColor: 'var(--color-surface)',
  },
  input: {
    border: 'none',
    width: '100%',
    padding: '10px 0',
    backgroundColor: 'transparent',
    fontSize: '14px',
  },
  submitBtn: {
    backgroundColor: 'var(--color-primary)',
    color: '#FFFFFF',
    padding: '12px',
    borderRadius: '8px',
    fontSize: '15px',
    fontWeight: 600,
    marginTop: '6px',
    cursor: 'pointer',
  },
  seedSection: {
    marginTop: '20px',
    borderTop: '1px solid var(--color-border-light)',
    paddingTop: '14px',
  },
  seedTitle: {
    fontSize: '12px',
    color: 'var(--color-text-muted)',
    marginBottom: '8px',
    textAlign: 'center',
  },
  seedGrid: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gap: '8px',
  },
  seedBtn: {
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    backgroundColor: 'var(--color-surface-alt)',
    color: 'var(--color-header-bg)',
    padding: '8px 10px',
    borderRadius: '6px',
    fontSize: '11px',
    textAlign: 'left',
    border: '1px solid var(--color-border-light)',
    cursor: 'pointer',
  },
};
