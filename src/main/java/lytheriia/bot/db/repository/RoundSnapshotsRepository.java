package lytheriia.bot.db.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import lytheriia.bot.db.entity.RoundSnapshotsEntity;

public interface RoundSnapshotsRepository extends JpaRepository<RoundSnapshotsEntity, Long> {

    List<RoundSnapshotsEntity> findByTimeStampAfterOrderByTimeStampAsc(LocalDateTime from);

    List<RoundSnapshotsEntity> findByTimeStampBetweenOrderByTimeStampAsc(LocalDateTime from, LocalDateTime to);

    default List<RoundSnapshotsEntity> findLastDays(int days) {
        return findByTimeStampAfterOrderByTimeStampAsc(LocalDateTime.now().minusDays(days));
    }

    default RoundSnapshotsEntity saveSnapshot(int roundId, int players) {
        RoundSnapshotsEntity entity = new RoundSnapshotsEntity();
        entity.setRoundId(roundId);
        entity.setPlayers(players);
        return save(entity);
    }
}