
ALTER TABLE EGW_ABSTRACTESTIMATE ALTER COLUMN longitude TYPE double precision;
ALTER TABLE EGW_ABSTRACTESTIMATE ALTER COLUMN latitude TYPE double precision;

--rollback ALTER TABLE EGW_ABSTRACTESTIMATE ALTER COLUMN longitude TYPE bigint;
--rollback ALTER TABLE EGW_ABSTRACTESTIMATE ALTER COLUMN latitude TYPE bigint;