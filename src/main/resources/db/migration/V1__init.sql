CREATE TABLE IF NOT EXISTS round_snapshots (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    time_stamp  TIMESTAMPTZ NOT NULL DEFAULT now(),
    round_id    INTEGER     NOT NULL,
    players     INTEGER     NOT NULL
);

CREATE TABLE discord_linked_accounts (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    discord_id    BIGINT      NOT NULL UNIQUE,
    ss14_user_id  UUID        NOT NULL UNIQUE,
    linked_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_snapshots_ts       ON round_snapshots (time_stamp);
CREATE INDEX IF NOT EXISTS idx_snapshots_round_id ON round_snapshots (round_id);