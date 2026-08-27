package com.riwi.messaging.repository;

import com.riwi.messaging.model.ChannelMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChannelMemberRepository extends JpaRepository<ChannelMemberEntity, Long> {

    Optional<ChannelMemberEntity> findByChannelIdAndUserId(UUID channelId, UUID userId);

    boolean existsByChannelIdAndUserId(UUID channelId, UUID userId);

    List<ChannelMemberEntity> findByChannelId(UUID channelId);

    List<ChannelMemberEntity> findByUserId(UUID userId);
}
