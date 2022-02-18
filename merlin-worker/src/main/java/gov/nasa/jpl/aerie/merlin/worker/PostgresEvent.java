package gov.nasa.jpl.aerie.merlin.worker;

public record PostgresEvent(int processId, String channelName, String payload) {
}
