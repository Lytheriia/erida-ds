package lytheriia.bot.db.entity.department;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "department_role")
public class DepartmentRoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private DepartmentEntity department;

    @Column(nullable = false, unique = true, updatable = false)
    private Long roleId;

    @Column()
    private int level;

    @Column()
    private boolean canManage;

    protected DepartmentRoleEntity() {
    }

    public DepartmentRoleEntity(DepartmentEntity department, Long roleId, int level, boolean canManage) {
        this.department = Objects.requireNonNull(department);
        this.roleId = Objects.requireNonNull(roleId);
        this.level = requireValidLevel(level);
        this.canManage = canManage;
    }

    public Long getId() {
        return id;
    }

    public DepartmentEntity getDepartment() {
        return department;
    }

    public Long getRoleId() {
        return roleId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = requireValidLevel(level);
    }

    private static int requireValidLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("level should be higher then 1");
        }
        return level;
    }

    public boolean isCanManage() {
        return canManage;
    }

    public void setCanManage(boolean canManage) {
        this.canManage = canManage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DepartmentRoleEntity that))
            return false;
        return Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(roleId);
    }

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "role_access", joinColumns = @JoinColumn(name = "department_role_id", nullable = false))
    @Column(name = "access", nullable = false)
    private Set<String> access = new HashSet<>();

    public Set<String> getAccess() {
        return Collections.unmodifiableSet(access);
    }

    public boolean grant(String value) {
        return access.add(Objects.requireNonNull(value));
    }

    public boolean revoke(String value) {
        return access.remove(value);
    }
}