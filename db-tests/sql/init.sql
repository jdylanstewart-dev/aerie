-- The order of inclusion is important! Tables referenced by foreign keys must be loaded before their dependants.

begin;

  -- Tables.
  -- Uploaded files (JARs or simulation input files).
  \ir ../../merlin-server/sql/tables/uploaded_file.sql

  -- Planning intents.
  \ir ../../merlin-server/sql/tables/mission_model.sql
  \ir ../../merlin-server/sql/tables/activity_type.sql
  \ir ../../merlin-server/sql/tables/plan.sql
  \ir ../../merlin-server/sql/tables/activity.sql
  \ir ../../merlin-server/sql/tables/simulation_template.sql
  \ir ../../merlin-server/sql/tables/simulation.sql

  -- Uploaded datasets (or datasets generated from simulation).
  \ir ../../merlin-server/sql/tables/dataset.sql
  \ir ../../merlin-server/sql/tables/span.sql
  \ir ../../merlin-server/sql/tables/profile.sql
  \ir ../../merlin-server/sql/tables/profile_segment.sql
  \ir ../../merlin-server/sql/tables/topic.sql
  \ir ../../merlin-server/sql/tables/event.sql

  -- Analysis intents
  \ir ../../merlin-server/sql/tables/condition.sql
  \ir ../../merlin-server/sql/tables/profile_request.sql

  \ir ../../merlin-server/sql/tables/mission_model_parameters.sql
  \ir ../../merlin-server/sql/tables/simulation_dataset.sql
  \ir ../../merlin-server/sql/tables/plan_dataset.sql
end;
