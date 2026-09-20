package lytheriia.bot.db.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import lytheriia.bot.db.entity.DiscordAccountInfoEntity;

public interface DiscordAccountInfoRepository extends JpaRepository<DiscordAccountInfoEntity, Long> {
    boolean existsByDiscordId(long discordId);

    Optional<DiscordAccountInfoEntity> findByDiscordId(long discordId);

    List<DiscordAccountInfoEntity> findByVerifiedTrue();

    List<DiscordAccountInfoEntity> findByBoosterTrue();

    long deleteByDiscordId(long discordId);
}
