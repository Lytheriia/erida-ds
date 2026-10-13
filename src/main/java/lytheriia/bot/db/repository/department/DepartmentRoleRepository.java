package lytheriia.bot.db.repository.department;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import lytheriia.bot.db.entity.department.DepartmentRoleEntity;

public interface DepartmentRoleRepository extends JpaRepository<DepartmentRoleEntity, Long> {

    @EntityGraph(attributePaths = "access")
    List<DepartmentRoleEntity> findByDepartmentIdOrderByLevel(Long departmentId);

    Optional<DepartmentRoleEntity> findByRoleId(Long roleId);

    @EntityGraph(attributePaths = "access")
    List<DepartmentRoleEntity> findByRoleIdIn(Collection<Long> roleIds);

    boolean existsByDepartmentIdAndLevel(Long departmentId, int level);
}