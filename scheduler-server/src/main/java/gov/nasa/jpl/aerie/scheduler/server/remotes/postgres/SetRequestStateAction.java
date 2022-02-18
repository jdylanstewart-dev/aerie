package gov.nasa.jpl.aerie.scheduler.server.remotes.postgres;

import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/*package-local*/ final class SetRequestStateAction implements AutoCloseable {
  private static final @Language("SQL") String sql = """
    update scheduling_request
      set
        state = ?,
        failure_reason = ?
      where
        spec_id = ? and
        spec_revision = ?
    """;

  private final PreparedStatement statement;

  public SetRequestStateAction(final Connection connection) throws SQLException {
    this.statement = connection.prepareStatement(sql);
  }

  public void apply(
      final long specificationid,
      final long specificationRevision,
      final String state,
      final String failureReason
  ) throws SQLException {
    this.statement.setString(1, state);
    this.statement.setString(2, failureReason);
    this.statement.setLong(3, specificationid);
    this.statement.setLong(4, specificationRevision);

    final var count = this.statement.executeUpdate();
    if (count < 1) throw new FailedUpdateException("scheduling_request");
    if (count > 1) throw new Error("More than one row affected by scheduling_request update by primary key. Is the database corrupted?");
  }

  @Override
  public void close() throws SQLException {
    this.statement.close();
  }
}
