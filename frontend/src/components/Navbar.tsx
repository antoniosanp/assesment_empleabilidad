import React from 'react';
import { useTranslation, Language } from '../i18n/useTranslation';
import { User } from '../types/user';
import { LogOut, Globe, User as UserIcon } from 'lucide-react';
import { authService } from '../services/authService';

interface NavbarProps {
  user: User | null;
  onOpenProfile: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({ user, onOpenProfile }) => {
  const { t, lang, setLang } = useTranslation();

  return (
    <header style={styles.header}>
      <div style={styles.leftSection}>
        <img src="/riwi_logo.png" alt="Riwi Logo" style={styles.logo} />
        <h1 style={styles.title}>{t('app_title')}</h1>
      </div>

      <div style={styles.rightSection}>
        {/* Language Switcher */}
        <div style={styles.langSelector}>
          <Globe size={16} color="#A7A2B0" />
          <select
            value={lang}
            onChange={(e) => setLang(e.target.value as Language)}
            style={styles.select}
          >
            <option value="es">Español (ES)</option>
            <option value="en">English (EN)</option>
          </select>
        </div>

        {user && (
          <>
            {/* User Profile Badge */}
            <button style={styles.userBadge} onClick={onOpenProfile}>
              <div style={styles.avatar}>
                {user.fullName.substring(0, 2).toUpperCase()}
              </div>
              <span style={styles.userName}>{user.fullName}</span>
            </button>

            {/* Logout Button */}
            <button
              style={styles.logoutBtn}
              onClick={() => authService.logout()}
              title={t('logout')}
            >
              <LogOut size={18} color="#FFFFFF" />
            </button>
          </>
        )}
      </div>
    </header>
  );
};

const styles: Record<string, React.CSSProperties> = {
  header: {
    height: '60px',
    backgroundColor: 'var(--color-header-bg)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '0 20px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
    zIndex: 10,
  },
  leftSection: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
  },
  logo: {
    height: '32px',
    objectFit: 'contain',
  },
  title: {
    color: '#FFFFFF',
    fontSize: '18px',
    fontWeight: 600,
    letterSpacing: '0.3px',
  },
  rightSection: {
    display: 'flex',
    alignItems: 'center',
    gap: '16px',
  },
  langSelector: {
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    backgroundColor: 'rgba(255,255,255,0.08)',
    padding: '4px 8px',
    borderRadius: '6px',
  },
  select: {
    backgroundColor: 'transparent',
    color: '#FFFFFF',
    border: 'none',
    fontSize: '13px',
    cursor: 'pointer',
  },
  userBadge: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    backgroundColor: 'rgba(255,255,255,0.1)',
    color: '#FFFFFF',
    padding: '4px 12px 4px 6px',
    borderRadius: '20px',
    cursor: 'pointer',
    border: '1px solid rgba(255,255,255,0.15)',
  },
  avatar: {
    width: '28px',
    height: '28px',
    borderRadius: '50%',
    backgroundColor: 'var(--color-primary)',
    color: '#FFFFFF',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '12px',
    fontWeight: 'bold',
  },
  userName: {
    fontSize: '14px',
    fontWeight: 500,
  },
  logoutBtn: {
    backgroundColor: 'rgba(239, 68, 68, 0.2)',
    border: '1px solid rgba(239, 68, 68, 0.4)',
    padding: '6px',
    borderRadius: '6px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    cursor: 'pointer',
  },
};
