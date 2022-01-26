create table scheduling_template_rules (
  template_id integer not null,
  rule_id integer not null,
  priority integer not null,

  constraint scheduling_template_rules_primary_key
    primary key (template_id, rule_id, priority),
  constraint scheduling_template_rules_references_scheduling_template
    foreign key (template_id)
      references scheduling_template
      on update cascade
      on delete cascade,
  constraint scheduling_template_rules_references_scheduling_rules
    foreign key (rule_id)
      references scheduling_rule
      on update cascade
      on delete cascade
);

comment on table scheduling_template_rules is e''
  'A join table associating scheduling templates with scheduling rules.';
comment on column scheduling_template_rules.template_id is e''
  'The ID of the scheduling template a scheduling rule is associated with.';
comment on column scheduling_template_rules.rule_id is e''
  'The ID of the scheduling rule a scheduling template is associated with.';
comment on column scheduling_template_rules.priority is e''
  'The relative priority of a scheduling rule in relation to other '
  'scheduling rules within the same template.';
