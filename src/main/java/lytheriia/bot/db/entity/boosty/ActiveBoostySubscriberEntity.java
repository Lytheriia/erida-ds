package lytheriia.bot.db.entity.boosty;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "active_boosty_subscriber")
public class ActiveBoostySubscriberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "discord_id")
    private long discordId;

    private long promotedById;

    private int rank;

    @CreationTimestamp
    private Instant startedAt;

    protected ActiveBoostySubscriberEntity() {
    }

    public ActiveBoostySubscriberEntity(long discordId, long promotedById, int rank) {
        this.discordId = discordId;
        this.promotedById = promotedById;
        this.rank = rank;
    }

    public long getDiscordId() {
        return discordId;
    }

    public void setDiscordId(long discordId) {
        this.discordId = discordId;
    }

    public long getPromotedById() {
        return promotedById;
    }

    public void setPromotedById(long promotedById) {
        this.promotedById = promotedById;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Long getId() {
        return id;
    }

}
