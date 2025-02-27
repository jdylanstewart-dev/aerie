package gov.nasa.jpl.aerie.constraints.tree;

import gov.nasa.jpl.aerie.constraints.model.ActivityInstance;
import gov.nasa.jpl.aerie.constraints.model.LinearProfile;
import gov.nasa.jpl.aerie.constraints.model.SimulationResults;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class Rate implements Expression<LinearProfile> {
  public final Expression<LinearProfile> profile;

  public Rate(final Expression<LinearProfile> profile) {
    this.profile = profile;
  }


  @Override
  public LinearProfile evaluate(final SimulationResults results, final Map<String, ActivityInstance> environment) {
    return this.profile.evaluate(results, environment).rate();
  }

  @Override
  public void extractResources(final Set<String> names) {
    this.profile.extractResources(names);
  }

  @Override
  public String prettyPrint(final String prefix) {
    return String.format(
        "(rate-of %s)",
        this.profile.prettyPrint(prefix + "  ")
    );
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Rate)) return false;
    final var o = (Rate)obj;

    return Objects.equals(this.profile, o.profile);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.profile);
  }
}
