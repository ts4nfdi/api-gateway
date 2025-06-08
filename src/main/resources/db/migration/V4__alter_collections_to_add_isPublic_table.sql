ALTER TABLE terminology_collection
    ALTER COLUMN description SET NOT NULL;

ALTER TABLE terminology_collection
    ALTER COLUMN description SET DATA TYPE TEXT;

ALTER TABLE terminology_collection
    ALTER COLUMN label SET NOT NULL;

ALTER TABLE terminology_collection
    ADD COLUMN is_public BOOLEAN DEFAULT FALSE NOT NULL;

