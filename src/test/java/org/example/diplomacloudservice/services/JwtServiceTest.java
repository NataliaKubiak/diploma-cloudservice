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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        jwtService.secret = SECRET_KEY;  // Присваиваем секретный ключ
    }

    @Test
    void shouldGenerateTokenForUser() {
        String token = jwtService.generateToken(USERNAME);

        assertNotNull(token);

        verify(tokenRepository, times(0)).findFirstByToken(any());
    }

    @Test
    void shouldValidateTokenAndRetrieveClaim() throws JWTDecodeException {
        String token = jwtService.generateToken(USERNAME);

        String usernameFromToken = jwtService.validateTokenAndRetrieveClaim(token);

        assertEquals(USERNAME, usernameFromToken);
    }

    @Test
    void shouldThrowJWTDecodeExceptionWhenTokenIsInvalid() {
        String invalidToken = "invalidToken";

        assertThrows(JWTDecodeException.class, () -> {
            jwtService.validateTokenAndRetrieveClaim(invalidToken);
        });
    }

    @Test
    void shouldReturnTrueIfTokenIsInBlacklist() {
        String token = "Bearer blacklistedToken";
        when(tokenRepository.findFirstByToken(token)).thenReturn(Optional.of(new Token()));

        assertTrue(jwtService.isTokenInBlacklist(token));
    }

    @Test
    void shouldReturnFalseIfTokenIsNotInBlacklist() {
        String token = "Bearer nonBlacklistedToken";
        when(tokenRepository.findFirstByToken(token)).thenReturn(Optional.empty());

        assertFalse(jwtService.isTokenInBlacklist(token));
    }
}