package com.riwi.messaging.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "rw_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rw_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rw_channel_id", nullable = false, foreignKey = @ForeignKey(name = "rw_fk_messages_channel"))
    @ToString.Exclude
    private ChannelEntity channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rw_sender_id", nullable = false, foreignKey = @ForeignKey(name = "rw_fk_messages_sender"))
    @ToString.Exclude
    private UserEntity sender;

    @Column(name = "rw_content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "rw_original_content", columnDefinition = "TEXT")
    private String originalContent;

    @Column(name = "rw_status", nullable = false, length = 20)
    @Builder.Default
    private String status = "SENT";

    @Column(name = "rw_is_edited", nullable = false)
    @Builder.Default
    private Boolean isEdited = false;

    @Column(name = "rw_is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "rw_deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "rw_created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "rw_updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
