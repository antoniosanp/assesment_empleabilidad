package com.riwi.messaging.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "rw_channel_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rw_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rw_channel_id", nullable = false, foreignKey = @ForeignKey(name = "rw_fk_members_channel"))
    @ToString.Exclude
    private ChannelEntity channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rw_user_id", nullable = false, foreignKey = @ForeignKey(name = "rw_fk_members_user"))
    @ToString.Exclude
    private UserEntity user;

    @Column(name = "rw_member_role", nullable = false, length = 20)
    @Builder.Default
    private String memberRole = "MEMBER";

    @Column(name = "rw_joined_at", nullable = false, updatable = false)
    private OffsetDateTime joinedAt;

    @Column(name = "rw_last_read_at", nullable = false)
    private OffsetDateTime lastReadAt;

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = OffsetDateTime.now();
        }
        if (lastReadAt == null) {
            lastReadAt = OffsetDateTime.now();
        }
    }
}
