package gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.models.activities;

import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.constraints.Constraint;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.constraints.ConstraintStructure;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.time.Windows;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.constraints.ConditionTypes.ActivityCondition.OCCURRING;

public final class ActivityTypeState {
  private final String activityType;
  private final ActivityModelQuerier querier;

  public ActivityTypeState(final String activityType, final ActivityModelQuerier querier) {
    this.activityType = activityType;
    this.querier = querier;
  }

  public Constraint whenActive() {
    return this.exists(a -> a.whenActive());
  }

  public Constraint exists(final Function<ActivityInstanceState, Constraint> predicate) {;

    return new Constraint() {
      @Override
      public Set<String> getActivityTypes() {
        return Set.of(activityType);
      }

      @Override
      public Set<String> getActivityIds() {
        final var matchingIds = new HashSet<String>();

        for (var activityId : querier.getActivitiesOfType(activityType)) {
          final var constraint = predicate.apply(new ActivityInstanceState(activityId, activityType, querier));
          final var matchWindows = constraint.getWindows();
          if (matchWindows.isEmpty()) continue;

          matchingIds.addAll(constraint.getActivityIds());
        }

        return matchingIds;
      }

      @Override
      public Set<String> getStateIds() {
        return new HashSet<>();
      }

      @Override
      public Windows getWindows() {
        final var windows = new Windows();

        for (var activityId : querier.getActivitiesOfType(activityType)) {
          final var constraint = predicate.apply(new ActivityInstanceState(activityId, activityType, querier));
          final var matchWindows = constraint.getWindows();
          if (matchWindows.isEmpty()) continue;

          windows.addAll(matchWindows);
        }

        return windows;
      }

      @Override
      public ConstraintStructure getStructure() {
        return ConstraintStructure.ofActivityConstraint(activityType, OCCURRING);
      }

    };
  }
}