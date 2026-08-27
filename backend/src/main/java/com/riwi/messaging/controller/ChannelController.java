package com.riwi.messaging.controller;

import com.riwi.messaging.dto.ChannelDTO;
import com.riwi.messaging.dto.CreateChannelRequest;
import com.riwi.messaging.dto.UserConversationDTO;
import com.riwi.messaging.security.UserPrincipal;
import com.riwi.messaging.service.ChannelService;
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
public class ChannelController {

    private final ChannelService channelService;

    @GetMapping
    public ResponseEntity<List<UserConversationDTO>> getUserConversations(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(channelService.getUserConversations(currentUser));
    }

    @PostMapping
    public ResponseEntity<ChannelDTO> createChannel(
            @Valid @RequestBody CreateChannelRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return new ResponseEntity<>(channelService.createChannel(request, currentUser), HttpStatus.CREATED);
    }
}
