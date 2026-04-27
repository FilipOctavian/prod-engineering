package ro.unibuc.prodeng;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Tag("IntegrationTest")
public abstract class IntegrationTestBase {

    private static final String MONGO_URL_ENV = System.getenv("MONGODB_CONECTION_URL");

    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer();

    // static {
    //     if (MONGO_URL_ENV == null || MONGO_URL_ENV.isEmpty()) {
    //         mongoDBContainer = new MongoDBContainer("mongo:6.0.20")
    //                 .withExposedPorts(27017)
    //                 .withSharding()
    //                 .withLabel("ro.unibuc.prodeng", "integration-test-mongo");
    //         mongoDBContainer.start();
    //     } else {
    //         mongoDBContainer = null;
    //     }
    // }

    // @DynamicPropertySource
    // static void setProperties(DynamicPropertyRegistry registry) {
    //     if (MONGO_URL_ENV != null && !MONGO_URL_ENV.isEmpty()) {
    //         registry.add("mongodb.connection.url", () -> MONGO_URL_ENV);
    //     } else {
    //         registry.add("mongodb.connection.url",
    //                 () -> "mongodb://localhost:" + mongoDBContainer.getMappedPort(27017));
    //     }
    // }

    static {
        if (System.getenv("MONGODB_CONECTION_URL") == null) {
            mongoDBContainer.start();
        }
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        if (mongoDBContainer.isRunning()) {
            String mongoUrl = "mongodb://localhost:" + mongoDBContainer.getMappedPort(27017);
            registry.add("mongodb.connection.url", () -> mongoUrl);
        }
    }
}
