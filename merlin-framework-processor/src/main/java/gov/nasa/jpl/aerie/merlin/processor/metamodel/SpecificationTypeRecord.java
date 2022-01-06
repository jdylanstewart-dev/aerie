package gov.nasa.jpl.aerie.merlin.processor.metamodel;

import javax.lang.model.element.TypeElement;
import java.util.List;

public record SpecificationTypeRecord(
    String specificationName,
    String name,
    TypeElement declaration,
    List<ParameterRecord> parameters) { }