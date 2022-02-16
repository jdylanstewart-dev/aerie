package gov.nasa.jpl.aerie.merlin.protocol.types;

public sealed interface DurationType {
  record Controllable(String parameterName) implements DurationType {}
  record Uncontrollable() implements DurationType {}
}
