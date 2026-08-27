import React from 'react';
import { User } from '../types/user';
import { useTranslation } from '../i18n/useTranslation';
import { X, ShieldCheck, Mail, Briefcase, Calendar, CheckCircle2 } from 'lucide-react';

interface UserProfileModalProps {
  user: User | null;
  onClose: () => void;
}

export const UserProfileModal: React.FC<UserProfileModalProps> = ({ user, onClose }) => {
  const { t } = useTranslation();

  if (!user) return null;

  return (
    <div style={styles.overlay}>
      <div style={styles.card}>
        <div style={styles.header}>
          <h3 style={styles.title}>{t('profile')}</h3>
          <button style={styles.closeBtn} onClick={onClose}>
            <X size={20} color="#676B73" />
          </button>
        </div>

        <div style={styles.avatarSection}>
          <div style={styles.avatarLarge}>
            {user.fullName.substring(0, 2).toUpperCase()}
          </div>
          <h4 style={styles.fullName}>{user.fullName}</h4>
          <span style={styles.jobBadge}>{user.jobTitle || 'Team Member'}</span>
        </div>

        <div style={styles.detailsList}>
          <div style={styles.detailItem}>
            <Mail size={16} color="var(--color-primary)" />
            <div style={styles.detailText}>
              <span style={styles.label}>{t('email')}</span>
              <span style={styles.value}>{user.email}</span>
            </div>
          </div>

          <div style={styles.detailItem}>
            <Briefcase size={16} color="var(--color-primary)" />
            <div style={styles.detailText}>
              <span style={styles.label}>{t('job_title')}</span>
              <span style={styles.value}>{user.jobTitle || 'N/A'}</span>
            </div>
          </div>

          <div style={styles.detailItem}>
            <ShieldCheck size={16} color="var(--color-primary)" />
            <div style={styles.detailText}>
              <span style={styles.label}>{t('role')}</span>
              <span style={styles.value}>{user.role}</span>
            </div>
          </div>

          <div style={styles.detailItem}>
            <CheckCircle2 size={16} color="var(--color-secondary-dark)" />
            <div style={styles.detailText}>
              <span style={styles.label}>Estado de Cuenta</span>
              <span style={styles.value}>
                {user.isActive ? '🟢 Activa (Stateless JWT Session)' : '🔴 Inactiva'}
              </span>
            </div>
          </div>

          <div style={styles.detailItem}>
            <Calendar size={16} color="var(--color-primary)" />
            <div style={styles.detailText}>
              <span style={styles.label}>Fecha de Registro</span>
              <span style={styles.value}>
                {new Date(user.createdAt).toLocaleDateString()}
              </span>
            </div>
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
    backgroundColor: 'rgba(0,0,0,0.5)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 1000,
  },
  card: {
    width: '380px',
    backgroundColor: '#FFFFFF',
    borderRadius: '12px',
    padding: '24px',
    boxShadow: '0 10px 30px rgba(0,0,0,0.2)',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '20px',
  },
  title: {
    fontSize: '18px',
    color: 'var(--color-header-bg)',
  },
  closeBtn: {
    background: 'none',
    border: 'none',
    cursor: 'pointer',
  },
  avatarSection: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    marginBottom: '24px',
  },
  avatarLarge: {
    width: '64px',
    height: '64px',
    borderRadius: '50%',
    backgroundColor: 'var(--color-primary)',
    color: '#FFFFFF',
    fontSize: '24px',
    fontWeight: 'bold',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: '12px',
  },
  fullName: {
    fontSize: '18px',
    color: 'var(--color-header-bg)',
    marginBottom: '4px',
  },
  jobBadge: {
    backgroundColor: 'var(--color-secondary-light)',
    color: 'var(--color-text-surface)',
    fontSize: '12px',
    padding: '4px 10px',
    borderRadius: '12px',
    fontWeight: 500,
  },
  detailsList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '14px',
  },
  detailItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    backgroundColor: 'var(--color-surface)',
    padding: '10px 14px',
    borderRadius: '8px',
  },
  detailText: {
    display: 'flex',
    flexDirection: 'column',
  },
  label: {
    fontSize: '11px',
    color: 'var(--color-text-muted)',
    textTransform: 'uppercase',
  },
  value: {
    fontSize: '13px',
    fontWeight: 600,
    color: 'var(--color-text-main)',
  },
};
