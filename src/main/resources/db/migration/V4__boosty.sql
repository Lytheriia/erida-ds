CREATE TABLE active_boosty_subscriber (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    discord_id BIGINT NOT NULL,
    promoted_by_id BIGINT,
    rank INT NOT NULL,
    started_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE expired_boosty_subscriber (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ss14_id UUID NOT NULL,
    discord_id BIGINT,
    rank BIGINT NOT NULL,
    promoted_by_id BIGINT,
    expire_at TIMESTAMPTZ,
    started_at TIMESTAMPTZ
);

ALTER TABLE
    active_boosty_subscriber
ADD
    CONSTRAINT uq_active_boosty_discord_rank UNIQUE (discord_id, rank);