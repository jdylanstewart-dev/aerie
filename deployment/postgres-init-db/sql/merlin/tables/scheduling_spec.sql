create table scheduling_spec (
  id integer generated always as identity,
  revision integer not null default 0,

  plan_id integer not null,
  scheduling_window interval not null,
  simulation_arguments merlin_argument_set not null,

  constraint scheduling_specification_synthetic_key
    primary key(id),
  constraint scheduling_specification_references_plan
    foreign key(plan_id)
    references plan
    on update cascade
    on delete cascade
);

comment on table scheduling_spec is e''
  'The specification for a scheduling run.';
comment on column scheduling_spec.id is e''
  'The synthetic identifier for this scheduling specification.';
comment on column scheduling_spec.revision is e''
  'A monotonic clock that ticks for every change to this scheduling specification.';
comment on column scheduling_spec.plan_id is e''
  'The ID of the plan to be scheduled.';
comment on column scheduling_spec.scheduling_window is e''
  'The period of time within which the scheduler may place activities.';
comment on column scheduling_spec.simulation_arguments is e''
  'The arguments to use for simulation during scheduling.';

create function increment_revision_on_update()
  returns trigger
  security definer
language plpgsql as $$begin
  new.revision = old.revision + 1;
return new;
end$$;

create trigger increment_revision_on_update_trigger
  before update on scheduling_spec
  for each row
  when (pg_trigger_depth() < 1)
  execute function increment_revision_on_update();
