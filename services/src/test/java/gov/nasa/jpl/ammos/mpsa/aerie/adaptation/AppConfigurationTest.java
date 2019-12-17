package gov.nasa.jpl.ammos.mpsa.aerie.adaptation;

import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class AppConfigurationTest {

    @Test
    public void testParseProperties() {
        // Create JsonObject with expected properties
        int http_port = 7654;
        URI mongo_uri = URI.create("http://localhost.mongo.test");
        String mongo_database = "mongo_database_test";
        String mongo_adaptation_collection = "mongo_adaptation_collection_test";

        AppConfiguration expected = new AppConfiguration(http_port, mongo_uri, mongo_database, mongo_adaptation_collection);

        JsonObject config = Json.createObjectBuilder()
                .add("HTTP_PORT", http_port)
                .add("MONGO_URI", mongo_uri.toString())
                .add("MONGO_DATABASE", mongo_database)
                .add("MONGO_ADAPTATION_COLLECTION", mongo_adaptation_collection)
                .build();

        // Parse the JsonObject with parseProperties
        AppConfiguration observed = AppConfiguration.parseProperties(config);

        // Verify the values of each configuration parameter are as expected
        assertThat(observed).isEqualTo(expected);
    }
}