ALTER TABLE users
    ADD COLUMN oidc_subject_identifier VARCHAR(255) UNIQUE;