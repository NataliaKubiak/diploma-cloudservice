package org.example.diplomacloudservice.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.log4j.Log4j2;
import org.example.diplomacloudservice.entities.Token;
import org.example.diplomacloudservice.entities.User;
import org.example.diplomacloudservice.repositories.TokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@Log4j2
@Service
public class JwtService {

    @Value("${jwt_secret}")
    private String secret;

    private final TokenRepository tokenRepository;
    private final UserService userService;

    public JwtService(TokenRepository tokenRepository, UserService userService) {
        this.tokenRepository = tokenRepository;
        this.userService = userService;
    }

    public String generateToken(String username) {
        log.debug("Generating token for user: {}", username);
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(60).toInstant());

        String token = JWT.create()
                .withSubject("User details")
                .withClaim("username", username)
                .withIssuedAt(new Date())
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secret));

        log.debug("Token generated successfully for user: {}", username);
        return token;
    }

    public String validateTokenAndRetrieveClaim(String token) throws JWTDecodeException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User details")
                .build();

        DecodedJWT decodedJWT = verifier.verify(token);
        return decodedJWT.getClaim("username").asString();
    }

    @Transactional
    public void addTokenToBlacklist(String username, String tokenHeader) {
        User owner = userService.getUserByUsername(username);

        String token = extractToken(tokenHeader);
        Date expirationDate = JWT.decode(token).getExpiresAt();
        LocalDateTime expires_at = expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        Token tokenEntity = Token.builder()
                .token(token)
                .user(owner)
                .expiresAt(expires_at)
                .build();

        tokenRepository.save(tokenEntity);
        log.info("Token added to blacklist");
    }

    public boolean isTokenInBlacklist(String token) {
        return tokenRepository.findFirstByToken(token).isPresent();
    }

    private String extractToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        log.error("Token Header does not start with 'Bearer ' or equals null");
        throw new RuntimeException("Token Header does not start with 'Bearer ' or equals null");
    }
}
