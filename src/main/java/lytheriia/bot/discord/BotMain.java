package lytheriia.bot.discord;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Configuration
public class BotMain {

    @Bean(destroyMethod = "shutdown")
    public JDA jda(@Value("${discord.token}") String token,
            List<ListenerAdapter> listeners) throws InterruptedException {

        JDA jda = JDABuilder.createLight(token)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.customStatus("Запуск..."))
                .addEventListeners(listeners.toArray())
                .build()
                .awaitReady();

        // jda.updateCommands().addCommands(
        //         Commands.slash("stats", "Статистика раундов")
        //                 .addOptions(new OptionData(OptionType.INTEGER, "days", "За сколько дней", false)
        //                         .setMinValue(1)
        //                         .setMaxValue(90)))
        //         .queue();

        return jda;
    }
}