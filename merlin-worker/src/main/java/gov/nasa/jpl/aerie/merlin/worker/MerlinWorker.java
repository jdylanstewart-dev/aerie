package gov.nasa.jpl.aerie.merlin.worker;

import com.impossibl.postgres.jdbc.PGDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gov.nasa.jpl.aerie.merlin.server.config.InMemoryStore;
import gov.nasa.jpl.aerie.merlin.server.config.PostgresStore;
import gov.nasa.jpl.aerie.merlin.server.config.Store;
import gov.nasa.jpl.aerie.merlin.server.mocks.InMemoryMissionModelRepository;
import gov.nasa.jpl.aerie.merlin.server.mocks.InMemoryPlanRepository;
import gov.nasa.jpl.aerie.merlin.server.remotes.InMemoryResultsCellRepository;
import gov.nasa.jpl.aerie.merlin.server.remotes.MissionModelRepository;
import gov.nasa.jpl.aerie.merlin.server.remotes.PlanRepository;
import gov.nasa.jpl.aerie.merlin.server.remotes.ResultsCellRepository;
import gov.nasa.jpl.aerie.merlin.server.remotes.postgres.PostgresMissionModelRepository;
import gov.nasa.jpl.aerie.merlin.server.remotes.postgres.PostgresPlanRepository;
import gov.nasa.jpl.aerie.merlin.server.remotes.postgres.PostgresResultsCellRepository;
import gov.nasa.jpl.aerie.merlin.server.services.LocalMissionModelService;
import gov.nasa.jpl.aerie.merlin.server.services.LocalPlanService;
import gov.nasa.jpl.aerie.merlin.server.services.UnexpectedSubtypeError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/* TODO: account for mission model jar and files that need to be copied and sandboxed with the worker. At the moment
    in Docker I'm mounting the merlin-file-store volume for the worker as a read-only volume.
*/
public final class MerlinWorker {
  private static final Logger log = Logger.getLogger(MerlinWorker.class.getName());

  public static void main(String[] args) throws Exception {
    final var configuration = loadConfiguration();
    final var store = configuration.store();

    if (!(store instanceof final PostgresStore postgresStore)) {
      throw new UnexpectedSubtypeError(Store.class, store);
    }
    final var pgDataSource = new PGDataSource();
    pgDataSource.setServerName(postgresStore.server());
    pgDataSource.setPortNumber(postgresStore.port());
    pgDataSource.setDatabaseName(postgresStore.database());
    pgDataSource.setApplicationName("Merlin Server");

    final var hikariConfig = new HikariConfig();
    hikariConfig.setUsername(postgresStore.user());
    hikariConfig.setPassword(postgresStore.password());
    hikariConfig.setDataSource(pgDataSource);

    final var hikariDataSource = new HikariDataSource(hikariConfig);

    final var stores = new Stores(
        new PostgresPlanRepository(hikariDataSource),
        new PostgresMissionModelRepository(hikariDataSource),
        new PostgresResultsCellRepository(hikariDataSource));

    // Assemble the core non-web object graph.
    final var modelController = new LocalMissionModelService(configuration.merlinJarsPath(), stores.missionModels());
    final var planController = new LocalPlanService(stores.plans());
    System.out.println("Models configured");
    final var listenAction = new ListenSimulationCapability(hikariDataSource);
//    PGConnection connection = hikariDataSource.getConnection().unwrap(PGConnection.class);
    listenAction.registerListener(new ClaimSimulation(hikariDataSource));

    while (true) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
  }

  private record Stores (PlanRepository plans, MissionModelRepository missionModels, ResultsCellRepository results) {}

  private static Stores loadStores(final WorkerAppConfiguration config) {
    final var store = config.store();

    if (store instanceof PostgresStore c) {
      final var pgDataSource = new PGDataSource();
      pgDataSource.setServerName(c.server());
      pgDataSource.setPortNumber(c.port());
      pgDataSource.setDatabaseName(c.database());
      pgDataSource.setApplicationName("Merlin Server");

      final var hikariConfig = new HikariConfig();
      hikariConfig.setUsername(c.user());
      hikariConfig.setPassword(c.password());
      hikariConfig.setDataSource(pgDataSource);

      final var hikariDataSource = new HikariDataSource(hikariConfig);

      return new Stores(
          new PostgresPlanRepository(hikariDataSource),
          new PostgresMissionModelRepository(hikariDataSource),
          new PostgresResultsCellRepository(hikariDataSource));
    } else if (store instanceof InMemoryStore c) {
      final var inMemoryPlanRepository = new InMemoryPlanRepository();
      return new Stores(
          inMemoryPlanRepository,
          new InMemoryMissionModelRepository(),
          new InMemoryResultsCellRepository(inMemoryPlanRepository));

    } else {
      throw new UnexpectedSubtypeError(Store.class, store);
    }
  }

  private static Path makeJarsPath(final WorkerAppConfiguration configuration) {
    try {
      System.out.println(configuration.merlinJarsPath());
      return Files.createDirectories(configuration.merlinJarsPath());
    } catch (final IOException ex) {
      throw new Error("Error creating merlin file store jars directory", ex);
    }
  }

  private static Path makeMissionModelDataPath(final WorkerAppConfiguration configuration) {
    try {
      return Files.createDirectories(configuration.merlinFilesPath());
    } catch (final IOException ex) {
      throw new Error("Error creating merlin file store files directory", ex);
    }
  }

  private static final String getEnv(final String key, final String fallback){
    final var env = System.getenv(key);
    return env == null ? fallback : env;
  }

  private static WorkerAppConfiguration loadConfiguration() {
    return new WorkerAppConfiguration(
        Integer.parseInt(getEnv("MERLIN_WORKER_PORT","27185")),
        Path.of(getEnv("MERLIN_WORKER_LOCAL_STORE","/usr/src/app/merlin_file_store")),
        Path.of(getEnv("MERLIN_WORKER_SANDBOX_STORE","/usr/src/app/merlin_file_sandbox")),
        new PostgresStore(getEnv("MERLIN_WORKER_DB_SERVER","postgres"),
                          getEnv("MERLIN_WORKER_DB_USER","aerie"),
                          Integer.parseInt(getEnv("MERLIN_WORKER_DB_PORT","5432")),
                          getEnv("MERLIN_WORKER_DB_PASSWORD","aerie"),
                          getEnv("MERLIN_WORKER_DB","aerie_merlin"))
    );
  }
}
