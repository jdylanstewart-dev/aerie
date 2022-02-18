package gov.nasa.jpl.aerie.merlin.server.config;

import java.nio.file.Path;
import java.util.Objects;

public record AppConfiguration (
    int httpPort,
    JavalinLoggingState javalinLogging,
    boolean useWorkers,
    Path merlinFileStore,
    Store store
) {
  public AppConfiguration {
    Objects.requireNonNull(javalinLogging);
    Objects.requireNonNull(merlinFileStore);
    Objects.requireNonNull(store);
  }
}
