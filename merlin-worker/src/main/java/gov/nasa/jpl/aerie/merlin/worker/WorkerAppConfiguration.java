package gov.nasa.jpl.aerie.merlin.worker;

import gov.nasa.jpl.aerie.merlin.server.config.Store;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public record WorkerAppConfiguration(
    int httpPort,
    Path merlinFileStore,
    Path sandboxFileStore,
    Store store
) {
  public WorkerAppConfiguration {
    Objects.requireNonNull(merlinFileStore);
    Objects.requireNonNull(store);
  }

  public Path merlinJarsPath() { return merlinFileStore.resolve("jars"); }
  public Path merlinFilesPath() { return merlinFileStore.resolve("files"); }
  public Path sandboxFilesPath() { return sandboxFileStore.resolve("files"); }
}
