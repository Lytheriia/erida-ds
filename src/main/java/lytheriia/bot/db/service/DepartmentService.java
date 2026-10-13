package lytheriia.bot.db.service;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lytheriia.bot.db.entity.department.DepartmentControlEntity;
import lytheriia.bot.db.entity.department.DepartmentEntity;
import lytheriia.bot.db.entity.department.DepartmentRoleEntity;
import lytheriia.bot.db.repository.department.DepartmentControlRepository;
import lytheriia.bot.db.repository.department.DepartmentRepository;
import lytheriia.bot.db.repository.department.DepartmentRoleRepository;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departments;
    private final DepartmentRoleRepository roles;
    private final DepartmentControlRepository controls;

    public DepartmentService(DepartmentRepository departments,
            DepartmentRoleRepository roles,
            DepartmentControlRepository controls) {
        this.departments = departments;
        this.roles = roles;
        this.controls = controls;
    }

    public DepartmentEntity create(
            long guildId,
            String name,
            long roleId) {
        if (departments.existsByGuildIdAndNameIgnoreCase(guildId, name)) {
            throw new IllegalStateException(
                    "Департамент «" + name + "» уже существует");
        }

        return departments.save(
                new DepartmentEntity(guildId, name, roleId));
    }

    public void delete(long guildId, String name) {
        departments.delete(get(guildId, name));
    }

    @Transactional(readOnly = true)
    public List<DepartmentEntity> list(long guildId) {
        return departments.findByGuildIdOrderByName(guildId);
    }

    @Transactional(readOnly = true)
    public DepartmentEntity get(long guildId, String name) {
        return departments.findByGuildIdAndNameIgnoreCase(guildId, name)
                .orElseThrow(() -> new NoSuchElementException("Департамент «" + name + "» не найден"));
    }

    public DepartmentRoleEntity addRole(long guildId, String department, long roleId, int level, boolean canManage) {
        DepartmentEntity dept = get(guildId, department);
        if (roles.findByRoleId(roleId).isPresent()) {
            throw new IllegalStateException("Эта роль уже привязана к департаменту");
        }
        if (roles.existsByDepartmentIdAndLevel(dept.getId(), level)) {
            throw new IllegalStateException("Уровень " + level + " уже занят");
        }
        return roles.save(new DepartmentRoleEntity(dept, roleId, level, canManage));
    }

    public void removeRole(long roleId) {
        roles.delete(getRole(roleId));
    }

    public void setLevel(long roleId, int level) {
        DepartmentRoleEntity role = getRole(roleId);
        if (role.getLevel() == level)
            return;
        if (roles.existsByDepartmentIdAndLevel(role.getDepartment().getId(), level)) {
            throw new IllegalStateException("Уровень " + level + " уже занят");
        }
        role.setLevel(level);
    }

    public void setCanManage(long roleId, boolean value) {
        getRole(roleId).setCanManage(value);
    }

    @Transactional(readOnly = true)
    public List<DepartmentRoleEntity> rolesOf(long guildId, String department) {
        return roles.findByDepartmentIdOrderByLevel(get(guildId, department).getId());
    }

    private DepartmentRoleEntity getRole(long roleId) {
        return roles.findByRoleId(roleId)
                .orElseThrow(() -> new NoSuchElementException("Роль не привязана к департаменту"));
    }

    public boolean grant(long roleId, String access) {
        return getRole(roleId).grant(access);
    }

    public boolean revoke(long roleId, String access) {
        return getRole(roleId).revoke(access);
    }

    @Transactional(readOnly = true)
    public boolean hasAccess(Collection<Long> memberRoleIds, String access) {
        return roles.findByRoleIdIn(memberRoleIds).stream()
                .anyMatch(r -> r.getAccess().contains(access));
    }

    public void addControl(long guildId, String controller, String controlled) {
        DepartmentEntity a = get(guildId, controller);
        DepartmentEntity b = get(guildId, controlled);
        if (controls.existsByControllerIdAndControlledId(a.getId(), b.getId())) {
            throw new IllegalStateException("Связь уже существует");
        }
        controls.save(new DepartmentControlEntity(a, b));
    }

    public void removeControl(long guildId, String controller, String controlled) {
        DepartmentEntity a = get(guildId, controller);
        DepartmentEntity b = get(guildId, controlled);
        if (controls.deleteLink(a.getId(), b.getId()) == 0) {
            throw new NoSuchElementException("Такой связи нет");
        }
    }

    @Transactional(readOnly = true)
    public List<DepartmentEntity> controlledBy(long guildId, String department) {
        return controls.findControlled(get(guildId, department).getId());
    }

    @Transactional(readOnly = true)
    public List<DepartmentEntity> controllersOf(long guildId, String department) {
        return controls.findControllers(get(guildId, department).getId());
    }

    @Transactional(readOnly = true)
    public Set<String> accessOf(long roleId) {
        return Set.copyOf(getRole(roleId).getAccess());
    }

    @Transactional(readOnly = true)
    public DepartmentRoleEntity findRole(Collection<Long> roleIds) {
        return roles.findByRoleIdIn(roleIds)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean canManage(
            Collection<Long> executorRoleIds,
            Collection<Long> targetRoleIds) {
        DepartmentRoleEntity executorRole = findRole(executorRoleIds);
        DepartmentRoleEntity targetRole = findRole(targetRoleIds);

        if (executorRole == null || targetRole == null) {
            return false;
        }

        if (!executorRole.isCanManage()) {
            return false;
        }

        if (!executorRole.getDepartment().getId()
                .equals(targetRole.getDepartment().getId())) {
            return false;
        }

        return executorRole.getLevel() < targetRole.getLevel();
    }

    @Transactional(readOnly = true)
    public DepartmentRoleEntity findDepartmentRole(Collection<Long> roleIds) {
        List<DepartmentRoleEntity> found = roles.findByRoleIdIn(roleIds);

        if (found.isEmpty()) {
            return null;
        }

        if (found.size() > 1) {
            throw new IllegalStateException(
                    "Пользователь состоит более чем в одном департаменте");
        }

        return found.getFirst();
    }
}