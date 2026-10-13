package lytheriia.bot.db.repository.boosty;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import lytheriia.bot.db.entity.boosty.ActiveBoostySubscriberEntity;

public interface ActiveBoostySubscriberRepository extends JpaRepository<ActiveBoostySubscriberEntity, Long> {

    boolean existsByDiscordId(long discordId);

    Optional<ActiveBoostySubscriberEntity> findByDiscordId(long discordId);

    Optional<ActiveBoostySubscriberEntity> findByDiscordIdAndRank(long discordId, int rank);

    List<ActiveBoostySubscriberEntity> findByRank(int rank);

    Optional<ActiveBoostySubscriberEntity> findTopByDiscordIdOrderByRankDesc(long discordId);
}