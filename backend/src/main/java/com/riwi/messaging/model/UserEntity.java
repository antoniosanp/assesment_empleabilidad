package com.riwi.messaging.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "rw_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rw_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "rw_email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "rw_password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "rw_full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "rw_job_title", nullable = false, length = 100)
    private String jobTitle;

    @Column(name = "rw_role", nullable = false, length = 50)
    private String role;

    @Column(name = "rw_is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

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
