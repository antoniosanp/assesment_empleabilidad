import { apiClient } from './apiClient';
import { Channel, ChannelType, UserConversation } from '../types/channel';

export interface CreateChannelPayload {
  name: string;
  type: ChannelType;
  memberUserIds?: string[];
}

export const channelService = {
  async getUserConversations(): Promise<UserConversation[]> {
    return apiClient<UserConversation[]>('/channels');
  },

  async createChannel(payload: CreateChannelPayload): Promise<Channel> {
    return apiClient<Channel>('/channels', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },
};
