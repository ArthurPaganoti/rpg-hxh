CREATE TABLE rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    master_id BIGINT NOT NULL REFERENCES users(id),
    is_private BOOLEAN NOT NULL DEFAULT false,
    invite_code VARCHAR(6) UNIQUE,
    current_players INTEGER NOT NULL DEFAULT 1,
    max_players INTEGER NOT NULL DEFAULT 10,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);