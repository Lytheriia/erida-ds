package lytheriia.bot.discord.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component

public class AutoLeaveFromServers extends ListenerAdapter {

    private final long allowedGuildId;

    public AutoLeaveFromServers(@Value("${discord.guild-id}") long allowedGuildId) {
        this.allowedGuildId = allowedGuildId;
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        if (event.getGuild().getIdLong() != allowedGuildId) {
            event.getGuild().leave().queue();
        }
    }
}
