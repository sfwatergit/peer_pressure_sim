SELECT
  t.boarding_stop,
  count(t.trip_id)
FROM matsim_trips_1kfirst t, mode_types m
WHERE t.mode = m.type_id
      AND m.type_id = 'subway'
GROUP BY t.boarding_stop;