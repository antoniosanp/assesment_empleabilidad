package com.riwi.messaging.repository;

import com.riwi.messaging.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Procedure(procedureName = "rw_sp_update_or_delete_user")
    void updateOrDeleteUser(
            @Param("p_user_id") UUID userId,
            @Param("p_full_name") String fullName,
            @Param("p_job_title") String jobTitle,
            @Param("p_is_active") Boolean isActive
    );
}
