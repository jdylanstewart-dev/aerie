package gov.nasa.jpl.aerie.foomissionmodel;

import javax.json.Json;

import gov.nasa.jpl.aerie.foomissionmodel.generated.ConfigurationMapper;
import gov.nasa.jpl.aerie.foomissionmodel.mappers.FooValueMappers;
import gov.nasa.jpl.aerie.merlin.driver.json.JsonEncoding;
import gov.nasa.jpl.aerie.merlin.protocol.model.ConfigurationType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public final class FooValueMappersTest {
  @Test
  public void testConfigurationMapper() throws ConfigurationType.UnconstructableConfigurationException {
    final var stream = FooValueMappersTest.class.getResourceAsStream("mission_config.json");
    final var serializedConfig = JsonEncoding.decode(Json.createReader(stream).read());
    final var config = new ConfigurationMapper().instantiate(serializedConfig.asMap().get());

    assertThat(config.sinkRate).isCloseTo(42.0, within(1e-9));
  }
}
