package lytheriia.bot.discord.listeners.presence;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ServerStatus(
        Integer players,
        Integer softMaxPlayers,
        String map,
        Integer roundId,
        String roundStartTime,
        Integer runLevel,
        Boolean panicBunker,
        Integer admins) {
}