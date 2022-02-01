create table scheduling_request (
  spec_id     integer not null,
  analysis_id integer not null,

  status text not null default 'incomplete',

  scheduling_spec_revision integer not null,

  constraint scheduling_request_primary_key
    primary key(spec_id, analysis_id),
  constraint scheduling_request_references_scheduling_spec
    foreign key(spec_id)
      references scheduling_spec
      on update cascade
      on delete cascade,
  constraint scheduling_request_references_analysis
    foreign key(analysis_id)
      references scheduling_analysis
      on update cascade
      on delete cascade
);

comment on table scheduling_request is e''
  'The status of a scheduling run that is to be performed (or has been performed).';
comment on column scheduling_request.spec_id is e''
  'The ID of scheduling specification for this scheduling run.';
comment on column scheduling_request.analysis_id is e''
  'The ID associated with the analysis of this scheduling run.';
comment on column scheduling_request.status is e''
  'The state of the the scheduling request.';
comment on column scheduling_request.scheduling_spec_revision is e''
  'The revision of the scheduling_spec associated with this request.';

create or replace function create_scheduling_analysis()
returns trigger
security definer
language plpgsql as $$begin
  insert into scheduling_analysis
  default values
  returning id into new.analysis_id;
return new;
end$$;

do $$ begin
create trigger create_scheduling_analysis_trigger
  before insert on scheduling_request
  for each row
  execute function create_scheduling_analysis();
exception
  when duplicate_object then null;
end $$;
