DROP TABLE IF EXISTS endpoint_hits;

CREATE TABLE IF NOT EXISTS endpoint_hits
(
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    app         VARCHAR(255)                            NOT NULL,
    uri         VARCHAR(255)                            NOT NULL,
    ip          VARCHAR(255)                            NOT NULL,
    create_dttm timestamp                               NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);