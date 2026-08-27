package com.riwi.messaging.controller;

import com.riwi.messaging.dto.ChannelDTO;
import com.riwi.messaging.dto.CreateChannelRequest;
import com.riwi.messaging.dto.UserConversationDTO;
import com.riwi.messaging.security.UserPrincipal;
import com.riwi.messaging.service.ChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
@Tag(name = "Channels", description = "Endpoints for channel management and user conversation overview")
public class ChannelController {

    private final ChannelService channelService;

    @GetMapping
    @Operation(summary = "Get user conversations", description = "Queries PostgreSQL view rw_v_user_conversations to list channels, last message, and unread counts for current actor")
    public ResponseEntity<List<UserConversationDTO>> getUserConversations(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(channelService.getUserConversations(currentUser));
    }

    @PostMapping
    @Operation(summary = "Create channel", description = "Creates a new PUBLIC, PRIVATE, or DIRECT channel and assigns initial members")
    public ResponseEntity<ChannelDTO> createChannel(
            @Valid @RequestBody CreateChannelRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return new ResponseEntity<>(channelService.createChannel(request, currentUser), HttpStatus.CREATED);
    }
}
