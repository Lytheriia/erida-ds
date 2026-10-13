package lytheriia.bot.db.entity.boosty;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "expired_boosty_subscriber")
public class ExpiredBoostySubscriberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "ss14_id")
    private UUID ss14Id;

    @Column(nullable = false)
    private long discordId;

    private int rank;

    private long promotedById;

    @CreationTimestamp
    private Instant expireAt;

    @Column(nullable = false)
    private Instant startedAt;

    protected ExpiredBoostySubscriberEntity() {
    }

    public ExpiredBoostySubscriberEntity(UUID ss14Id, long discordId, int rank, long promotedById, Instant startedAt) {
        this.ss14Id = ss14Id;
        this.discordId = discordId;
        this.rank = rank;
        this.promotedById = promotedById;
        this.startedAt = startedAt;
    }

    public Long getId() {
        return id;
    }

    public UUID getSs14Id() {
        return ss14Id;
    }

    public void setSs14Id(UUID ss14Id) {
        this.ss14Id = ss14Id;
    }

    public long getDiscordId() {
        return discordId;
    }

    public void setDiscordId(long discordId) {
        this.discordId = discordId;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public long getPromotedById() {
        return promotedById;
    }

    public void setPromotedById(long promotedById) {
        this.promotedById = promotedById;
    }

    public Instant getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Instant expireAt) {
        this.expireAt = expireAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }
}
