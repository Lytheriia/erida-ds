package lytheriia.bot.discord.commands;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import lytheriia.bot.db.entity.DiscordLinkedAccountEntity;
import lytheriia.bot.db.repository.DiscordLinkRepository;
import lytheriia.bot.db.service.DiscordAccountInfoService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.utils.FileUpload;

@Component
public class SyncDiscordRolesInDB extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SyncDiscordRolesInDB.class);

    public static final String COMMAND = "syncdbroles";

    public static final List<String> MANAGED = List.of(
            "verified",
            "boosty");

    private final long developerId;
    private final long verifiedRoleId;
    private final long boosterRoleId;

    private final DiscordAccountInfoService accountService;
    private final DiscordLinkRepository links;
    private final TaskExecutor executor;

    private final AtomicBoolean running = new AtomicBoolean(false);

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
        OptionData option = new OptionData(
                OptionType.STRING,
                "roles",
                "Роль для синхронизации",
                true);

        MANAGED.forEach(role -> option.addChoice(role, role));

        return option;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals(COMMAND))
            return;

        event.deferReply(true).queue();

        if (event.getUser().getIdLong() != developerId) {
            event.getHook().sendMessage("Данная команда доступна только главному разработчику.").queue();
            return;
        }

        Guild guild = event.getGuild();
        String roleName = event.getOption("roles", OptionMapping::getAsString);

        if (guild == null) {
            event.getHook().sendMessage("Команда может использоваться только на сервере.").queue();
            return;
        }

        if (roleName == null || !MANAGED.contains(roleName)) {
            event.getHook().sendMessage("Неизвестная роль: `" + roleName + "`.").queue();
            return;
        }

        long roleId = roleName.equals("verified") ? verifiedRoleId : boosterRoleId;
        Role role = guild.getRoleById(roleId);

        if (role == null) {
            event.getHook().sendMessage("Роль `" + roleName + "` не найдена на сервере.").queue();
            return;
        }

        if (!running.compareAndSet(false, true)) {
            event.getHook().sendMessage("Синхронизация уже выполняется, подождите завершения.").queue();
            return;
        }

        executor.execute(() -> {
            try {
                sync(event, guild, role, roleName);
            } finally {
                running.set(false);
            }
        });
    }

    private void sync(SlashCommandInteractionEvent event, Guild guild, Role role, String roleName) {
        try {
            List<Member> withRole = guild.findMembersWithRoles(role).get();
            Set<Long> withRoleIds = withRole.stream()
                    .map(Member::getIdLong)
                    .collect(Collectors.toSet());

            Set<Long> linkedIds = links.findAll().stream()
                    .map(DiscordLinkedAccountEntity::getDiscordId)
                    .collect(Collectors.toSet());

            int granted = 0;
            int revoked = 0;
            List<Long> notLinked = new ArrayList<>();

            for (Member member : withRole) {
                long id = member.getIdLong();

                if (linkedIds.contains(id)) {
                    apply(roleName, id, true);
                    granted++;
                } else {
                    notLinked.add(id);
                }
            }

            for (long id : linkedIds) {
                if (!withRoleIds.contains(id)) {
                    apply(roleName, id, false);
                    revoked++;
                }
            }

            String summary = "Синхронизация роли " + role.getAsMention() + " завершена.\n"
                    + "Всего с ролью: **" + withRole.size() + "**\n"
                    + "Привязаны, флаг включён: **" + granted + "**\n"
                    + "Привязаны, роли нет, флаг снят: **" + revoked + "**\n"
                    + "Роль есть, аккаунт не привязан: **" + notLinked.size() + "**";

            var action = event.getHook().editOriginal(summary);

            if (!notLinked.isEmpty()) {
                String list = notLinked.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining("\n"));

                action = action.setFiles(
                        FileUpload.fromData(list.getBytes(StandardCharsets.UTF_8), "not-linked.txt"));
            }

            action.queue();

            log.info("syncdbroles {}: withRole={}, granted={}, revoked={}, notLinked={}",
                    roleName, withRole.size(), granted, revoked, notLinked.size());

        } catch (Exception e) {
            log.error("syncdbroles failed", e);
            event.getHook()
                    .editOriginal("Синхронизация прервана ошибкой: " + e.getClass().getSimpleName())
                    .queue();
        }
    }

    private void apply(String roleName, long discordId, boolean value) {
        switch (roleName) {
            case "verified" -> accountService.setVerified(discordId, value);
            case "boosty" -> accountService.setBooster(discordId, value);
            default -> throw new IllegalArgumentException("Unknown role: " + roleName);
        }
    }
}