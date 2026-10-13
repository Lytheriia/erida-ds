package lytheriia.bot.db.entity.department;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "department")
public class DepartmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long guildId;
    private String name;

    @Column(nullable = false, unique = true)
    private Long roleId;

    protected DepartmentEntity() {
    }

    public DepartmentEntity(long guildId, String name, long roleId) {
        this.guildId = guildId;
        this.name = Objects.requireNonNull(name);
        this.roleId = roleId;
    }

    public Long getId() {
        return id;
    }

    public long getGuildId() {
        return guildId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}