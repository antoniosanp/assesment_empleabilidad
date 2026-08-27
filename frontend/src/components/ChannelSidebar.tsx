import React, { useState } from 'react';
import { UserConversation, ChannelType } from '../types/channel';
import { useTranslation } from '../i18n/useTranslation';
import { Hash, Lock, User as UserIcon, Plus, MessageSquare } from 'lucide-react';
import { channelService } from '../services/channelService';

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
  const [creating, setCreating] = useState(false);

  const handleCreateChannel = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newChannelName.trim()) return;

    setCreating(true);
    try {
      await channelService.createChannel({
        name: newChannelName.trim(),
        type: newChannelType,
      });
      setNewChannelName('');
      setShowModal(false);
      onChannelCreated();
    } catch (err: any) {
      alert(err.message || 'Error al crear canal');
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
        <button style={styles.addBtn} onClick={() => setShowModal(true)}>
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

      {/* Modal para Crear Nuevo Canal */}
      {showModal && (
        <div style={styles.modalOverlay}>
          <div style={styles.modalCard}>
            <h3>{t('new_channel')}</h3>
            <form onSubmit={handleCreateChannel} style={styles.modalForm}>
              <input
                type="text"
                placeholder="Nombre del canal..."
                value={newChannelName}
                onChange={(e) => setNewChannelName(e.target.value)}
                required
              />
              <select
                value={newChannelType}
                onChange={(e) => setNewChannelType(e.target.value as ChannelType)}
              >
                <option value="PUBLIC">Público</option>
                <option value="PRIVATE">Privado</option>
              </select>
              <div style={styles.modalActions}>
                <button type="button" onClick={() => setShowModal(false)}>
                  Cancelar
                </button>
                <button type="submit" disabled={creating} style={styles.createSubmit}>
                  {creating ? '...' : 'Crear'}
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
    width: '280px',
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
    zIndex: 100,
  },
  modalCard: {
    backgroundColor: '#FFFFFF',
    padding: '20px',
    borderRadius: '8px',
    width: '300px',
  },
  modalForm: {
    display: 'flex',
    flexDirection: 'column',
    gap: '12px',
    marginTop: '12px',
  },
  modalActions: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: '8px',
    marginTop: '8px',
  },
  createSubmit: {
    backgroundColor: 'var(--color-primary)',
    color: '#FFFFFF',
    padding: '6px 14px',
    borderRadius: '6px',
  },
};
