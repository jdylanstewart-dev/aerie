package gov.nasa.jpl.aerie.merlin.worker;

import com.impossibl.postgres.api.jdbc.PGConnection;
import gov.nasa.jpl.aerie.merlin.worker.postgres.ClaimSimulationStatusAction;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.function.Consumer;

public class ClaimSimulation implements Consumer<PostgresEvent> {
  private final DataSource dataSource;

  public ClaimSimulation(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public void accept(final PostgresEvent event) {
    try {
      final var action =  new ClaimSimulationStatusAction(this.dataSource.getConnection().unwrap(PGConnection.class));
    } catch (SQLException ex) {

    }
  }
}
