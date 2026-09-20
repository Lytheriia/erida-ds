package lytheriia.bot.discord;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lytheriia.bot.db.service.CommandPermissionService;
import lytheriia.bot.discord.commands.SyncDiscordRolesInDB;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

@Configuration
public class BotMain {

        private static final Logger log = LoggerFactory.getLogger(BotMain.class);

        private static OptionData commandsOption() {
                OptionData option = new OptionData(OptionType.STRING, "command", "Команда", true);
                CommandPermissionService.MANAGED.forEach(c -> option.addChoice(c, c));
                return option;
        }

        @Bean(destroyMethod = "shutdown")
        public JDA jda(@Value("${discord.token}") String token, @Value("${discord.guild-id}") long guildId,
                        List<ListenerAdapter> listeners) throws InterruptedException {

                JDA jda = JDABuilder.createLight(token)
                                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                                .setActivity(Activity.customStatus("Запуск..."))
                                .addEventListeners(listeners.toArray())
                                .build()
                                .awaitReady();

                jda.getGuilds().stream()
                                .filter(g -> g.getIdLong() != guildId)
                                .forEach(g -> g.leave().queue());

                Guild guild = jda.getGuildById(guildId);
                if (guild == null) {
                        throw new IllegalStateException("Main guild not found : " + guildId);
                }

                jda.updateCommands().queue();

                jda.updateCommands().addCommands(
                                Commands.slash("stats", "Статистика раундов")
                                                .setDefaultPermissions(DefaultMemberPermissions.ENABLED),

                                Commands.slash("link-setup", "Отправить сообщение с кнопкой привязки")
                                                .setContexts(InteractionContextType.GUILD)
                                                .setDefaultPermissions(DefaultMemberPermissions
                                                                .enabledFor(Permission.ADMINISTRATOR))
                                                .addOptions(new OptionData(OptionType.CHANNEL, "channel",
                                                                "Канал для сообщения", true)
                                                                .setChannelTypes(ChannelType.TEXT)),

                                Commands.slash("addpermission", "Разрешить роли использовать команду")
                                                .setDefaultPermissions(DefaultMemberPermissions
                                                                .enabledFor(Permission.ADMINISTRATOR))
                                                .addOptions(new OptionData(OptionType.ROLE, "role", "Роль", true),
                                                                commandsOption()),

                                Commands.slash("removepermission", "Убрать доступ роли к команде")
                                                .setDefaultPermissions(DefaultMemberPermissions
                                                                .enabledFor(Permission.ADMINISTRATOR))
                                                .addOptions(new OptionData(OptionType.ROLE, "role", "Роль", true),
                                                                commandsOption()),

                                Commands.slash("listpermissions", "Кто может использовать команду")
                                                .setDefaultPermissions(DefaultMemberPermissions
                                                                .enabledFor(Permission.ADMINISTRATOR))
                                                .addOptions(commandsOption()),
                                Commands.slash("syncdbroles", "Синхронизация дискорд сервера с базой данных")
                                                .setDefaultPermissions(DefaultMemberPermissions
                                                                .enabledFor(Permission.ADMINISTRATOR))
                                                .addOptions(SyncDiscordRolesInDB.rolesOption()))
                                .queue(
                                                ok -> log.info("Registered {} guild commands", ok.size()),
                                                err -> log.error("Failed to register commands", err));

                return jda;
        }
}