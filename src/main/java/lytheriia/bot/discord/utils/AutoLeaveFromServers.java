package lytheriia.bot.discord.utils;

import org.springframework.stereotype.Component;

import lytheriia.bot.properties.DiscordProperties;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component

public class AutoLeaveFromServers extends ListenerAdapter {

    private final long allowedGuildId;

    public AutoLeaveFromServers(DiscordProperties props) {
        this.allowedGuildId = props.getGuildId();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        if (event.getGuild().getIdLong() != allowedGuildId) {
            event.getGuild().leave().queue();
        }
    }
}
