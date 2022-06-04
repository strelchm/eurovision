package ru.strelchm.eurovision.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static ru.strelchm.eurovision.service.RateLimiterService.API_KEY_PARAMETER;

@Component
public class OpenApiCustomizer implements OpenApiCustomiser {
  private static final List<Function<PathItem, Operation>> OPERATION_GETTERS = Arrays.asList(
      PathItem::getGet, PathItem::getPost, PathItem::getDelete, PathItem::getHead,
      PathItem::getOptions, PathItem::getPatch, PathItem::getPut);

  private Stream<Operation> getOperations(PathItem pathItem) {
    return OPERATION_GETTERS.stream()
        .map(getter -> getter.apply(pathItem))
        .filter(Objects::nonNull);
  }

  @Override
  public void customise(OpenAPI openApi) {
    openApi.getPaths().values().stream()
        .flatMap(this::getOperations)
        .forEach(this::customize);
  }

  private void customize(Operation operation) {
    operation.addParametersItem(
        new Parameter()
            .in("header")
            .required(true)
            .description(String.format("%s header", API_KEY_PARAMETER))
            .name(API_KEY_PARAMETER));
  }
}