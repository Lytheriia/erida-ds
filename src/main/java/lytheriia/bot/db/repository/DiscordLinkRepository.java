package lytheriia.bot.db.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import lytheriia.bot.db.entity.DiscordLinkedAccountEntity;

public interface DiscordLinkRepository extends JpaRepository<DiscordLinkedAccountEntity, Long> {

    boolean existsByDiscordId(long discordId);

    boolean existsBySs14UserId(UUID ss14UserId);

    Optional<DiscordLinkedAccountEntity> findByDiscordId(long discordId);

    Optional<DiscordLinkedAccountEntity> findBySs14UserId(UUID ss14UserId);

    long deleteByDiscordId(long discordId);
}