package com.riwi.messaging.service;

import com.riwi.messaging.dto.*;
import com.riwi.messaging.model.ChannelEntity;
import com.riwi.messaging.model.MessageEntity;
import com.riwi.messaging.model.UserEntity;
import com.riwi.messaging.repository.ChannelMemberRepository;
import com.riwi.messaging.repository.ChannelRepository;
import com.riwi.messaging.repository.MessageRepository;
import com.riwi.messaging.repository.UserRepository;
import com.riwi.messaging.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public KeysetPageResponse<MessageDTO> getChannelMessages(UUID channelId, Long afterId, Integer limit, UserPrincipal currentUser) {
        if (!channelMemberRepository.existsByChannelIdAndUserId(channelId, currentUser.getId())) {
            throw new AccessDeniedException("User is not a member of this channel");
        }

        int queryLimit = (limit == null || limit <= 0) ? 30 : limit;
        long queryAfterId = (afterId == null) ? 0L : afterId;

        List<MessageDTO> messages = messageRepository.findChannelMessagesKeyset(channelId, queryAfterId, queryLimit + 1).stream()
                .map(p -> MessageDTO.builder()
                        .id(p.getMessageId())
                        .channelId(p.getChannelId())
                        .senderId(p.getSenderId())
                        .senderName(p.getSenderName())
                        .content(p.getContent())
                        .status(p.getStatus())
                        .isEdited(p.getIsEdited())
                        .isDeleted(false)
                        .createdAt(p.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        boolean hasMore = messages.size() > queryLimit;
        if (hasMore) {
            messages = messages.subList(0, queryLimit);
        }

        Long nextAfterId = messages.isEmpty() ? null : messages.get(messages.size() - 1).getId();

        return KeysetPageResponse.<MessageDTO>builder()
                .items(messages)
                .nextAfterId(nextAfterId)
                .hasMore(hasMore)
                .build();
    }

    @Transactional
    public MessageDTO sendMessage(SendMessageRequest request, UserPrincipal currentUser) {
        if (!channelMemberRepository.existsByChannelIdAndUserId(request.getChannelId(), currentUser.getId())) {
            throw new AccessDeniedException("User is not a member of this channel");
        }

        ChannelEntity channel = channelRepository.findById(request.getChannelId())
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));

        UserEntity sender = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Sender user not found"));

        MessageEntity message = MessageEntity.builder()
                .channel(channel)
                .sender(sender)
                .content(request.getContent())
                .status("SENT")
                .isEdited(false)
                .isDeleted(false)
                .build();

        MessageEntity saved = messageRepository.save(message);

        return MessageDTO.builder()
                .id(saved.getId())
                .channelId(channel.getId())
                .senderId(sender.getId())
                .senderName(sender.getFullName())
                .content(saved.getContent())
                .status(saved.getStatus())
                .isEdited(saved.getIsEdited())
                .isDeleted(saved.getIsDeleted())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Transactional
    public MessageDTO editMessage(Long messageId, String newContent, UserPrincipal currentUser) {
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the sender can edit this message");
        }

        if (Boolean.TRUE.equals(message.getIsDeleted())) {
            throw new IllegalStateException("Cannot edit a deleted message");
        }

        if (message.getOriginalContent() == null) {
            message.setOriginalContent(message.getContent());
        }

        message.setContent(newContent);
        message.setIsEdited(true);

        MessageEntity updated = messageRepository.save(message);

        return MessageDTO.builder()
                .id(updated.getId())
                .channelId(updated.getChannel().getId())
                .senderId(updated.getSender().getId())
                .senderName(updated.getSender().getFullName())
                .content(updated.getContent())
                .status(updated.getStatus())
                .isEdited(updated.getIsEdited())
                .isDeleted(updated.getIsDeleted())
                .createdAt(updated.getCreatedAt())
                .build();
    }

    @Transactional
    public void softDeleteMessage(Long messageId, UserPrincipal currentUser) {
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Only the sender can delete this message");
        }

        message.setIsDeleted(true);
        message.setDeletedAt(OffsetDateTime.now());
        messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<MessageSearchDTO> searchMessages(String term, UserPrincipal currentUser) {
        return messageRepository.searchMessagesWithHighlight(currentUser.getId(), term).stream()
                .map(p -> MessageSearchDTO.builder()
                        .messageId(p.getMessageId())
                        .channelId(p.getChannelId())
                        .channelName(p.getChannelName())
                        .senderName(p.getSenderName())
                        .content(p.getContent())
                        .highlightedSnippet(p.getHighlightedSnippet())
                        .createdAt(p.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
