package lytheriia.bot.db.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lytheriia.bot.db.entity.DiscordAccountInfoEntity;
import lytheriia.bot.db.repository.DiscordAccountInfoRepository;

@Service
public class DiscordAccountInfoService {

    public record AccountStatus(boolean verified, boolean booster) {
    }

    private final DiscordAccountInfoRepository infoRepository;

    public DiscordAccountInfoService(DiscordAccountInfoRepository infoRepository) {
        this.infoRepository = infoRepository;
    }

    @Transactional(readOnly = true)
    public AccountStatus getStatus(long discordId) {
        return infoRepository.findByDiscordId(discordId)
                .map(i -> new AccountStatus(i.isVerified(), i.isBooster()))
                .orElse(new AccountStatus(false, false));
    }

    @Transactional
    public void createIfMissing(long discordId) {
        getOrCreate(discordId);
    }

    @Transactional
    public void setVerified(long discordId, boolean verified) {
        getOrCreate(discordId).setVerified(verified);
    }

    @Transactional
    public void setBooster(long discordId, boolean booster) {
        getOrCreate(discordId).setBooster(booster);
    }

    private DiscordAccountInfoEntity getOrCreate(long discordId) {
        return infoRepository.findByDiscordId(discordId).orElseGet(() -> {
            return infoRepository.save(new DiscordAccountInfoEntity(discordId));
        });
    }
}