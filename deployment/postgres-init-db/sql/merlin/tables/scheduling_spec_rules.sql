create table scheduling_spec_rules (
  spec_id integer not null,
  rule_id integer not null,
  priority integer not null,

  constraint scheduling_spec_rules_primary_key
    primary key (spec_id, rule_id, priority),
  constraint scheduling_spec_rules_references_scheduling_spec
    foreign key (spec_id)
      references scheduling_spec
      on update cascade
      on delete cascade,
  constraint scheduling_spec_rules_references_scheduling_rules
    foreign key (rule_id)
      references scheduling_rule
      on update cascade
      on delete cascade
);

comment on table scheduling_spec_rules is e''
  'A join table associating scheduling specs with scheduling rules.';
comment on column scheduling_spec_rules.spec_id is e''
  'The ID of the scheduling spec a scheduling rule is associated with.';
comment on column scheduling_spec_rules.rule_id is e''
  'The ID of the scheduling rule a scheduling spec is associated with.';
comment on column scheduling_spec_rules.priority is e''
  'The relative priority of a scheduling rule in relation to other '
  'scheduling rules within the same spec.';
