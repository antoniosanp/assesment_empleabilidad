export type ChannelType = 'PUBLIC' | 'PRIVATE' | 'DIRECT';

export interface Channel {
  id: string;
  name: string;
  type: ChannelType;
  createdBy: string;
  createdAt: string;
}

export interface UserConversation {
  channelId: string;
  channelName: string;
  channelType: ChannelType;
  userId: string;
  memberRole: string;
  lastReadAt?: string;
  lastMessageId?: number;
  lastMessageContent?: string;
  lastMessageAt?: string;
  lastMessageSenderId?: string;
  unreadCount: number;
}
