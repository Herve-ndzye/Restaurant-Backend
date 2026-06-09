package com.mavic.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
    "jwt.secret=test-jwt-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm"
})
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
