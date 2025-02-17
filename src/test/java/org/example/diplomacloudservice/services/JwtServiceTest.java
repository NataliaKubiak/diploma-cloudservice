package org.example.diplomacloudservice.services;

import com.auth0.jwt.exceptions.JWTDecodeException;
import org.example.diplomacloudservice.entities.Token;
import org.example.diplomacloudservice.repositories.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @Test
 * void название-тестируемого-метода_что-проверяет-тест() {...}
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private JwtService jwtService;

    private static final String SECRET_KEY = "testSecretKey";
    private static final String VALID_TOKEN = "Bearer validToken123";
    private static final String USERNAME = "testUser";
    private static final String INVALID_TOKEN = "Bearer invalidToken123";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(tokenRepository, userService);
        jwtService.secret = SECRET_KEY;
    }

    @Test
    void generateToken_shouldGenerateTokenForUser() {
        String token = jwtService.generateToken(USERNAME);

        assertNotNull(token);

        verify(tokenRepository, times(0)).findFirstByToken(any());
    }

    @Test
    void validateTokenAndRetrieveClaim_shouldValidateTokenAndRetrieveClaim() throws JWTDecodeException {
        String token = jwtService.generateToken(USERNAME);

        String usernameFromToken = jwtService.validateTokenAndRetrieveClaim(token);

        assertEquals(USERNAME, usernameFromToken);
    }

    @Test
    void validateTokenAndRetrieveClaim_shouldThrowJWTDecodeExceptionWhenTokenIsInvalid() {
        assertThrows(JWTDecodeException.class, () -> {
            jwtService.validateTokenAndRetrieveClaim(INVALID_TOKEN);
        });
    }

    @Test
    void addTokenToBlacklist_shouldReturnTrueIfTokenIsInBlacklist() {
        when(tokenRepository.findFirstByToken(VALID_TOKEN)).thenReturn(Optional.of(new Token()));

        assertTrue(jwtService.isTokenInBlacklist(VALID_TOKEN));
    }

    @Test
    void addTokenToBlacklist_shouldReturnFalseIfTokenIsNotInBlacklist() {
        when(tokenRepository.findFirstByToken(INVALID_TOKEN)).thenReturn(Optional.empty());

        assertFalse(jwtService.isTokenInBlacklist(INVALID_TOKEN));
    }
}