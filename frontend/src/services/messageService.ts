import { apiClient } from './apiClient';
import { KeysetPageResponse, Message, SendMessageRequest } from '../types/message';

export const messageService = {
  async getChannelMessages(
    channelId: string,
    afterId?: number,
    limit: number = 30
  ): Promise<KeysetPageResponse<Message>> {
    let url = `/channels/${channelId}/messages?limit=${limit}`;
    if (afterId !== undefined && afterId !== null) {
      url += `&afterId=${afterId}`;
    }
    return apiClient<KeysetPageResponse<Message>>(url);
  },

  async sendMessage(payload: SendMessageRequest): Promise<Message> {
    return apiClient<Message>(`/channels/${payload.channelId}/messages`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  async searchMessages(term: string): Promise<any[]> {
    return apiClient<any[]>(`/messages/search?term=${encodeURIComponent(term)}`);
  },
};
