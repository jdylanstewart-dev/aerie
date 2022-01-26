-- The order of inclusion is important! Tables referenced by foreign keys must be loaded before their dependants.

begin;
  -- Domain types.
  \ir domain-types/merlin-arguments.sql

  -- Tables.
  -- Uploaded files (JARs or simulation input files).
  \ir tables/uploaded_file.sql

  -- Planning intents.
  \ir tables/mission_model.sql
  \ir tables/activity_type.sql
  \ir tables/plan.sql
  \ir tables/activity.sql
  \ir tables/simulation_template.sql
  \ir tables/simulation.sql

  -- Scheduling intents
  \ir tables/scheduling_rule.sql
  \ir tables/scheduling_template.sql
  \ir tables/scheduling_template_rules.sql
  \ir tables/scheduling_spec.sql
  \ir tables/scheduling_spec_rules.sql
  \ir tables/scheduling_request.sql
  \ir tables/scheduling_analysis.sql
  \ir tables/scheduling_rule_analysis.sql
  \ir tables/scheduling_rule_analysis_created_activities.sql
  \ir tables/scheduling_rule_analysis_satisfying_activities.sql

  -- Uploaded datasets (or datasets generated from simulation).
  \ir tables/dataset.sql
  \ir tables/span.sql
  \ir tables/profile.sql
  \ir tables/profile_segment.sql
  \ir tables/topic.sql
  \ir tables/event.sql

  -- Analysis intents
  \ir tables/condition.sql
  \ir tables/profile_request.sql

  \ir tables/mission_model_parameters.sql
  \ir tables/simulation_dataset.sql
  \ir tables/plan_dataset.sql
end;
