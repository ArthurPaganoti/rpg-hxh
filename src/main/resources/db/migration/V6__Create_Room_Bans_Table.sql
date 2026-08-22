CREATE TABLE room_bans (
    id BIGSERIAL PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES rooms(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    banned_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_room_bans UNIQUE (room_id, user_id)
);