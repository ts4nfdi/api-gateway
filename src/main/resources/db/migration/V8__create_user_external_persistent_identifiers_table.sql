CREATE TABLE user_external_persistent_identifiers
(
    user_id                         BIGINT NOT NULL,
    external_persistent_identifiers VARCHAR(255)
);

ALTER TABLE user_external_persistent_identifiers
    ADD CONSTRAINT fk_user_external_persistent_identifiers_on_user FOREIGN KEY (user_id) REFERENCES users (id);