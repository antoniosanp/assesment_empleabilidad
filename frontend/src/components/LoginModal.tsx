import React, { useState } from 'react';
import { useTranslation } from '../i18n/useTranslation';
import { authService } from '../services/authService';
import { User } from '../types/user';
import { KeyRound, Mail, UserCheck } from 'lucide-react';

interface LoginModalProps {
  onLoginSuccess: (user: User) => void;
}

export const LoginModal: React.FC<LoginModalProps> = ({ onLoginSuccess }) => {
  const { t } = useTranslation();
  const [email, setEmail] = useState('admin@riwi.io');
  const [password, setPassword] = useState('123456');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await authService.login(email, password);
      onLoginSuccess(response.user);
    } catch (err: any) {
      setError(err.message || 'Error al iniciar sesión');
    } finally {
      setLoading(false);
    }
  };

  const selectSeedUser = (seedEmail: string) => {
    setEmail(seedEmail);
    setPassword('123456');
  };

  return (
    <div style={styles.overlay}>
      <div style={styles.card}>
        <div style={styles.header}>
          <img src="/riwi_logo.png" alt="Riwi Logo" style={styles.logo} />
          <h2 style={styles.title}>{t('login')}</h2>
        </div>

        {error && <div style={styles.errorBox}>{error}</div>}

        <form onSubmit={handleLogin} style={styles.form}>
          <div style={styles.inputGroup}>
            <label style={styles.label}>{t('email')}</label>
            <div style={styles.inputWrapper}>
              <Mail size={18} color="#A7A2B0" />
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
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
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                style={styles.input}
              />
            </div>
          </div>

          <button type="submit" style={styles.submitBtn} disabled={loading}>
            {loading ? '...' : t('login')}
          </button>
        </form>

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
    padding: '32px',
    boxShadow: '0 20px 40px rgba(0,0,0,0.2)',
  },
  header: {
    textAlign: 'center',
    marginBottom: '24px',
  },
  logo: {
    height: '44px',
    marginBottom: '12px',
  },
  title: {
    fontSize: '22px',
    color: 'var(--color-header-bg)',
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
    gap: '16px',
  },
  inputGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '6px',
  },
  label: {
    fontSize: '13px',
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
    padding: '12px 0',
    backgroundColor: 'transparent',
  },
  submitBtn: {
    backgroundColor: 'var(--color-primary)',
    color: '#FFFFFF',
    padding: '12px',
    borderRadius: '8px',
    fontSize: '15px',
    fontWeight: 600,
    marginTop: '8px',
  },
  seedSection: {
    marginTop: '24px',
    borderTop: '1px solid var(--color-border-light)',
    paddingTop: '16px',
  },
  seedTitle: {
    fontSize: '12px',
    color: 'var(--color-text-muted)',
    marginBottom: '10px',
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
  },
};
