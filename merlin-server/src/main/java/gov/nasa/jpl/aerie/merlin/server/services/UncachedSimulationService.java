package gov.nasa.jpl.aerie.merlin.server.services;

import gov.nasa.jpl.aerie.merlin.server.ResultsProtocol;
import gov.nasa.jpl.aerie.merlin.server.mocks.InMemoryRevisionData;
import gov.nasa.jpl.aerie.merlin.server.models.PlanId;
import gov.nasa.jpl.aerie.merlin.server.remotes.InMemoryResultsCellRepository.InMemoryCell;

public record UncachedSimulationService (SimulationAgent action) implements SimulationService {
  @Override
  public ResultsProtocol.State getSimulationResults(final PlanId planId, final RevisionData revisionData) {
    if (!(revisionData instanceof InMemoryRevisionData inMemoryRevisionData)) {
      throw new Error("UncachedSimulationService only accepts InMemoryRevisionData");
    }
    final var cell = new InMemoryCell(planId, inMemoryRevisionData.planRevision());

    try {
      this.action.simulate(planId, revisionData, cell);
    } catch (final InterruptedException ex) {
      // Do nothing. We'll get the current result (probably an Incomplete)
      // and throw away the cell anyway.

      // TODO: Fail back to the common supervisor of this Service and the Agent.
      //   We don't know if the simulation request was successfully received or not,
      //   so the interaction between the two is now in an undefined state.
    }

    final var result = cell.get();
    cell.cancel();

    return result;
  }
}
