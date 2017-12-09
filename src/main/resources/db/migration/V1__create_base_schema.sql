CREATE TABLE aggregates(
    id uuid NOT NULL,
    aggregate_type character varying(255) NOT NULL,
    version bigint NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE events(
    aggregate_id uuid NOT NULL,
    version bigint NOT NULL,
    data jsonb NOT NULL,
    date_event timestamp NOT NULL DEFAULT now() NOT NULL,
    PRIMARY KEY (aggregate_id, version),
    FOREIGN KEY (aggregate_id) REFERENCES aggregates(id)
);

CREATE TABLE snapshots(
    aggregate_id uuid NOT NULL,
    data jsonb NOT NULL,
    version bigint
);