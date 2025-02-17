package org.example.diplomacloudservice.integrationTests.service;

import org.example.diplomacloudservice.repositories.TokenRepository;
import org.example.diplomacloudservice.services.JwtService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JwtServiceIntegrationTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenRepository tokenRepository;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
    }

    @Test
    void testAddTokenToBlacklist() {
        String token = jwtService.generateToken("user1");
        jwtService.addTokenToBlacklist("user1", "Bearer " + token);

        Optional<Object> maybeToken = tokenRepository.findFirstByToken(token);
        assertTrue(maybeToken.isPresent());
    }

    @Test
    void testIsTokenInBlacklist() {
        String token = jwtService.generateToken("user1");
        jwtService.addTokenToBlacklist("user1", "Bearer " + token);

        assertTrue(jwtService.isTokenInBlacklist(token));
    }
}
