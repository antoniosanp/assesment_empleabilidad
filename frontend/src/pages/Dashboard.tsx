import React, { useState, useEffect } from 'react';
import { User } from '../types/user';
import { UserConversation } from '../types/channel';
import { Navbar } from '../components/Navbar';
import { ChannelSidebar } from '../components/ChannelSidebar';
import { ChatWindow } from '../components/ChatWindow';
import { CopilotPanel } from '../components/CopilotPanel';
import { UserProfileModal } from '../components/UserProfileModal';
import { channelService } from '../services/channelService';
import { websocketService } from '../services/websocketService';
import { MessageSquare, Bot, User as UserIcon } from 'lucide-react';

interface DashboardProps {
  user: User;
}

export const Dashboard: React.FC<DashboardProps> = ({ user }) => {
  const [conversations, setConversations] = useState<UserConversation[]>([]);
  const [activeChannelId, setActiveChannelId] = useState<string | null>(null);
  const [showProfileModal, setShowProfileModal] = useState(false);
  const [mobileTab, setMobileTab] = useState<'channels' | 'chat' | 'copilot'>('chat');

  // Load conversations on mount
  const loadConversations = async () => {
    try {
      const list = await channelService.getUserConversations();
      setConversations(list);
      if (list.length > 0 && !activeChannelId) {
        setActiveChannelId(list[0].channelId);
      }
    } catch (err) {
      console.error('Error loading conversations:', err);
    }
  };

  useEffect(() => {
    loadConversations();
    websocketService.connect();

    return () => {
      websocketService.disconnect();
    };
  }, []);

  const activeConversation =
    conversations.find((c) => c.channelId === activeChannelId) || null;

  return (
    <div style={styles.dashboardLayout}>
      <Navbar user={user} onOpenProfile={() => setShowProfileModal(true)} />

      {/* Main 3-Zone Fixed Container */}
      <main style={styles.mainContent}>
        {/* Zona 3 / Sidebar Left (Fixed 280px) */}
        <div style={styles.leftZoneContainer} className="zone-channels">
          <ChannelSidebar
            conversations={conversations}
            activeChannelId={activeChannelId}
            onSelectChannel={(id) => {
              setActiveChannelId(id);
              setMobileTab('chat');
            }}
            onChannelCreated={loadConversations}
          />
        </div>

        {/* Zona 1 / Center Chat Window (Flex 1 fills remaining space) */}
        <div style={styles.centerZoneContainer} className="zone-chat">
          <ChatWindow conversation={activeConversation} currentUser={user} />
        </div>

        {/* Zona 2 / Copilot Right Panel (Fixed 360px) */}
        <div style={styles.rightZoneContainer} className="zone-copilot">
          <CopilotPanel />
        </div>
      </main>

      {/* Bottom Navigation for Mobile Devices */}
      <div style={styles.mobileNav}>
        <button
          style={{ ...styles.mobileNavBtn, color: mobileTab === 'channels' ? 'var(--color-primary)' : '#676B73' }}
          onClick={() => setMobileTab('channels')}
        >
          <MessageSquare size={20} /> Canales
        </button>
        <button
          style={{ ...styles.mobileNavBtn, color: mobileTab === 'chat' ? 'var(--color-primary)' : '#676B73' }}
          onClick={() => setMobileTab('chat')}
        >
          <MessageSquare size={20} /> Chat
        </button>
        <button
          style={{ ...styles.mobileNavBtn, color: mobileTab === 'copilot' ? 'var(--color-primary)' : '#676B73' }}
          onClick={() => setMobileTab('copilot')}
        >
          <Bot size={20} /> Copiloto IA
        </button>
      </div>

      {/* Modal de Perfil de Usuario */}
      {showProfileModal && (
        <UserProfileModal user={user} onClose={() => setShowProfileModal(false)} />
      )}
    </div>
  );
};

const styles: Record<string, React.CSSProperties> = {
  dashboardLayout: {
    display: 'flex',
    flexDirection: 'column',
    height: '100vh',
    width: '100vw',
    overflow: 'hidden',
  },
  mainContent: {
    flex: 1,
    display: 'flex',
    overflow: 'hidden',
    position: 'relative',
    width: '100%',
    height: 'calc(100vh - 60px)',
  },
  leftZoneContainer: {
    width: '280px',
    minWidth: '280px',
    maxWidth: '280px',
    flexShrink: 0,
    height: '100%',
  },
  centerZoneContainer: {
    flex: 1,
    minWidth: 0,
    height: '100%',
    display: 'flex',
    flexDirection: 'column',
  },
  rightZoneContainer: {
    width: '360px',
    minWidth: '360px',
    maxWidth: '360px',
    flexShrink: 0,
    height: '100%',
  },
  mobileNav: {
    display: 'none',
    height: '56px',
    backgroundColor: '#FFFFFF',
    borderTop: '1px solid var(--color-border)',
    justifyContent: 'space-around',
    alignItems: 'center',
  },
  mobileNavBtn: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    fontSize: '11px',
    background: 'none',
    border: 'none',
    gap: '2px',
  },
};
