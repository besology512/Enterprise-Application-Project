package com.workhub.observability;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:workhub;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.jpa.show-sql=false",
                "debug=false",
                "logging.level.org.springframework=WARN",
                "logging.level.org.hibernate.SQL=WARN",
                "management.endpoint.health.probes.enabled=true",
                "management.health.rabbit.enabled=false"
        }
)
class ActuatorEndpointsIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void healthEndpointReturnsOkAndUpStatus() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/health", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "UP");
    }

    @Test
    void readinessEndpointIsAccessible() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/health/readiness", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void livenessEndpointIsAccessible() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/actuator/health/liveness", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
