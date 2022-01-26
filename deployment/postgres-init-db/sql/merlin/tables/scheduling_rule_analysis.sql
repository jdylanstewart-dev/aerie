create table scheduling_rule_analysis (
  analysis_id integer not null,
  rule_id integer not null,

  satisfied boolean not null,

  constraint scheduling_rule_analysis_primary_key
    primary key (analysis_id, rule_id),
  constraint scheduling_rule_analysis_references_scheduling_analysis
    foreign key (analysis_id)
      references scheduling_analysis
      on update cascade
      on delete cascade,
  constraint scheduling_rule_analysis_references_scheduling_rule
    foreign key (rule_id)
      references scheduling_rule
      on update cascade
      on delete cascade
);

comment on table scheduling_rule_analysis is e''
  'The analysis of single rule from a scheduling run.';
comment on column scheduling_rule_analysis.analysis_id is e''
  'The associated analysis ID.';
comment on column scheduling_rule_analysis.rule_id is e''
  'The associated rule ID.';
comment on column scheduling_rule_analysis.satisfied is e''
  'Whether the associated rule was satisfied by the scheduling run.';
