package gov.nasa.jpl.aerie.merlin.processor.generator;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import gov.nasa.jpl.aerie.merlin.framework.annotations.Export;
import gov.nasa.jpl.aerie.merlin.processor.TypePattern;
import gov.nasa.jpl.aerie.merlin.processor.metamodel.ExportTypeRecord;
import gov.nasa.jpl.aerie.merlin.protocol.types.MissingArgumentsException;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Method maker for defaults style where some arguments are provided within an @WithDefaults static class. */
/*package-private*/ final class SomeStaticallyDefinedMethodMaker extends MapperMethodMaker {

  public SomeStaticallyDefinedMethodMaker(final ExportTypeRecord exportType) {
    super(exportType);
  }

  @Override
  public MethodSpec makeInstantiateMethod() {
    var activityTypeName = exportType.declaration().getSimpleName().toString();

    var methodBuilder = MethodSpec.methodBuilder("instantiate")
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(Override.class)
        .returns(TypeName.get(exportType.declaration().asType()))
        .addException(unconstructableInstantiateException)
        .addException(MissingArgumentsException.class)
        .addParameter(
            ParameterizedTypeName.get(
                java.util.Map.class,
                String.class,
                gov.nasa.jpl.aerie.merlin.protocol.types.SerializedValue.class),
            "arguments",
            Modifier.FINAL);

    for (final var element : exportType.declaration().getEnclosedElements()) {
      if (element.getAnnotation(Export.WithDefaults.class) == null) continue;
      var defaultsName = element.getSimpleName().toString();
      methodBuilder = methodBuilder.addStatement(
          "final var defaults = new $L.$L()",
          activityTypeName,
          defaultsName);

      methodBuilder = methodBuilder.addCode(
          exportType.parameters()
              .stream()
              .map(parameter -> CodeBlock
                  .builder()
                  .addStatement(
                      "$T $L = $T$L",
                      new TypePattern.ClassPattern(
                          ClassName.get(Optional.class),
                          List.of(TypePattern.from(parameter.type))).render(),
                      parameter.name,
                      Optional.class,
                      ".empty()"
                  )
              )
              .reduce(CodeBlock.builder(), (x, y) -> x.add(y.build()))
              .build()).addCode("\n");

      methodBuilder = produceParametersFromDefaultsClass(methodBuilder);

      methodBuilder = methodBuilder.beginControlFlow("for (final var $L : $L.entrySet())", "entry", "arguments")
        .beginControlFlow("switch ($L.getKey())", "entry")
        .addCode(
            exportType.parameters()
                .stream()
                .map(parameter -> CodeBlock
                    .builder()
                    .add("case $S:\n", parameter.name)
                    .indent()
                    .addStatement(
                        "$L = Optional.ofNullable(this.mapper_$L.deserializeValue($L.getValue()).getSuccessOrThrow($$ -> new $T()))",
                        parameter.name,
                        parameter.name,
                        "entry",
                        unconstructableInstantiateException)
                    .addStatement("break")
                    .unindent())
                .reduce(CodeBlock.builder(), (x, y) -> x.add(y.build()))
                .build())
        .addCode(
            CodeBlock
                .builder()
                .add("default:\n")
                .indent()
                .addStatement(
                    "throw new $T()",
                    unconstructableInstantiateException)
                .unindent()
                .build())
        .endControlFlow()
        .endControlFlow().addCode("\n");
    }

    methodBuilder = makeArgumentsPresentCheck(methodBuilder).addCode("\n");

    // Add return statement with instantiation of class with parameters
    methodBuilder = methodBuilder.addStatement(
        "return new $T($L)",
        exportType.declaration(),
        exportType.parameters().stream().map(parameter -> parameter.name + ".get()").collect(Collectors.joining(", ")));

    return methodBuilder.build();
  }

  @Override
  public List<String> getParametersWithDefaults() {
    Optional<Element> defaultsClass = Optional.empty();
    for (final var element : exportType.declaration().getEnclosedElements()) {
      if (element.getAnnotation(Export.WithDefaults.class) == null) continue;
      defaultsClass = Optional.of(element);
    }

    final var fieldNameList = new ArrayList<String>();
    defaultsClass.ifPresent(c -> {
      for (final Element fieldElement : c.getEnclosedElements()) {
        if (fieldElement.getKind() != ElementKind.FIELD) continue;
        fieldNameList.add(fieldElement.getSimpleName().toString());
      }
    });

    return fieldNameList;
  }

  private MethodSpec.Builder produceParametersFromDefaultsClass(final MethodSpec.Builder methodBuilder)
  {
    return methodBuilder.addCode(getParametersWithDefaults().stream()
        .map(fieldName -> CodeBlock
            .builder()
            .addStatement(
                "$L = Optional.ofNullable($L.$L)",
                fieldName,
                "defaults",
                fieldName))
        .reduce(CodeBlock.builder(), (x, y) -> x.add(y.build())).build()).addCode("\n");
  }
}
