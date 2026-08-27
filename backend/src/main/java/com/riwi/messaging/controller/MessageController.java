package com.riwi.messaging.controller;

import com.riwi.messaging.dto.*;
import com.riwi.messaging.security.UserPrincipal;
import com.riwi.messaging.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Messages", description = "Endpoints for channel message history, keyset pagination, term search, soft edit and soft delete")
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/channels/{channelId}/messages")
    @Operation(summary = "Get channel message history (Keyset Pagination)", description = "Invokes SQL function rw_fn_get_channel_messages for keyset pagination without OFFSET")
    public ResponseEntity<KeysetPageResponse<MessageDTO>> getChannelMessages(
            @PathVariable UUID channelId,
            @RequestParam(required = false) Long afterId,
            @RequestParam(required = false, defaultValue = "30") Integer limit,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(messageService.getChannelMessages(channelId, afterId, limit, currentUser));
    }

    @PostMapping("/channels/{channelId}/messages")
    @Operation(summary = "Send message", description = "Sends a new message to a channel with RLS permission verification")
    public ResponseEntity<MessageDTO> sendMessage(
            @PathVariable UUID channelId,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        request.setChannelId(channelId);
        return new ResponseEntity<>(messageService.sendMessage(request, currentUser), HttpStatus.CREATED);
    }

    @PutMapping("/messages/{id}")
    @Operation(summary = "Edit message (Soft Edit)", description = "Edits message content while preserving the original text in rw_original_content for error recovery")
    public ResponseEntity<MessageDTO> editMessage(
            @PathVariable Long id,
            @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(messageService.editMessage(id, request.getContent(), currentUser));
    }

    @DeleteMapping("/messages/{id}")
    @Operation(summary = "Soft delete message", description = "Flags message as deleted (rw_is_deleted = true). Physical deletion is forbidden")
    public ResponseEntity<Void> softDeleteMessage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        messageService.softDeleteMessage(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/messages/search")
    @Operation(summary = "Search messages with term highlighting", description = "Invokes SQL function rw_fn_search_messages to search accessible messages with <mark> highlighting")
    public ResponseEntity<List<MessageSearchDTO>> searchMessages(
            @RequestParam("q") String query,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(messageService.searchMessages(query, currentUser));
    }
}
