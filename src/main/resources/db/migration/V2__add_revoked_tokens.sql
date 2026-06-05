CREATE TABLE revoked_tokens (
    token_hash  VARCHAR(64)  PRIMARY KEY,
    subject     VARCHAR(150) NOT NULL,
    expires_at   TIMESTAMP    NOT NULL,
    revoked_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_revoked_tokens_expires_at ON revoked_tokens (expires_at);
