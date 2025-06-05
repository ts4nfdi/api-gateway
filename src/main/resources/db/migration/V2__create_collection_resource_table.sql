-- Create the Resource table, linked to TerminologyCollection
CREATE TABLE collection_resource
(
    id          UUID      NOT NULL,
    collection_id UUID    NOT NULL,
    uri         VARCHAR(255),
    label       VARCHAR(255),
    source      VARCHAR(255),
    type        VARCHAR(50),
    CONSTRAINT pk_resource PRIMARY KEY (id)
);

ALTER TABLE collection_resource
    ADD CONSTRAINT fk_resource_on_terminologycollection FOREIGN KEY (collection_id) REFERENCES terminology_collection (id);


-- Drop legacy tables no longer used (terminology_collection_terminologies and user_roles)
DROP TABLE IF EXISTS terminology_collection_terminologies;