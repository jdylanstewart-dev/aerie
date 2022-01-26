create table scheduling_rule_analysis_satisfying_activities (
  analysis_id integer not null,
  rule_id integer not null,
  activity_id integer not null,

  constraint satisfying_activities_primary_key
      primary key (analysis_id, rule_id, activity_id),
  constraint satisfying_activities_references_scheduling_analysis
    foreign key (analysis_id)
      references scheduling_analysis
      on update cascade
      on delete cascade,
  constraint satisfying_activities_references_scheduling_rule
    foreign key (rule_id)
      references scheduling_rule
      on update cascade
      on delete cascade,
  constraint satisfying_activities_references_activity
    foreign key (activity_id)
      references activity
      on update cascade
      on delete cascade
);

comment on table scheduling_rule_analysis_satisfying_activities is e''
  'The activity instances satisfying a scheduling rule.';
comment on column scheduling_rule_analysis_satisfying_activities.analysis_id is e''
  'The associated analysis ID.';
comment on column scheduling_rule_analysis_satisfying_activities.rule_id is e''
  'The associated rule ID.';
comment on column scheduling_rule_analysis_satisfying_activities.activity_id is e''
  'The ID of an activity instance satisfying the associated rule.';
