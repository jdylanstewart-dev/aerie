package gov.nasa.jpl.aerie.scheduler.server.services;

import gov.nasa.jpl.aerie.scheduler.server.exceptions.NoSuchSpecificationException;
import gov.nasa.jpl.aerie.scheduler.server.models.Specification;
import gov.nasa.jpl.aerie.scheduler.server.models.SpecificationId;
import gov.nasa.jpl.aerie.scheduler.server.remotes.SpecificationRepository;

public record LocalSpecificationService(SpecificationRepository specificationRepository) implements SpecificationService {
  @Override
  public Specification getSpecification(final SpecificationId specificationId) throws NoSuchSpecificationException
  {
    // TODO needs to be implemented
    throw new UnsupportedOperationException();
  }

  @Override
  public RevisionData getSpecificationRevisionData(final SpecificationId specificationId)
  throws NoSuchSpecificationException
  {
    // TODO needs to be implemented
    throw new UnsupportedOperationException();
  }
}
