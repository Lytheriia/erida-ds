package lytheriia.bot.discord.commands;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import lytheriia.bot.db.entity.department.DepartmentEntity;
import lytheriia.bot.db.service.DepartmentService;
import lytheriia.bot.discord.interfaces.IBaseDepartamentCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

@Component
public class DepartmentPersonnelCommand implements IBaseDepartamentCommand {

        private final DepartmentService service;

        public DepartmentPersonnelCommand(DepartmentService service) {
                this.service = service;
        }

        @Override
        public SlashCommandData data() {
                return Commands.slash(
                                "department-personnel",
                                "Управление сотрудниками отдела")
                                .setContexts(InteractionContextType.GUILD)
                                .setDefaultPermissions(
                                                DefaultMemberPermissions.ENABLED)
                                .addSubcommands(
                                                new SubcommandData("promote", "Повысить сотрудника")
                                                                .addOption(
                                                                                OptionType.USER,
                                                                                "player",
                                                                                "Сотрудник",
                                                                                true)
                                                                .addOption(
                                                                                OptionType.STRING,
                                                                                "department",
                                                                                "Отдел",
                                                                                true,
                                                                                true),

                                                new SubcommandData("demote", "Понизить сотрудника")
                                                                .addOption(
                                                                                OptionType.USER,
                                                                                "player",
                                                                                "Сотрудник",
                                                                                true)
                                                                .addOption(
                                                                                OptionType.STRING,
                                                                                "department",
                                                                                "Отдел",
                                                                                true,
                                                                                true),

                                                new SubcommandData("join", "Принять сотрудника в отдел")
                                                                .addOption(
                                                                                OptionType.USER,
                                                                                "player",
                                                                                "Сотрудник",
                                                                                true)
                                                                .addOption(
                                                                                OptionType.STRING,
                                                                                "department",
                                                                                "отдел",
                                                                                true,
                                                                                true),

                                                new SubcommandData("leave", "Удалить сотрудника из отдела")
                                                                .addOption(
                                                                                OptionType.USER,
                                                                                "player",
                                                                                "Сотрудник",
                                                                                true)
                                                                .addOption(
                                                                                OptionType.STRING,
                                                                                "department",
                                                                                "Отдел",
                                                                                true,
                                                                                true),

                                                new SubcommandData("vacation", "Отправить сотрудника в отпуск")
                                                                .addOption(
                                                                                OptionType.USER,
                                                                                "player",
                                                                                "Сотрудник",
                                                                                true)
                                                                .addOption(
                                                                                OptionType.STRING,
                                                                                "department",
                                                                                "отдел",
                                                                                true,
                                                                                true));
        }

        @Override
        public void autocomplete(CommandAutoCompleteInteractionEvent e) {
                if (!e.getFocusedOption().getName().equals("department")) {
                        return;
                }

                String typed = e.getFocusedOption()
                                .getValue()
                                .toLowerCase();

                List<String> names = service.list(
                                Objects.requireNonNull(e.getGuild()).getIdLong()).stream()
                                .map(DepartmentEntity::getName)
                                .filter(name -> name.toLowerCase().startsWith(typed))
                                .limit(25)
                                .toList();

                e.replyChoiceStrings(names).queue();
        }

        @Override
        public void execute(SlashCommandInteractionEvent e) {
                Member player = Objects.requireNonNull(
                                e.getOption("player")).getAsMember();

                String department = Objects.requireNonNull(
                                e.getOption("department")).getAsString();

                if (player == null) {
                        e.reply("❌ Игрок не найден.")
                                        .setEphemeral(true)
                                        .queue();
                        return;
                }

                switch (e.getSubcommandName()) {
                        case "promote" -> promote(e, player, department);
                        case "demote" -> demote(e, player, department);
                        case "join" -> join(e, player, department);
                        case "leave" -> leave(e, player, department);
                        case "vacation" -> vacation(e, player, department);
                        default -> e.reply("❌ Неизвестная команда.")
                                        .setEphemeral(true)
                                        .queue();
                }
        }

        private void promote(
                        SlashCommandInteractionEvent e,
                        Member player,
                        String department) {
                // TODO
        }

        private void demote(
                        SlashCommandInteractionEvent e,
                        Member player,
                        String department) {
                // TODO
        }

        private void join(
                        SlashCommandInteractionEvent e,
                        Member player,
                        String department) {
                // TODO
        }

        private void leave(
                        SlashCommandInteractionEvent e,
                        Member player,
                        String department) {
                // TODO
        }

        private void vacation(
                        SlashCommandInteractionEvent e,
                        Member player,
                        String department) {
                // TODO
        }
}