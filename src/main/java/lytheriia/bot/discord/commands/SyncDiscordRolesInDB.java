package lytheriia.bot.discord.commands;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lytheriia.bot.db.service.DiscordAccountInfoService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Component
public class SyncDiscordRolesInDB extends ListenerAdapter {

    public static final List<String> MANAGED = List.of(
            "verified",
            "boosty");

    @Value("${discord.developer-id}")
    private Long developerId;

    @Value("${discord.roles.verified}")
    private long verifiedRoleId;

    @Value("${discord.roles.booster}")
    private long boosterRoleId;

    private final DiscordAccountInfoService accountService;

    public SyncDiscordRolesInDB(DiscordAccountInfoService accountService) {
        this.accountService = accountService;
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

        if (!event.getName().equals("syncdbroles")) {
            return;
        }

        if (event.getMember() == null
                || !developerId.equals(event.getMember().getIdLong())) {

            event.reply("Данная команда доступна только главному разработчику.")
                    .setEphemeral(true)
                    .queue();

            return;
        }

        Guild guild = event.getGuild();

        if (guild == null) {
            event.reply("Команда может использоваться только на сервере.")
                    .setEphemeral(true)
                    .queue();

            return;
        }

        String roleName = event.getOption(
                "roles",
                OptionMapping::getAsString);

        if (roleName == null) {
            event.reply("Необходимо указать роль.")
                    .setEphemeral(true)
                    .queue();

            return;
        }

        long roleId;

        switch (roleName) {
            case "verified" -> roleId = verifiedRoleId;
            case "boosty" -> roleId = boosterRoleId;

            default -> {
                event.reply("Неизвестная роль: `" + roleName + "`.")
                        .setEphemeral(true)
                        .queue();

                return;
            }
        }

        Role role = guild.getRoleById(roleId);

        if (role == null) {
            event.reply(
                    "Роль `" + roleName + "` не найдена на сервере.").setEphemeral(true).queue();

            return;
        }

        event.deferReply(true).queue();

        List<Member> members = guild.getMembersWithRoles(role);

        int updated = 0;

        for (Member member : members) {

            switch (roleName) {

                case "verified" -> {
                    accountService.setVerified(
                            member.getIdLong(),
                            true);
                }

                case "boosty" -> {
                    accountService.setBooster(
                            member.getIdLong(),
                            true);
                }
            }

            updated++;
        }

        event.getHook()
                .editOriginal(
                        "Синхронизация роли " + role.getAsMention()
                                + " завершена.\n"
                                + "Обновлено аккаунтов: **"
                                + updated
                                + "**.")
                .queue();
    }
}