CREATE TABLE department (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    guild_id BIGINT NOT NULL,
    name TEXT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE (role_id)
);

CREATE UNIQUE INDEX uq_department_name ON department (guild_id, lower(name));

CREATE TABLE department_control (
    controller_id BIGINT NOT NULL REFERENCES department (id) ON DELETE CASCADE,
    controlled_id BIGINT NOT NULL REFERENCES department (id) ON DELETE CASCADE,
    PRIMARY KEY (controller_id, controlled_id),
    CHECK (controller_id <> controlled_id)
);

CREATE TABLE department_role (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    department_id BIGINT NOT NULL REFERENCES department (id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL UNIQUE,
    level INTEGER NOT NULL CHECK (level >= 1),
    can_manage BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (department_id, level)
);

CREATE TABLE role_access (
    department_role_id BIGINT NOT NULL REFERENCES department_role (id) ON DELETE CASCADE,
    access TEXT NOT NULL,
    PRIMARY KEY (department_role_id, access)
);