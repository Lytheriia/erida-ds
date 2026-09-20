CREATE TABLE discord_account_info (
    discord_id BIGINT PRIMARY KEY,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    booster BOOLEAN NOT NULL DEFAULT FALSE
);

INSERT INTO
    discord_account_info (discord_id)
SELECT
    discord_id
FROM
    discord_linked_accounts;

CREATE TABLE command_permissions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    command_name VARCHAR(32) NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT uq_command_role UNIQUE (command_name, role_id)
);