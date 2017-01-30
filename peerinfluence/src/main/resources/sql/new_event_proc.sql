CREATE TABLE matsim_activities_1klast
(
  activity_id     SERIAL PRIMARY KEY NOT NULL,
  person_id       INT                NOT NULL,
  facility_id     INT,
  type            VARCHAR(10)        NOT NULL,
  start_time      INT                NOT NULL,
  end_time        INT                NOT NULL,
  sample_selector DOUBLE PRECISION   NOT NULL
);
CREATE TABLE matsim_journeys_1klast
(
  journey_id             INT PRIMARY KEY  NOT NULL,
  person_id              INT              NOT NULL,
  start_time             INT              NOT NULL,
  end_time               INT              NOT NULL,
  distance               INT              NOT NULL,
  main_mode              VARCHAR(30)      NOT NULL,
  to_act                 INT              NOT NULL,
  from_act               INT              NOT NULL,
  in_vehicle_distance    INT              NOT NULL,
  in_vehicle_time        INT              NOT NULL,
  access_walk_distance   INT              NOT NULL,
  access_walk_time       INT              NOT NULL,
  access_wait_time       INT              NOT NULL,
  first_boarding_stop    VARCHAR(128),
  egress_walk_distance   INT              NOT NULL,
  egress_walk_time       INT              NOT NULL,
  last_alighting_stop    VARCHAR(128),
  transfer_walk_distance INT              NOT NULL,
  transfer_walk_time     INT              NOT NULL,
  transfer_wait_time     INT              NOT NULL,
  sample_selector        DOUBLE PRECISION NOT NULL
);
CREATE TABLE matsim_transfers_1klast
(
  transfer_id     SERIAL PRIMARY KEY NOT NULL,
  journey_id      INT                NOT NULL,
  start_time      INT                NOT NULL,
  end_time        INT                NOT NULL,
  from_trip       INT                NOT NULL,
  to_trip         INT                NOT NULL,
  walk_distance   INT                NOT NULL,
  walk_time       INT                NOT NULL,
  wait_time       INT                NOT NULL,
  sample_selector DOUBLE PRECISION   NOT NULL
);
CREATE TABLE matsim_trips_1klast
(
  trip_id         SERIAL PRIMARY KEY NOT NULL,
  journey_id      INT                NOT NULL,
  start_time      INT                NOT NULL,
  end_time        INT                NOT NULL,
  distance        INT,
  mode            VARCHAR(128)       NOT NULL,
  line            INT,
  route           INT,
  boarding_stop   VARCHAR(128),
  alighting_stop  VARCHAR(128),
  sample_selector DOUBLE PRECISION   NOT NULL
);
CREATE TABLE mode_types
(
  type_id CHAR(30) PRIMARY KEY NOT NULL
);
CREATE TABLE matsim_activities_1klast_activity_id_seq
(
  sequence_name VARCHAR NOT NULL,
  last_value    BIGINT  NOT NULL,
  start_value   BIGINT  NOT NULL,
  increment_by  BIGINT  NOT NULL,
  max_value     BIGINT  NOT NULL,
  min_value     BIGINT  NOT NULL,
  cache_value   BIGINT  NOT NULL,
  log_cnt       BIGINT  NOT NULL,
  is_cycled     BOOL    NOT NULL,
  is_called     BOOL    NOT NULL
);
CREATE TABLE matsim_transfers_1klast_transfer_id_seq
(
  sequence_name VARCHAR NOT NULL,
  last_value    BIGINT  NOT NULL,
  start_value   BIGINT  NOT NULL,
  increment_by  BIGINT  NOT NULL,
  max_value     BIGINT  NOT NULL,
  min_value     BIGINT  NOT NULL,
  cache_value   BIGINT  NOT NULL,
  log_cnt       BIGINT  NOT NULL,
  is_cycled     BOOL    NOT NULL,
  is_called     BOOL    NOT NULL
);
CREATE TABLE matsim_trips_1klast_trip_id_seq
(
  sequence_name VARCHAR NOT NULL,
  last_value    BIGINT  NOT NULL,
  start_value   BIGINT  NOT NULL,
  increment_by  BIGINT  NOT NULL,
  max_value     BIGINT  NOT NULL,
  min_value     BIGINT  NOT NULL,
  cache_value   BIGINT  NOT NULL,
  log_cnt       BIGINT  NOT NULL,
  is_cycled     BOOL    NOT NULL,
  is_called     BOOL    NOT NULL
);
CREATE UNIQUE INDEX unique_activity_id ON matsim_activities_1klast (activity_id);
ALTER TABLE matsim_journeys_1klast ADD FOREIGN KEY (from_act) REFERENCES matsim_activities_1klast (activity_id);
ALTER TABLE matsim_journeys_1klast ADD FOREIGN KEY (to_act) REFERENCES matsim_activities_1klast (activity_id);
ALTER TABLE matsim_journeys_1klast ADD FOREIGN KEY (main_mode) REFERENCES mode_types (type_id) ON UPDATE CASCADE;
CREATE UNIQUE INDEX unique_journey_id ON matsim_journeys_1klast (journey_id);
ALTER TABLE matsim_transfers_1klast ADD FOREIGN KEY (journey_id) REFERENCES matsim_journeys_1klast (journey_id);
ALTER TABLE matsim_transfers_1klast ADD FOREIGN KEY (from_trip) REFERENCES matsim_trips_1klast (trip_id);
ALTER TABLE matsim_transfers_1klast ADD FOREIGN KEY (to_trip) REFERENCES matsim_trips_1klast (trip_id);
CREATE UNIQUE INDEX unique_transfer_id ON matsim_transfers_1klast (transfer_id);
ALTER TABLE matsim_trips_1klast ADD FOREIGN KEY (journey_id) REFERENCES matsim_journeys_1klast (journey_id);
ALTER TABLE matsim_trips_1klast ADD FOREIGN KEY (mode) REFERENCES mode_types (type_id);
CREATE UNIQUE INDEX unique_trip_id ON matsim_trips_1klast (trip_id);
