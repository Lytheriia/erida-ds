package lytheriia.bot.db.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "command_permissions")
public class CommandPermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String commandName;
    private long roleId;

    protected CommandPermissionEntity() {
    }

    public CommandPermissionEntity(String commandName, long roleId) {
        this.commandName = commandName;
        this.roleId = roleId;
    }

    public String getCommandName() {
        return commandName;
    }

    public long getRoleId() {
        return roleId;
    }
}