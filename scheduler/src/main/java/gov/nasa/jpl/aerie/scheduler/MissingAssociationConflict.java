package gov.nasa.jpl.aerie.scheduler;

import gov.nasa.jpl.aerie.constraints.time.Windows;

import java.util.Collection;

public class MissingAssociationConflict extends Conflict {
  private final Collection<ActivityInstance> instances;

  /**
   * ctor creates a new conflict
   *
   * @param goal IN STORED the dissatisfied goal that issued the conflict
   * @param instancesToChooseFrom IN the list of instances to choose from to perform the association
   */
  public MissingAssociationConflict(final Goal goal, final Collection<ActivityInstance> instancesToChooseFrom) {
    super(goal);
    this.instances = instancesToChooseFrom;
  }

  public Collection<ActivityInstance> getActivityInstancesToChooseFrom(){
    return instances;
  }

  @Override
  public Windows getTemporalContext() {
    return null;
  }
}
