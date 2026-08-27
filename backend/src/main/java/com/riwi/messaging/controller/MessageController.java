package com.riwi.messaging.controller;

import com.riwi.messaging.dto.*;
import com.riwi.messaging.security.UserPrincipal;
import com.riwi.messaging.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/channels/{channelId}/messages")
    public ResponseEntity<KeysetPageResponse<MessageDTO>> getChannelMessages(
            @PathVariable UUID channelId,
            @RequestParam(required = false) Long afterId,
            @RequestParam(required = false, defaultValue = "30") Integer limit,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(messageService.getChannelMessages(channelId, afterId, limit, currentUser));
    }

    @PostMapping("/channels/{channelId}/messages")
    public ResponseEntity<MessageDTO> sendMessage(
            @PathVariable UUID channelId,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        request.setChannelId(channelId);
        return new ResponseEntity<>(messageService.sendMessage(request, currentUser), HttpStatus.CREATED);
    }

    @PutMapping("/messages/{id}")
    public ResponseEntity<MessageDTO> editMessage(
            @PathVariable Long id,
            @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(messageService.editMessage(id, request.getContent(), currentUser));
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> softDeleteMessage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        messageService.softDeleteMessage(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/messages/search")
    public ResponseEntity<List<MessageSearchDTO>> searchMessages(
            @RequestParam("q") String query,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(messageService.searchMessages(query, currentUser));
    }
}
