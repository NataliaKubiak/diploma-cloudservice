package org.example.diplomacloudservice.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Date;

@Log4j2
@Service
public class JWTService {

    @Value("${jwt_secret}")
    private String secret;

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
}
