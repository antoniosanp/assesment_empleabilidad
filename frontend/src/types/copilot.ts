export interface CopilotSourceCitation {
  messageId: number;
  channelId: string;
  channelName: string;
  senderName: string;
  contentSnippet: string;
}

export interface CopilotResponse {
  answer: string;
  citations: CopilotSourceCitation[];
  tokensUsed: number;
  isRefusedDueToPermissionsOrContext: boolean;
}

export interface CopilotQueryRequest {
  query: string;
}
