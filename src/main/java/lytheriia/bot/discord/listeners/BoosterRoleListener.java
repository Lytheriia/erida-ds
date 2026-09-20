package lytheriia.bot.discord.listeners;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lytheriia.bot.db.service.DiscordAccountInfoService;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class BoosterRoleListener extends ListenerAdapter {

    @Value("${discord.roles.booster}")
    private long boosterRoleId;

    private final DiscordAccountInfoService accountService;

    public BoosterRoleListener(DiscordAccountInfoService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        boolean boosterRoleAdded = event.getRoles().stream()
                .anyMatch(role -> role.getIdLong() == boosterRoleId);

        if (!boosterRoleAdded) {
            return;
        }

        accountService.setBooster(event.getUser().getIdLong(), true);
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        boolean boosterRole = event.getRoles().stream().anyMatch(role -> role.getIdLong() == boosterRoleId);

        if (!boosterRole) {
            return;
        }

        accountService.setBooster(event.getUser().getIdLong(), false);
    }

}
