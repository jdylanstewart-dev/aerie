package gov.nasa.jpl.ammos.mpsa.aerie.banananation;

import gov.nasa.jpl.ammos.mpsa.aerie.banananation.state.BananaStates;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.MerlinAdaptation;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.SimulationState;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.ActivityMapper;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.ActivityMapperLoader;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.annotations.Adaptation;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.spice.SpiceLoader;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.states.interfaces.State;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.time.Instant;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Adaptation(name="Banananation", version="0.0.1")
public class Banananation implements MerlinAdaptation {

  @Override
  public ActivityMapper getActivityMapper() {
    try {
      return ActivityMapperLoader.loadActivityMapper(Banananation.class);
    } catch (ActivityMapperLoader.ActivityMapperLoadException e) {
      // TODO: We should add an exception to merlin-sdk that adaptations can
      //       throw to signify that loading the activity mapper failed
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public SimulationState newSimulationState(final Instant simulationStartTime) {
    final var model = new BananaStates(simulationStartTime);

    return new SimulationState() {
      @Override
      public void applyInScope(final Runnable scope) {
        BananaStates.modelRef.setWithin(model, scope::run);
      }

      @Override
      public Map<String, State<?>> getStates() {
        final var states = List.of(model.fruitState, model.peelState);
        return states.stream().collect(Collectors.toMap(x -> x.getName(), x -> x));
      }
    };
  }

  // TODO: move this into newSimulationState()
  static {
    SpiceLoader.loadSpice();
  }
}
