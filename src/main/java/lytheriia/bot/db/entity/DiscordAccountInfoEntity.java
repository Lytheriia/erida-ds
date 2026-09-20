package lytheriia.bot.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "discord_account_info")
public class DiscordAccountInfoEntity {

    @Id
    @Column(name = "discord_id", nullable = false, unique = true)
    private long discordId;

    private boolean verified;

    private boolean booster;

    protected DiscordAccountInfoEntity() {
    }

    public DiscordAccountInfoEntity(long discordId) {
        this.discordId = discordId;
    }

    public long getDiscordId() {
        return discordId;
    }

    public void setDiscordId(long discordId) {
        this.discordId = discordId;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isBooster() {
        return booster;
    }

    public void setBooster(boolean booster) {
        this.booster = booster;
    }
}
