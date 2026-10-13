package lytheriia.bot.discord.commands;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import lytheriia.bot.db.entity.department.DepartmentEntity;
import lytheriia.bot.db.entity.department.DepartmentRoleEntity;
import lytheriia.bot.db.service.DepartmentService;
import lytheriia.bot.discord.interfaces.IBaseCommand;
import lytheriia.bot.discord.utils.DepartamentCommandsCatalog;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

@Component
public class DepartmentCommand implements IBaseCommand {

    private static final Logger log = LoggerFactory.getLogger(DepartmentCommand.class);

    private static final Set<String> DEPARTMENT_OPTIONS = Set.of("department", "controller", "controlled");

    private final DepartmentService service;
    private final DepartamentCommandsCatalog catalog;

    public DepartmentCommand(DepartmentService service, DepartamentCommandsCatalog catalog) {
        this.service = service;
        this.catalog = catalog;
    }

    @Override
    public SlashCommandData data() {
        return Commands.slash("department", "Управление отделами")
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .addSubcommands(
                        new SubcommandData("create", "Создать отдел")
                                .addOption(OptionType.STRING, "name", "Название", true),
                        new SubcommandData("delete", "Удалить отдел")
                                .addOptions(dept("department", "Отдел")),
                        new SubcommandData("list", "Список отделов"))
                .addSubcommandGroups(
                        new SubcommandGroupData("role", "Роли отдела").addSubcommands(
                                new SubcommandData("add", "Привязать роль к отделу")
                                        .addOptions(dept("department", "Отдел"),
                                                role(),
                                                level(),
                                                new OptionData(OptionType.BOOLEAN, "can_manage",
                                                        "Может управлять (по умолчанию нет)", false)),
                                new SubcommandData("remove", "Отвязать роль")
                                        .addOptions(role()),
                                new SubcommandData("level", "Изменить уровень роли")
                                        .addOptions(role(), level()),
                                new SubcommandData("manage", "Изменить флаг управления")
                                        .addOptions(role(),
                                                new OptionData(OptionType.BOOLEAN, "value", "Новое значение", true)),
                                new SubcommandData("list", "Роли отдела")
                                        .addOptions(dept("department", "Отдел"))),
                        new SubcommandGroupData("access", "Права ролей").addSubcommands(
                                new SubcommandData("grant", "Выдать право для роли")
                                        .addOptions(role(), access()),
                                new SubcommandData("revoke", "Забрать право у роли")
                                        .addOptions(role(), access())),
                        new SubcommandGroupData("control", "Управление между отделами").addSubcommands(
                                new SubcommandData("add", "Управление одного над другим")
                                        .addOptions(dept("controller", "Кто управляет"),
                                                dept("controlled", "Кем управляют")),
                                new SubcommandData("remove", "Убрать связь")
                                        .addOptions(dept("controller", "Кто управляет"),
                                                dept("controlled", "Кем управляют")),
                                new SubcommandData("list", "Связи отдела")
                                        .addOptions(dept("department", "Отдел"))));
    }

    private static OptionData dept(String name, String description) {
        return new OptionData(OptionType.STRING, name, description, true, true); // autocomplete = true
    }

    private static OptionData role() {
        return new OptionData(OptionType.ROLE, "role", "Роль Discord", true);
    }

    private static OptionData level() {
        return new OptionData(OptionType.INTEGER, "level", "Уровень (>= 1)", true).setMinValue(1);
    }

    private static OptionData access() {
        return new OptionData(OptionType.STRING, "access", "Команда", true, true); // autocomplete = true
    }

    @Override
    public void autocomplete(CommandAutoCompleteInteractionEvent e) {
        if (e.getGuild() == null) {
            return;
        }
        String option = e.getFocusedOption().getName();
        String typed = e.getFocusedOption().getValue();

        if (option.equals("access")) {
            List<String> choices = e.getFullCommandName().equals("department access revoke")
                    ? currentAccess(e, typed)
                    : catalog.search(typed);
            e.replyChoiceStrings(choices).queue();
            return;
        }
        if (DEPARTMENT_OPTIONS.contains(option)) {
            String prefix = typed.toLowerCase();
            List<String> names = service.list(e.getGuild().getIdLong()).stream()
                    .map(DepartmentEntity::getName)
                    .filter(n -> n.toLowerCase().startsWith(prefix))
                    .limit(25) // лимит Discord
                    .toList();
            e.replyChoiceStrings(names).queue();
        }
    }

    private List<String> currentAccess(CommandAutoCompleteInteractionEvent e, String typed) {
        OptionMapping role = e.getOption("role");
        if (role == null) {
            return List.of();
        }
        String q = typed.toLowerCase();
        try {
            return service.accessOf(role.getAsRole().getIdLong()).stream()
                    .filter(a -> a.toLowerCase().contains(q))
                    .sorted()
                    .limit(25)
                    .toList();
        } catch (NoSuchElementException ex) {
            return List.of();
        }
    }

    @Override
    public void execute(SlashCommandInteractionEvent e) {
        if (e.getGuild() == null) {
            return;
        }
        handle(e, () -> switch (e.getFullCommandName()) {
            case "department create" -> create(e);
            case "department delete" -> delete(e);
            case "department list" -> list(e);

            case "department role add" -> roleAdd(e);
            case "department role remove" -> roleRemove(e);
            case "department role level" -> roleLevel(e);
            case "department role manage" -> roleManage(e);
            case "department role list" -> roleList(e);

            case "department access grant" -> accessGrant(e);
            case "department access revoke" -> accessRevoke(e);

            case "department control add" -> controlAdd(e);
            case "department control remove" -> controlRemove(e);
            case "department control list" -> controlList(e);

            default -> throw new IllegalStateException("Неизвестная команда");
        });
    }

    private void handle(SlashCommandInteractionEvent e, Supplier<String> action) {
        e.deferReply(true).queue();
        String message;
        try {
            message = action.get();
        } catch (IllegalStateException | IllegalArgumentException | NoSuchElementException ex) {
            message = "⚠️ " + ex.getMessage();
        } catch (DataIntegrityViolationException ex) {

            log.warn("Constraint violation", ex);
            message = "⚠️ Конфликт данных: такое значение уже существует или связано с другими записями";
        } catch (Exception ex) {
            log.error("Command {} failed", e.getFullCommandName(), ex);
            message = "❌ Внутренняя ошибка";
        }
        if (message.length() > 2000) { // лимит Discord
            message = message.substring(0, 1997) + "…";
        }
        e.getHook().editOriginal(message).queue();
    }

    private String create(SlashCommandInteractionEvent e) {
        String name = text(e, "name");
        Role role = e.getOption("role", OptionMapping::getAsRole);

        service.create(
                guild(e),
                name,
                role.getIdLong());
        return "✅ Отдел «" + name + "» создан";
    }

    private String delete(SlashCommandInteractionEvent e) {
        String name = text(e, "department");
        service.delete(guild(e), name);
        return "🗑️ Отдел «" + name + "» удалён вместе с ролями, правами и связями";
    }

    private String list(SlashCommandInteractionEvent e) {
        List<DepartmentEntity> all = service.list(guild(e));
        if (all.isEmpty()) {
            return "Отделов пока нет";
        }
        return "**Отделы:**\n" + all.stream()
                .map(d -> "• " + d.getName())
                .collect(Collectors.joining("\n"));
    }

    private String roleAdd(SlashCommandInteractionEvent e) {
        Role role = role(e);
        String dept = text(e, "department");
        int level = integer(e, "level");
        boolean canManage = e.getOption("can_manage", false, OptionMapping::getAsBoolean);
        service.addRole(guild(e), dept, role.getIdLong(), level, canManage);
        return "✅ Роль " + role.getAsMention() + " добавлена в «" + dept + "», уровень " + level
                + (canManage ? ", может управлять" : "");
    }

    private String roleRemove(SlashCommandInteractionEvent e) {
        Role role = role(e);
        service.removeRole(role.getIdLong());
        return "🗑️ Роль " + role.getAsMention() + " отвязана от отдела";
    }

    private String roleLevel(SlashCommandInteractionEvent e) {
        Role role = role(e);
        int level = integer(e, "level");
        service.setLevel(role.getIdLong(), level);
        return "✅ Уровень роли " + role.getAsMention() + " теперь " + level;
    }

    private String roleManage(SlashCommandInteractionEvent e) {
        Role role = role(e);
        boolean value = Objects.requireNonNull(e.getOption("value")).getAsBoolean();
        service.setCanManage(role.getIdLong(), value);
        return "✅ Роль " + role.getAsMention() + (value ? " теперь может управлять" : " больше не может управлять");
    }

    private String roleList(SlashCommandInteractionEvent e) {
        String dept = text(e, "department");
        List<DepartmentRoleEntity> roles = service.rolesOf(guild(e), dept);
        if (roles.isEmpty()) {
            return "В «" + dept + "» пока нет ролей";
        }
        return "**Роли «" + dept + "»:**\n" + roles.stream()
                .map(r -> {
                    String rights = r.getAccess().isEmpty() ? "нет" : String.join(", ", r.getAccess());
                    return "%d. <@&%d>%s — права: %s".formatted(
                            r.getLevel(), r.getRoleId(), r.isCanManage() ? " 🛡️" : "", rights);
                })
                .collect(Collectors.joining("\n"));
    }

    private String accessGrant(SlashCommandInteractionEvent e) {
        Role role = role(e);
        String access = text(e, "access");
        if (!catalog.contains(access)) {
            throw new IllegalArgumentException("Неизвестная команда: " + access);
        }
        return service.grant(role.getIdLong(), access)
                ? "✅ Роли " + role.getAsMention() + " выдано право `" + access + "`"
                : "ℹ️ У роли " + role.getAsMention() + " уже есть право `" + access + "`";
    }

    private String accessRevoke(SlashCommandInteractionEvent e) {
        Role role = role(e);
        String access = text(e, "access");
        return service.revoke(role.getIdLong(), access)
                ? "✅ У роли " + role.getAsMention() + " убрано право `" + access + "`"
                : "ℹ️ У роли " + role.getAsMention() + " нет права `" + access + "`";
    }

    // ---------- связи между отделами ----------

    private String controlAdd(SlashCommandInteractionEvent e) {
        String a = text(e, "controller");
        String b = text(e, "controlled");
        service.addControl(guild(e), a, b);
        return "✅ «" + a + "» теперь управляет «" + b + "»";
    }

    private String controlRemove(SlashCommandInteractionEvent e) {
        String a = text(e, "controller");
        String b = text(e, "controlled");
        service.removeControl(guild(e), a, b);
        return "🗑️ «" + a + "» больше не управляет «" + b + "»";
    }

    private String controlList(SlashCommandInteractionEvent e) {
        String dept = text(e, "department");
        long guild = guild(e);
        return "**«" + dept + "»**\n"
                + "Управляет: " + names(service.controlledBy(guild, dept)) + "\n"
                + "Управляется: " + names(service.controllersOf(guild, dept));
    }

    private static long guild(SlashCommandInteractionEvent e) {
        return Objects.requireNonNull(e.getGuild()).getIdLong();
    }

    private static String text(SlashCommandInteractionEvent e, String option) {
        String value = Objects.requireNonNull(e.getOption(option), option).getAsString().strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Значение «" + option + "» не может быть пустым");
        }
        return value;
    }

    private static int integer(SlashCommandInteractionEvent e, String option) {
        return Objects.requireNonNull(e.getOption(option), option).getAsInt();
    }

    private static Role role(SlashCommandInteractionEvent e) {
        return Objects.requireNonNull(e.getOption("role"), "role").getAsRole();
    }

    private static String names(List<DepartmentEntity> departments) {
        return departments.isEmpty()
                ? "—"
                : departments.stream().map(DepartmentEntity::getName).collect(Collectors.joining(", "));
    }
}