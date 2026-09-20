package lytheriia.bot.api.controllers;

import java.util.Map;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lytheriia.bot.db.repository.DiscordLinkRepository;
import lytheriia.bot.db.service.DiscordLinkingService;

// TODO add force updating function
// RegisterHandler(HttpMethod.Post, "/linking/update", RequestUpdateDiscordLinkingData);

@RestController
@RequestMapping("/api/link")
public class DiscordLinkingController {

    public record RegisterCodeRequest(
            @NotNull @Pattern(regexp = "^[A-Za-z0-9-]{8,36}$") String code,
            @NotNull UUID userId) {
    }

    public record LinkStatus(boolean linked, String discordId) {
    }

    private final DiscordLinkingService codes;
    private final DiscordLinkRepository links;

    public DiscordLinkingController(DiscordLinkingService codes, DiscordLinkRepository links) {
        this.codes = codes;
        this.links = links;
    }

    @PostMapping("/codes")
    public ResponseEntity<?> registerCode(@Valid @RequestBody RegisterCodeRequest req) {
        if (links.existsBySs14UserId(req.userId())) {
            return error(HttpStatus.CONFLICT, "ALREADY_LINKED");
        }
        try {
            codes.register(req.code(), req.userId());
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return error(HttpStatus.CONFLICT, "CODE_COLLISION");
        } catch (DataAccessException e) {
            return error(HttpStatus.SERVICE_UNAVAILABLE, "STORAGE_UNAVAILABLE");
        }
    }

    @GetMapping("/{userId}")
    public LinkStatus status(@PathVariable UUID userId) {
        return links.findBySs14UserId(userId)
                .map(l -> new LinkStatus(true, String.valueOf(l.getDiscordId())))
                .orElse(new LinkStatus(false, null));
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String code) {
        return ResponseEntity.status(status).body(Map.of("error", code));
    }
}