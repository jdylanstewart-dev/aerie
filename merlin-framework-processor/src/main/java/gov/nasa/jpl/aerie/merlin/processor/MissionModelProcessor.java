package gov.nasa.jpl.aerie.merlin.processor;

import gov.nasa.jpl.aerie.merlin.framework.annotations.ActivityType;
import gov.nasa.jpl.aerie.merlin.framework.annotations.MissionModel;
import gov.nasa.jpl.aerie.merlin.processor.generator.MissionModelGenerator;

import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Top-level annotation processor for mission models.
 * Involved parsing and code generation is handled by the {@link MissionModelParser} and {@link MissionModelGenerator} respectively.
 */
public final class MissionModelProcessor implements Processor {

  private final Set<Element> foundActivityTypes = new HashSet<>();
  private final Set<Element> ownedActivityTypes = new HashSet<>();

  // Effectively final, late-initialized
  private Messager messager = null;
  private Filer filer = null;
  private Elements elementUtils = null;
  private Types typeUtils = null;

  @Override
  public Set<String> getSupportedOptions() {
    return Set.of();
  }

  /** Elements marked by these annotations will be treated as processing roots. */
  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(
        MissionModel.class.getCanonicalName(),
        ActivityType.class.getCanonicalName());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public void init(final ProcessingEnvironment processingEnv) {
    this.messager = processingEnv.getMessager();
    this.filer = processingEnv.getFiler();
    this.elementUtils = processingEnv.getElementUtils();
    this.typeUtils = processingEnv.getTypeUtils();
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    // Accumulate any information added in this round.
    this.foundActivityTypes.addAll(roundEnv.getElementsAnnotatedWith(ActivityType.class));

    final var missionModelParser = new MissionModelParser(elementUtils, typeUtils);
    final var missionModelGen = new MissionModelGenerator(elementUtils, typeUtils, messager);

    // Iterate over all elements annotated with @MissionModel
    for (final var element : roundEnv.getElementsAnnotatedWith(MissionModel.class)) {
      final var packageElement = (PackageElement) element;
      try {
        final var missionModelRecord = missionModelParser.parseMissionModel(packageElement);

        final var generatedFiles = new ArrayList<>(List.of(missionModelGen.generateMerlinPlugin(missionModelRecord)));

        missionModelRecord.modelConfigurationType
            .flatMap(configType -> missionModelGen.generateMissionModelConfigurationMapper(missionModelRecord, configType))
            .ifPresent(generatedFiles::add);

        generatedFiles.addAll(List.of(
            missionModelGen.generateMissionModelFactory(missionModelRecord),
            missionModelGen.generateActivityActions(missionModelRecord),
            missionModelGen.generateActivityTypes(missionModelRecord)
        ));
        for (final var activityRecord : missionModelRecord.activityTypes) {
          this.ownedActivityTypes.add(activityRecord.declaration());
          if (!activityRecord.mapper().isCustom) {
            missionModelGen.generateActivityMapper(missionModelRecord, activityRecord).ifPresent(generatedFiles::add);
          }
        }

        for (final var generatedFile : generatedFiles) {
          this.messager.printMessage(
              Diagnostic.Kind.NOTE,
              "Generating " + generatedFile.packageName + "." + generatedFile.typeSpec.name);
          generatedFile.writeTo(this.filer);
        }
      } catch (final InvalidMissionModelException ex) {
        final var trace = ex.getStackTrace();
        this.messager.printMessage(
            Diagnostic.Kind.ERROR,
            (ex.getMessage()
             + "\n"
             + Arrays
                 .stream(trace)
                 .filter(x -> x.getClassName().startsWith("gov.nasa.jpl.aerie.merlin."))
                 .map(Object::toString)
                 .collect(Collectors.joining("\n"))),
            ex.element,
            ex.annotation,
            ex.attribute);
      } catch (final Throwable ex) {
        final var trace = ex.getStackTrace();
        this.messager.printMessage(
            Diagnostic.Kind.ERROR,
            (ex.getMessage()
             + "\n"
             + Arrays
                 .stream(trace)
                 .filter(x -> x.getClassName().startsWith("gov.nasa.jpl.aerie.merlin."))
                 .map(Object::toString)
                 .collect(Collectors.joining("\n"))));
      }
    }

    if (roundEnv.processingOver()) {
      for (final var foundActivityType : this.foundActivityTypes) {
        if (this.ownedActivityTypes.contains(foundActivityType)) continue;

        this.messager.printMessage(
            Diagnostic.Kind.WARNING,
            "@ActivityType-annotated class is not referenced by any @WithActivity",
            foundActivityType);
      }
    }

    // Allow other annotation processors to process the framework annotations.
    return false;
  }

  @Override
  public Iterable<? extends Completion> getCompletions(
      final Element element,
      final AnnotationMirror annotation,
      final ExecutableElement member,
      final String userText)
  {
    return Collections::emptyIterator;
  }
}
