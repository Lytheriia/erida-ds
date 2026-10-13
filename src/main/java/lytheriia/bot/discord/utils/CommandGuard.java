package lytheriia.bot.discord.utils;

import java.util.List;

import org.springframework.stereotype.Component;

import lytheriia.bot.db.service.CommandPermissionService;
import lytheriia.bot.db.service.DepartmentService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Component
public class CommandGuard {

    private final CommandPermissionService permissions;
    private final DepartmentService department;

    public CommandGuard(
            CommandPermissionService permissions,
            DepartmentService department) {
        this.permissions = permissions;
        this.department = department;
    }

    public boolean isAllow(SlashCommandInteractionEvent event) {
        if (permissions.hasAccess(event.getMember(), event.getName())) {
            return true;
        }

        event.reply("У вас нет доступа к этой команде.")
                .setEphemeral(true)
                .queue();

        return false;
    }

    public boolean canManageMember(
            SlashCommandInteractionEvent event,
            Member target) {
        Member executor = event.getMember();

        if (executor == null || target == null) {
            return false;
        }

        if (executor.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        }

        if (!department.hasAccess(
                roleIds(executor),
                event.getName())) {
            return false;
        }

        return department.canManage(
                roleIds(executor),
                roleIds(target));
    }

    private static List<Long> roleIds(Member member) {
        return member.getRoles()
                .stream()
                .map(Role::getIdLong)
                .toList();
    }
}