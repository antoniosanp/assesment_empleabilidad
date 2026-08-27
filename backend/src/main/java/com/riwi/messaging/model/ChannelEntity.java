package com.riwi.messaging.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "rw_channels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rw_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "rw_name", length = 100)
    private String name;

    @Column(name = "rw_type", nullable = false, length = 20)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rw_created_by", foreignKey = @ForeignKey(name = "rw_fk_channels_created_by"))
    @ToString.Exclude
    private UserEntity createdBy;

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
