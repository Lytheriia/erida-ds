package lytheriia.bot.properties;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "discord")
public class DiscordProperties {
    private String token;
    private Long guildId;
    private Roles roles = new Roles();
    private Long developerId;

    // getters/setters для token, guildId, developerId как раньше

    public Roles getRoles() {
        return roles;
    }

    public void setRoles(Roles roles) {
        this.roles = roles;
    }

    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(Long developerId) {
        this.developerId = developerId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static class Roles {
        private Long verified;
        private Long booster;
        private Map<Integer, Long> boosty = new HashMap<>();

        public Long getVerified() {
            return verified;
        }

        public void setVerified(Long verified) {
            this.verified = verified;
        }

        public Long getBooster() {
            return booster;
        }

        public void setBooster(Long booster) {
            this.booster = booster;
        }

        public Map<Integer, Long> getBoosty() {
            return boosty;
        }

        public void setBoosty(Map<Integer, Long> boosty) {
            this.boosty = boosty;
        }
    }
}