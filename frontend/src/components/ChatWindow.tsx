import React, { useState, useEffect, useRef } from 'react';
import { Message, MessageStatus } from '../types/message';
import { UserConversation } from '../types/channel';
import { User } from '../types/user';
import { useTranslation } from '../i18n/useTranslation';
import { messageService } from '../services/messageService';
import { websocketService } from '../services/websocketService';
import { Send, Clock, CheckCircle, AlertCircle, RefreshCw } from 'lucide-react';

interface ChatWindowProps {
  conversation: UserConversation | null;
  currentUser: User | null;
}

export const ChatWindow: React.FC<ChatWindowProps> = ({ conversation, currentUser }) => {
  const { t } = useTranslation();
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputText, setInputText] = useState('');
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(false);
  const [nextAfterId, setNextAfterId] = useState<number | undefined>(undefined);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Load initial messages for active channel
  useEffect(() => {
    if (!conversation) return;

    setMessages([]);
    setLoading(true);

    messageService.getChannelMessages(conversation.channelId)
      .then((res) => {
        setMessages(res.items);
        setHasMore(res.hasMore);
        setNextAfterId(res.nextAfterId);
      })
      .catch((err) => {
        console.error('Error fetching messages:', err);
      })
      .finally(() => {
        setLoading(false);
      });

    // Subscribe to STOMP WebSocket real-time updates with deduplication
    websocketService.subscribeToChannel(conversation.channelId, (incomingMsg) => {
      setMessages((prev) => {
        // 1. If message already exists by server ID, do nothing
        if (prev.some((m) => m.id === incomingMsg.id)) return prev;

        // 2. If it matches an optimistic pending message sent by the same user, replace it
        const optimisticIndex = prev.findIndex(
          (m) =>
            m.senderId === incomingMsg.senderId &&
            m.content === incomingMsg.content &&
            (m.tempId !== undefined || m.status === 'PENDING')
        );

        if (optimisticIndex !== -1) {
          const updated = [...prev];
          updated[optimisticIndex] = { ...incomingMsg, status: 'SENT' };
          return updated;
        }

        // 3. Otherwise, append new message from another user to the bottom
        return [...prev, incomingMsg];
      });
    });
  }, [conversation?.channelId]);

  // Scroll to bottom on new message
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Load older messages (Keyset pagination)
  const handleLoadOlderMessages = async () => {
    if (!conversation || !nextAfterId) return;

    try {
      const res = await messageService.getChannelMessages(conversation.channelId, nextAfterId);
      setMessages((prev) => [...res.items, ...prev]);
      setHasMore(res.hasMore);
      setNextAfterId(res.nextAfterId);
    } catch (err) {
      console.error('Error loading older messages:', err);
    }
  };

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputText.trim() || !conversation || !currentUser) return;

    const content = inputText.trim();
    setInputText('');

    // Optimistic UI update with PENDING state
    const tempId = `temp-${Date.now()}`;
    const optimisticMsg: Message = {
      id: Date.now(),
      tempId,
      channelId: conversation.channelId,
      senderId: currentUser.id,
      senderName: currentUser.fullName,
      content,
      status: 'PENDING',
      isEdited: false,
      isDeleted: false,
      createdAt: new Date().toISOString(),
    };

    setMessages((prev) => [...prev, optimisticMsg]);

    try {
      const savedMsg = await messageService.sendMessage({
        channelId: conversation.channelId,
        content,
      });

      // Update message status to SENT (or replace tempId)
      setMessages((prev) =>
        prev.map((m) => (m.tempId === tempId ? { ...savedMsg, status: 'SENT' } : m))
      );
    } catch (err) {
      // Update message status to FAILED
      setMessages((prev) =>
        prev.map((m) => (m.tempId === tempId ? { ...m, status: 'FAILED' } : m))
      );
    }
  };

  const renderStatusBadge = (status: MessageStatus) => {
    switch (status) {
      case 'PENDING':
        return (
          <span style={styles.badgePending} title={t('status_pending')}>
            <Clock size={12} color="#F59E0B" /> {t('status_pending')}
          </span>
        );
      case 'FAILED':
        return (
          <span style={styles.badgeFailed} title={t('status_failed')}>
            <AlertCircle size={12} color="#EF4444" /> {t('status_failed')}
          </span>
        );
      default:
        return (
          <span style={styles.badgeSent} title={t('status_sent')}>
            <CheckCircle size={12} color="#10B981" />
          </span>
        );
    }
  };

  if (!conversation) {
    return (
      <div style={styles.emptyState}>
        <p>{t('no_channels')}</p>
      </div>
    );
  }

  return (
    <section style={styles.container}>
      {/* Header del Canal */}
      <div style={styles.header}>
        <h3 style={styles.channelTitle}># {conversation.channelName}</h3>
        <span style={styles.channelTypeBadge}>{conversation.channelType}</span>
      </div>

      {/* Area de Mensajes */}
      <div style={styles.messagesBox}>
        {hasMore && (
          <button style={styles.loadMoreBtn} onClick={handleLoadOlderMessages}>
            <RefreshCw size={14} /> {t('load_more')}
          </button>
        )}

        {loading ? (
          <div style={styles.loadingText}>{t('loading_messages')}</div>
        ) : messages.length === 0 ? (
          <div style={styles.noMessagesText}>{t('no_messages')}</div>
        ) : (
          messages.map((msg) => {
            const isMe = msg.senderId === currentUser?.id;
            return (
              <div
                key={msg.tempId || msg.id}
                style={{
                  ...styles.messageRow,
                  justifyContent: isMe ? 'flex-end' : 'flex-start',
                }}
              >
                <div
                  style={{
                    ...styles.messageBubble,
                    backgroundColor: isMe ? 'var(--color-primary)' : 'var(--color-surface-alt)',
                    color: isMe ? '#FFFFFF' : 'var(--color-text-main)',
                  }}
                >
                  <div style={styles.senderHeader}>
                    <span style={styles.senderName}>{msg.senderName}</span>
                    <span style={styles.timeStr}>
                      {new Date(msg.createdAt).toLocaleTimeString([], {
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </span>
                  </div>

                  <p style={styles.msgContent}>{msg.content}</p>

                  <div style={styles.statusFooter}>
                    {renderStatusBadge(msg.status || 'SENT')}
                  </div>
                </div>
              </div>
            );
          })
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input de Mensaje */}
      <form onSubmit={handleSendMessage} style={styles.inputArea}>
        <input
          type="text"
          placeholder={t('message_placeholder')}
          value={inputText}
          onChange={(e) => setInputText(e.target.value)}
          style={styles.input}
        />
        <button type="submit" style={styles.sendBtn}>
          <Send size={18} color="#FFFFFF" />
        </button>
      </form>
    </section>
  );
};

const styles: Record<string, React.CSSProperties> = {
  container: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
    backgroundColor: '#FFFFFF',
  },
  emptyState: {
    flex: 1,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: 'var(--color-text-muted)',
  },
  header: {
    height: '56px',
    borderBottom: '1px solid var(--color-border)',
    padding: '0 20px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: 'var(--color-surface)',
  },
  channelTitle: {
    fontSize: '16px',
    color: 'var(--color-header-bg)',
    fontWeight: 600,
  },
  channelTypeBadge: {
    fontSize: '11px',
    backgroundColor: 'var(--color-secondary)',
    color: 'var(--color-text-surface)',
    padding: '2px 8px',
    borderRadius: '4px',
    fontWeight: 'bold',
  },
  messagesBox: {
    flex: 1,
    overflowY: 'auto',
    padding: '20px',
    display: 'flex',
    flexDirection: 'column',
    gap: '12px',
  },
  loadMoreBtn: {
    alignSelf: 'center',
    backgroundColor: 'var(--color-surface-alt)',
    border: '1px solid var(--color-border)',
    padding: '6px 14px',
    borderRadius: '16px',
    fontSize: '12px',
    color: 'var(--color-text-muted)',
    marginBottom: '10px',
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
  },
  loadingText: {
    textAlign: 'center',
    color: 'var(--color-text-muted)',
    fontSize: '13px',
  },
  noMessagesText: {
    textAlign: 'center',
    color: 'var(--color-text-placeholder)',
    marginTop: '40px',
  },
  messageRow: {
    display: 'flex',
    width: '100%',
  },
  messageBubble: {
    maxWidth: '65%',
    padding: '10px 14px',
    borderRadius: '12px',
    boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
  },
  senderHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    gap: '12px',
    marginBottom: '4px',
    fontSize: '11px',
    opacity: 0.8,
  },
  senderName: {
    fontWeight: 'bold',
  },
  timeStr: {
    fontSize: '10px',
  },
  msgContent: {
    fontSize: '14px',
    lineHeight: '1.4',
    wordBreak: 'break-word',
  },
  statusFooter: {
    display: 'flex',
    justifyContent: 'flex-end',
    marginTop: '4px',
  },
  badgePending: {
    fontSize: '10px',
    color: '#F59E0B',
    display: 'flex',
    alignItems: 'center',
    gap: '3px',
  },
  badgeSent: {
    fontSize: '10px',
    display: 'flex',
    alignItems: 'center',
  },
  badgeFailed: {
    fontSize: '10px',
    color: '#EF4444',
    display: 'flex',
    alignItems: 'center',
    gap: '3px',
  },
  inputArea: {
    padding: '16px 20px',
    borderTop: '1px solid var(--color-border)',
    display: 'flex',
    gap: '12px',
    backgroundColor: 'var(--color-surface)',
  },
  input: {
    flex: 1,
    padding: '12px 16px',
    fontSize: '14px',
    borderRadius: '24px',
  },
  sendBtn: {
    backgroundColor: 'var(--color-primary)',
    width: '44px',
    height: '44px',
    borderRadius: '50%',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
};
