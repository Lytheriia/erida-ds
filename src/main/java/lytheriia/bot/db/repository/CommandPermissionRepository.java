package lytheriia.bot.db.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import lytheriia.bot.db.entity.CommandPermissionEntity;

public interface CommandPermissionRepository extends JpaRepository<CommandPermissionEntity, Long> {

    boolean existsByCommandNameAndRoleId(String commandName, long roleId);

    boolean existsByCommandNameAndRoleIdIn(String commandName, Collection<Long> roleIds);

    List<CommandPermissionEntity> findByCommandName(String commandName);

    long deleteByCommandNameAndRoleId(String commandName, long roleId);
}