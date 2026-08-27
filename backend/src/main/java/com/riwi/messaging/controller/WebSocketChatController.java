package com.riwi.messaging.controller;

import com.riwi.messaging.dto.MessageDTO;
import com.riwi.messaging.dto.SendMessageRequest;
import com.riwi.messaging.security.UserPrincipal;
import com.riwi.messaging.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketChatController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(
            @Payload SendMessageRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.info("WebSocket message received for channel {}: {}", request.getChannelId(), request.getContent());
        
        MessageDTO messageDTO = messageService.sendMessage(request, currentUser);

        // Broadcast to channel topic in real time: /topic/channels/{channelId}
        String destination = "/topic/channels/" + messageDTO.getChannelId();
        messagingTemplate.convertAndSend(destination, messageDTO);
    }
}
