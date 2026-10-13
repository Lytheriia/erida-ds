package lytheriia.bot.db.repository.department;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import lytheriia.bot.db.entity.department.DepartmentControlEntity;
import lytheriia.bot.db.entity.department.DepartmentControlId;
import lytheriia.bot.db.entity.department.DepartmentEntity;

public interface DepartmentControlRepository
        extends JpaRepository<DepartmentControlEntity, DepartmentControlId> {

    @Query("select c.controlled from DepartmentControlEntity c where c.controller.id = :id")
    List<DepartmentEntity> findControlled(@Param("id") Long controllerId);

    @Query("select c.controller from DepartmentControlEntity c where c.controlled.id = :id")
    List<DepartmentEntity> findControllers(@Param("id") Long controlledId);

    boolean existsByControllerIdAndControlledId(Long controllerId, Long controlledId);

    @Modifying
    @Query("delete from DepartmentControlEntity c where c.controller.id = :controller and c.controlled.id = :controlled")
    int deleteLink(@Param("controller") Long controllerId, @Param("controlled") Long controlledId);
}