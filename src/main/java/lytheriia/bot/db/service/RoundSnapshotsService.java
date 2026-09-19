package lytheriia.bot.db.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lytheriia.bot.db.entity.RoundSnapshotsEntity;
import lytheriia.bot.db.repository.RoundSnapshotsRepository;

@Service
public class RoundSnapshotsService {

    private final RoundSnapshotsRepository roundSnapshotsRepository;

    public RoundSnapshotsService(RoundSnapshotsRepository roundSnapshotsRepository) {
        this.roundSnapshotsRepository = roundSnapshotsRepository;
    }

    @Transactional()
    public List<RoundSnapshotsEntity> findLastDays(int days) {
        return roundSnapshotsRepository.findLastDays(days);
    }

    @Transactional()
    public void saveSnapshot(int roundId, int players) {
        roundSnapshotsRepository.saveSnapshot(roundId, players);
    }
}
