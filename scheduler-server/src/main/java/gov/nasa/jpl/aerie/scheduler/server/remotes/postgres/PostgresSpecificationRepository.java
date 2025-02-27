package gov.nasa.jpl.aerie.scheduler.server.remotes.postgres;

import gov.nasa.jpl.aerie.constraints.time.Window;
import gov.nasa.jpl.aerie.merlin.protocol.types.Duration;
import gov.nasa.jpl.aerie.scheduler.ActivityCreationTemplate;
import gov.nasa.jpl.aerie.scheduler.ActivityExpression;
import gov.nasa.jpl.aerie.scheduler.ActivityType;
import gov.nasa.jpl.aerie.scheduler.CardinalityGoal;
import gov.nasa.jpl.aerie.scheduler.ChildCustody;
import gov.nasa.jpl.aerie.scheduler.server.exceptions.NoSuchSpecificationException;
import gov.nasa.jpl.aerie.scheduler.server.models.GoalRecord;
import gov.nasa.jpl.aerie.scheduler.server.models.PlanId;
import gov.nasa.jpl.aerie.scheduler.server.models.GoalId;
import gov.nasa.jpl.aerie.scheduler.server.models.Specification;
import gov.nasa.jpl.aerie.scheduler.server.models.SpecificationId;
import gov.nasa.jpl.aerie.scheduler.server.remotes.SpecificationRepository;
import gov.nasa.jpl.aerie.scheduler.server.services.RevisionData;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.stream.Collectors;

public final class PostgresSpecificationRepository implements SpecificationRepository {
  private final DataSource dataSource;

  public PostgresSpecificationRepository(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Specification getSpecification(final SpecificationId specificationId) throws NoSuchSpecificationException {
    try (final var connection = this.dataSource.getConnection()) {
      try (
          final var getSpecificationAction = new GetSpecificationAction(connection);
          final var getSpecificationGoalsAction = new GetSpecificationGoalsAction(connection)
      ) {
        final var specificationRecord = getSpecificationAction
            .get(specificationId.id())
            .orElseThrow(() -> new NoSuchSpecificationException(specificationId));
        final var goals = getSpecificationGoalsAction
            .get(specificationId.id())
            .stream()
            .map(PostgresSpecificationRepository::buildGoalRecord)
            .collect(Collectors.toList());

        return new Specification(
            new PlanId(specificationRecord.planId()),
            specificationRecord.planRevision(),
            goals,
            specificationRecord.horizonStartTimestamp(),
            specificationRecord.horizonEndTimestamp(),
            specificationRecord.simulationArguments()
        );
      }
    } catch (final SQLException ex) {
      throw new DatabaseException("Failed to get scheduling specification", ex);
    }
  }

  @Override
  public RevisionData getSpecificationRevisionData(final SpecificationId specificationId)
  throws NoSuchSpecificationException
  {
    try (final var connection = this.dataSource.getConnection()) {
      try (final var getSpecificationAction = new GetSpecificationAction(connection)) {
        final var specificationRevision = getSpecificationAction
            .get(specificationId.id())
            .orElseThrow(() -> new NoSuchSpecificationException(specificationId))
            .revision();

        return new SpecificationRevisionData(specificationRevision);
      }
    } catch (final SQLException ex) {
      throw new DatabaseException("Failed to get scheduling specification revision data", ex);
    }
  }

  private static GoalRecord buildGoalRecord(final PostgresGoalRecord pgGoal) {
    final var goalId = new GoalId(pgGoal.id());

    // TODO: WORKAROUND
    //       At this time we are unable to pull goal definitions from postgres
    //       As a workaround we are providing goal IDs and names and the
    //       scheduler agent will load the actual goal definitions from a JAR by name
    final var goalDefinition = new CardinalityGoal.Builder()
        .inPeriod(ActivityExpression.ofType(new ActivityType("")))
        .thereExistsOne(
            new ActivityCreationTemplate.Builder()
                .ofType(new ActivityType(""))
                .build())
        .named(pgGoal.definition())
        .forAllTimeIn(Window.at(Duration.SECONDS))
        .owned(ChildCustody.Jointly)
        .build();

    return new GoalRecord(goalId, goalDefinition);
  }
}
