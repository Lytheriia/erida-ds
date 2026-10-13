package lytheriia.bot.api.controllers;

import java.time.Instant;

public record SponsorStatusResponse(int rank, Instant startedAt) {
}