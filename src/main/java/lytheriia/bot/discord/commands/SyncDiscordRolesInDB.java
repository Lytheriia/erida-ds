package lytheriia.bot.discord.commands;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import lytheriia.bot.db.repository.DiscordLinkRepository;
import lytheriia.bot.db.service.DiscordAccountInfoService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.ErrorResponse;

@Component
public class SyncDiscordRolesInDB extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SyncDiscordRolesInDB.class);

    public static final List<String> MANAGED = List.of("verified", "boosty");

    private final long developerId;
    private final long verifiedRoleId;
    private final long boosterRoleId;
    private final DiscordAccountInfoService accountService;
    private final DiscordLinkRepository links;
    private final TaskExecutor executor;

    public SyncDiscordRolesInDB(
            @Value("${discord.developer-id}") long developerId,
            @Value("${discord.roles.verified}") long verifiedRoleId,
            @Value("${discord.roles.booster}") long boosterRoleId,
            DiscordAccountInfoService accountService,
            DiscordLinkRepository links,
            @Qualifier("applicationTaskExecutor") TaskExecutor executor) {

        this.developerId = developerId;
        this.verifiedRoleId = verifiedRoleId;
        this.boosterRoleId = boosterRoleId;
        this.accountService = accountService;
        this.links = links;
        this.executor = executor;
    }

    public static OptionData rolesOption() {
        OptionData option = new OptionData(OptionType.STRING, "roles", "Роль для синхронизации", true);
        MANAGED.forEach(r -> option.addChoice(r, r));
        return option;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("syncdbroles"))
            return;

        if (event.getUser().getIdLong() != developerId) {
            event.reply("Данная команда доступна только главному разработчику.").setEphemeral(true).queue();
            return;
        }

        Guild guild = event.getGuild();
        String roleName = event.getOption("roles", OptionMapping::getAsString);
        if (guild == null || roleName == null) {
            event.reply("Команда работает только на сервере и требует указать роль.").setEphemeral(true).queue();
            return;
        }

        long roleId = roleName.equals("verified") ? verifiedRoleId : boosterRoleId;
        Role role = guild.getRoleById(roleId);
        if (role == null) {
            event.reply("Роль `" + roleName + "` не найдена на сервере.").setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue();
        executor.execute(() -> sync(event, guild, role, roleName));
    }

    private void sync(SlashCommandInteractionEvent event, Guild guild, Role role, String roleName) {
        int with = 0, without = 0, left = 0;

        try {
            for (var link : links.findAll()) {
                long id = link.getDiscordId();
                try {
                    Member member = guild.retrieveMemberById(id).complete();
                    boolean has = member.getRoles().contains(role);

                    if (roleName.equals("verified"))
                        accountService.setVerified(id, has);
                    else
                        accountService.setBooster(id, has);

                    if (has)
                        with++;
                    else
                        without++;
                } catch (ErrorResponseException e) {
                    if (e.getErrorResponse() == ErrorResponse.UNKNOWN_MEMBER)
                        left++;
                    else
                        throw e;
                }
            }

            event.getHook().editOriginal("Синхронизация роли " + role.getAsMention() + " завершена.\n"
                    + "С ролью: **" + with + "**, без роли: **" + without + "**, вышли с сервера: **" + left + "**.")
                    .queue();
        } catch (Exception e) {
            log.error("syncdbroles failed", e);
            event.getHook().editOriginal("Синхронизация прервана ошибкой: " + e.getClass().getSimpleName()).queue();
        }
    }
}