import { apiClient } from './apiClient';
import { CopilotQueryRequest, CopilotResponse } from '../types/copilot';

export const copilotService = {
  async queryCopilot(payload: CopilotQueryRequest): Promise<CopilotResponse> {
    return apiClient<CopilotResponse>('/copilot/query', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  async getCopilotUsage(): Promise<any[]> {
    return apiClient<any[]>('/copilot/usage');
  },
};
