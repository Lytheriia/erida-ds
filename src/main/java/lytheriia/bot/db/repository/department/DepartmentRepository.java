package lytheriia.bot.db.repository.department;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import lytheriia.bot.db.entity.department.DepartmentEntity;

public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {

    Optional<DepartmentEntity> findByGuildIdAndNameIgnoreCase(long guildId, String name);

    boolean existsByGuildIdAndNameIgnoreCase(long guildId, String name);

    List<DepartmentEntity> findByGuildIdOrderByName(long guildId);
}