package gov.nasa.jpl.aerie.merlin.server.mocks;

import gov.nasa.jpl.aerie.merlin.server.remotes.AdaptationRepositoryContractTest;
import org.junit.jupiter.api.Disabled;

@Disabled
public final class InMemoryAdaptationRepositoryTest extends AdaptationRepositoryContractTest {
    @Override
    protected void resetRepository() {
        this.adaptationRepository = new InMemoryAdaptationRepository();
    }
}