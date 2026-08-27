package com.riwi.messaging.service;

import com.riwi.messaging.dto.ChannelDTO;
import com.riwi.messaging.dto.CreateChannelRequest;
import com.riwi.messaging.dto.UserConversationDTO;
import com.riwi.messaging.model.ChannelEntity;
import com.riwi.messaging.model.ChannelMemberEntity;
import com.riwi.messaging.model.UserEntity;
import com.riwi.messaging.repository.ChannelMemberRepository;
import com.riwi.messaging.repository.ChannelRepository;
import com.riwi.messaging.repository.UserRepository;
import com.riwi.messaging.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserConversationDTO> getUserConversations(UserPrincipal currentUser) {
        return channelRepository.findUserConversations(currentUser.getId()).stream()
                .map(p -> UserConversationDTO.builder()
                        .channelId(p.getChannelId())
                        .channelName(p.getChannelName())
                        .channelType(p.getChannelType())
                        .userId(p.getUserId())
                        .memberRole(p.getMemberRole())
                        .lastReadAt(p.getLastReadAt() != null ? p.getLastReadAt().atOffset(ZoneOffset.UTC) : null)
                        .lastMessageId(p.getLastMessageId())
                        .lastMessageContent(p.getLastMessageContent())
                        .lastMessageAt(p.getLastMessageAt() != null ? p.getLastMessageAt().atOffset(ZoneOffset.UTC) : null)
                        .lastMessageSenderId(p.getLastMessageSenderId())
                        .unreadCount(p.getUnreadCount())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public ChannelDTO createChannel(CreateChannelRequest request, UserPrincipal currentUser) {
        UserEntity creator = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Creator user not found"));

        ChannelEntity channel = ChannelEntity.builder()
                .name(request.getName())
                .type(request.getType())
                .createdBy(creator)
                .build();

        ChannelEntity savedChannel = channelRepository.save(channel);

        // Add creator as OWNER
        ChannelMemberEntity ownerMember = ChannelMemberEntity.builder()
                .channel(savedChannel)
                .user(creator)
                .memberRole("OWNER")
                .build();
        channelMemberRepository.save(ownerMember);

        // Add additional members if specified
        if (request.getMemberUserIds() != null) {
            for (UUID memberId : request.getMemberUserIds()) {
                if (!memberId.equals(currentUser.getId())) {
                    userRepository.findById(memberId).ifPresent(user -> {
                        ChannelMemberEntity member = ChannelMemberEntity.builder()
                                .channel(savedChannel)
                                .user(user)
                                .memberRole("MEMBER")
                                .build();
                        channelMemberRepository.save(member);
                    });
                }
            }
        }

        return ChannelDTO.builder()
                .id(savedChannel.getId())
                .name(savedChannel.getName())
                .type(savedChannel.getType())
                .createdBy(creator.getId())
                .createdAt(savedChannel.getCreatedAt())
                .build();
    }
}
