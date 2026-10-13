package lytheriia.bot.api.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lytheriia.bot.db.service.BoostySubscriberService;

@RestController
@RequestMapping("/api/boosty")
public class BoostyApiController {

    private final BoostySubscriberService boostyService;

    public BoostyApiController(BoostySubscriberService boostyService) {
        this.boostyService = boostyService;
    }

    @GetMapping("/{user}")
    public ResponseEntity<SponsorStatusResponse> getStatus(@PathVariable("user") UUID user) {
        return boostyService.findActiveByUser(user)
                .map(s -> ResponseEntity.ok(new SponsorStatusResponse(s.getRank(), s.getStartedAt())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}