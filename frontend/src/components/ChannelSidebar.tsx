import React, { useState, useEffect } from 'react';
import { UserConversation, ChannelType } from '../types/channel';
import { User } from '../types/user';
import { useTranslation } from '../i18n/useTranslation';
import { Hash, Lock, User as UserIcon, Plus, MessageSquare, Users } from 'lucide-react';
import { channelService } from '../services/channelService';
import { userService } from '../services/userService';

interface ChannelSidebarProps {
  conversations: UserConversation[];
  activeChannelId: string | null;
  onSelectChannel: (channelId: string) => void;
  onChannelCreated: () => void;
}

export const ChannelSidebar: React.FC<ChannelSidebarProps> = ({
  conversations,
  activeChannelId,
  onSelectChannel,
  onChannelCreated,
}) => {
  const { t } = useTranslation();
  const [showModal, setShowModal] = useState(false);
  const [newChannelName, setNewChannelName] = useState('');
  const [newChannelType, setNewChannelType] = useState<ChannelType>('PUBLIC');
  const [selectedTargetUserId, setSelectedTargetUserId] = useState<string>('');
  const [availableUsers, setAvailableUsers] = useState<User[]>([]);
  const [creating, setCreating] = useState(false);

  // Load team users when modal opens
  useEffect(() => {
    if (showModal) {
      userService.getAllUsers()
        .then((users) => setAvailableUsers(users))
        .catch((err) => console.error('Error fetching users for chat:', err));
    }
  }, [showModal]);

  const handleCreateChannel = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreating(true);

    try {
      let channelName = newChannelName.trim();
      let memberIds: string[] = [];

      if (newChannelType === 'DIRECT') {
        const targetUser = availableUsers.find((u) => u.id === selectedTargetUserId);
        if (!targetUser) {
          alert('Por favor selecciona un usuario para el chat directo.');
          setCreating(false);
          return;
        }
        channelName = `Chat con ${targetUser.fullName}`;
        memberIds = [selectedTargetUserId];
      } else if (!channelName) {
        alert('Por favor ingresa un nombre para el canal.');
        setCreating(false);
        return;
      }

      await channelService.createChannel({
        name: channelName,
        type: newChannelType,
        memberUserIds: memberIds.length > 0 ? memberIds : undefined,
      });

      setNewChannelName('');
      setSelectedTargetUserId('');
      setShowModal(false);
      onChannelCreated();
    } catch (err: any) {
      alert(err.message || 'Error al crear canal o chat directo');
    } finally {
      setCreating(false);
    }
  };

  const getChannelIcon = (type: ChannelType) => {
    switch (type) {
      case 'PRIVATE':
        return <Lock size={16} color="var(--color-primary)" />;
      case 'DIRECT':
        return <UserIcon size={16} color="var(--color-accent-blue)" />;
      default:
        return <Hash size={16} color="var(--color-text-muted)" />;
    }
  };

  return (
    <aside style={styles.sidebar}>
      <div style={styles.header}>
        <div style={styles.headerTitle}>
          <MessageSquare size={18} color="var(--color-primary)" />
          <h2 style={styles.title}>{t('channels')}</h2>
        </div>
        <button style={styles.addBtn} onClick={() => setShowModal(true)} title={t('new_channel')}>
          <Plus size={16} color="#FFFFFF" />
        </button>
      </div>

      <div style={styles.channelList}>
        {conversations.map((conv) => {
          const isActive = conv.channelId === activeChannelId;
          return (
            <button
              key={conv.channelId}
              style={{
                ...styles.channelItem,
                backgroundColor: isActive ? 'var(--color-secondary-light)' : 'transparent',
                borderColor: isActive ? 'var(--color-secondary-dark)' : 'transparent',
              }}
              onClick={() => onSelectChannel(conv.channelId)}
            >
              <div style={styles.channelInfo}>
                {getChannelIcon(conv.channelType)}
                <span
                  style={{
                    ...styles.channelName,
                    fontWeight: isActive ? 600 : 400,
                    color: isActive ? 'var(--color-text-surface)' : 'var(--color-text-main)',
                  }}
                >
                  {conv.channelName}
                </span>
              </div>

              {conv.unreadCount > 0 && (
                <span style={styles.unreadBadge}>{conv.unreadCount}</span>
              )}
            </button>
          );
        })}
      </div>

      {/* Modal para Crear Canal / Iniciar Chat Directo */}
      {showModal && (
        <div style={styles.modalOverlay}>
          <div style={styles.modalCard}>
            <div style={styles.modalHeader}>
              <Users size={20} color="var(--color-primary)" />
              <h3>{t('new_channel')} / Chat Directo</h3>
            </div>

            <form onSubmit={handleCreateChannel} style={styles.modalForm}>
              <div style={styles.formGroup}>
                <label style={styles.label}>Tipo de Conversación:</label>
                <select
                  value={newChannelType}
                  onChange={(e) => setNewChannelType(e.target.value as ChannelType)}
                  style={styles.select}
                >
                  <option value="PUBLIC">📢 Canal Público</option>
                  <option value="PRIVATE">🔒 Canal Privado</option>
                  <option value="DIRECT">💬 Chat Directo con Usuario</option>
                </select>
              </div>

              {newChannelType === 'DIRECT' ? (
                <div style={styles.formGroup}>
                  <label style={styles.label}>Selecciona el usuario de Riwi:</label>
                  <select
                    value={selectedTargetUserId}
                    onChange={(e) => setSelectedTargetUserId(e.target.value)}
                    required
                    style={styles.select}
                  >
                    <option value="">-- Elige un usuario --</option>
                    {availableUsers.map((u) => (
                      <option key={u.id} value={u.id}>
                        {u.fullName} ({u.jobTitle || u.email})
                      </option>
                    ))}
                  </select>
                </div>
              ) : (
                <div style={styles.formGroup}>
                  <label style={styles.label}>Nombre del Canal:</label>
                  <input
                    type="text"
                    placeholder="Ej. Proyecto Frontend"
                    value={newChannelName}
                    onChange={(e) => setNewChannelName(e.target.value)}
                    required
                    style={styles.input}
                  />
                </div>
              )}

              <div style={styles.modalActions}>
                <button type="button" onClick={() => setShowModal(false)} style={styles.cancelBtn}>
                  Cancelar
                </button>
                <button type="submit" disabled={creating} style={styles.createSubmit}>
                  {creating ? 'Creando...' : 'Iniciar'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </aside>
  );
};

const styles: Record<string, React.CSSProperties> = {
  sidebar: {
    width: '100%',
    backgroundColor: 'var(--color-surface)',
    borderRight: '1px solid var(--color-border)',
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
  },
  header: {
    padding: '16px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderBottom: '1px solid var(--color-border-light)',
  },
  headerTitle: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
  },
  title: {
    fontSize: '15px',
    color: 'var(--color-header-bg)',
    fontWeight: 600,
  },
  addBtn: {
    backgroundColor: 'var(--color-primary)',
    width: '28px',
    height: '28px',
    borderRadius: '6px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    cursor: 'pointer',
  },
  channelList: {
    flex: 1,
    overflowY: 'auto',
    padding: '12px 8px',
    display: 'flex',
    flexDirection: 'column',
    gap: '4px',
  },
  channelItem: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '10px 12px',
    borderRadius: '8px',
    textAlign: 'left',
    width: '100%',
    border: '1px solid transparent',
    cursor: 'pointer',
  },
  channelInfo: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    overflow: 'hidden',
  },
  channelName: {
    fontSize: '14px',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
  },
  unreadBadge: {
    backgroundColor: 'var(--color-primary)',
    color: '#FFFFFF',
    fontSize: '11px',
    fontWeight: 'bold',
    padding: '2px 7px',
    borderRadius: '10px',
  },
  modalOverlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.4)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 1000,
  },
  modalCard: {
    backgroundColor: '#FFFFFF',
    padding: '24px',
    borderRadius: '10px',
    width: '340px',
    boxShadow: '0 10px 25px rgba(0,0,0,0.2)',
  },
  modalHeader: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    marginBottom: '16px',
  },
  modalForm: {
    display: 'flex',
    flexDirection: 'column',
    gap: '14px',
  },
  formGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '6px',
  },
  label: {
    fontSize: '12px',
    fontWeight: 600,
    color: 'var(--color-text-main)',
  },
  select: {
    padding: '8px 10px',
    borderRadius: '6px',
    fontSize: '13px',
    width: '100%',
  },
  input: {
    padding: '8px 10px',
    borderRadius: '6px',
    fontSize: '13px',
    width: '100%',
  },
  modalActions: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: '8px',
    marginTop: '10px',
  },
  cancelBtn: {
    backgroundColor: 'var(--color-surface-alt)',
    color: 'var(--color-text-main)',
    padding: '8px 14px',
    borderRadius: '6px',
    fontSize: '13px',
  },
  createSubmit: {
    backgroundColor: 'var(--color-primary)',
    color: '#FFFFFF',
    padding: '8px 14px',
    borderRadius: '6px',
    fontSize: '13px',
    fontWeight: 600,
  },
};
