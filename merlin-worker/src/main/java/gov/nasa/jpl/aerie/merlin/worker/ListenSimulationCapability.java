package gov.nasa.jpl.aerie.merlin.worker;

import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.api.jdbc.PGNotificationListener;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.function.Consumer;

public class ListenSimulationCapability {
  private final DataSource dataSource;

  public ListenSimulationCapability(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void registerListener(Consumer<PostgresEvent> handler) throws SQLException {
    PGConnection connection = this.dataSource.getConnection().unwrap(PGConnection.class);
    connection.addNotificationListener(new PGNotificationListener() {
      @Override
      public void notification(int processId, String channelName, String payload) {
        System.out.println("Received PSQL Notification: " + processId + ", " + channelName + ", " + payload);
        handler.accept(new PostgresEvent(processId, channelName, payload));
      }
      @Override
      public void closed() {
        PGNotificationListener.super.closed();
      }
    });

    final var statement = connection.createStatement();
    statement.executeUpdate("LISTEN simulation_notification");
    statement.close();
  }
}
