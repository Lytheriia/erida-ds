package lytheriia.bot.db.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lytheriia.bot.db.entity.DiscordLinkedAccountEntity;
import lytheriia.bot.db.entity.boosty.ActiveBoostySubscriberEntity;
import lytheriia.bot.db.entity.boosty.ExpiredBoostySubscriberEntity;
import lytheriia.bot.db.repository.DiscordLinkRepository;
import lytheriia.bot.db.repository.boosty.ActiveBoostySubscriberRepository;
import lytheriia.bot.db.repository.boosty.ExpiredBoostySubscriberRepository;

@Service
public class BoostySubscriberService {

    private final ActiveBoostySubscriberRepository activeRepository;
    private final ExpiredBoostySubscriberRepository expiredRepository;
    private final DiscordLinkRepository discordLinkRepository;

    public BoostySubscriberService(ActiveBoostySubscriberRepository activeRepository,
            ExpiredBoostySubscriberRepository expiredRepository, DiscordLinkRepository discordLinkRepository) {
        this.activeRepository = activeRepository;
        this.expiredRepository = expiredRepository;
        this.discordLinkRepository = discordLinkRepository;
    }

    @Transactional()
    public Optional<ActiveBoostySubscriberEntity> findActiveByUser(UUID ss14UserId) {
        return discordLinkRepository.findBySs14UserId(ss14UserId)
                .flatMap(link -> activeRepository
                        .findTopByDiscordIdOrderByRankDesc(link.getDiscordId()));
    }

    @Transactional
    public void subscribe(long discordId, long promotedBy, int rank) {
        activeRepository.findByDiscordIdAndRank(discordId, rank)
                .ifPresent(old -> {
                    archive(old);
                    activeRepository.flush();
                });

        activeRepository.save(new ActiveBoostySubscriberEntity(discordId, promotedBy, rank));
    }

    @Transactional
    public void unsubscribe(long discordId, int rank) {
        activeRepository.findByDiscordIdAndRank(discordId, rank)
                .ifPresent(this::archive);
    }

    private void archive(ActiveBoostySubscriberEntity active) {
        UUID ss14Id = discordLinkRepository.findByDiscordId(active.getDiscordId())
                .map(DiscordLinkedAccountEntity::getSs14UserId)
                .orElse(new UUID(0L, 0L));

        expiredRepository.save(new ExpiredBoostySubscriberEntity(
                ss14Id,
                active.getDiscordId(),
                active.getRank(),
                active.getPromotedById(),
                active.getStartedAt()));

        activeRepository.delete(active);
    }
}
