package gov.nasa.jpl.aerie.merlin.worker;

import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/*package local*/ public class ListenSimulationStatusAction implements AutoCloseable{
  private static final @Language("SQL") String sql = """
    LISTEN "insert_simulation"
  """;

  private final PreparedStatement statement;

  public ListenSimulationStatusAction(final Connection connection) throws SQLException {
    this.statement = connection.prepareStatement(sql);
  }

  public void apply() throws SQLException {
      this.statement.execute();
  }

  @Override
  public void close() throws SQLException {
    this.statement.close();
  }
}
