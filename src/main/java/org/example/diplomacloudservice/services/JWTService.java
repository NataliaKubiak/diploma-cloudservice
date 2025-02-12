package org.example.diplomacloudservice.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Date;

@Service
public class JWTService {

    @Value("${jwt_secret}")
    private String secret;

    public String generateToken(String username) {
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(60).toInstant());

        return JWT.create()
                .withSubject("User details")
                .withClaim("username", username)//инфа которая идет в теле токена (payload)
                .withIssuedAt(new Date()) //когда создан
                .withExpiresAt(expirationDate) //когда закончится
                .sign(Algorithm.HMAC256(secret));
    }

    public String validateTokenAndRetrieveClaim(String token) throws JWTDecodeException { //эту ошибку выбрасывает verifier.verify(token) если один из указанных параметров не прошел проверку
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)) //верифаер специальный для НАШЕГО токена
                .withSubject("User details")
                .build();

        DecodedJWT decodedJWT = verifier.verify(token); //проверяем токен на: правильный алгоритм шифрования, правильную подпись (третья часть токена с секретом), срок не истек, claim - наличие и правильность
        return decodedJWT.getClaim("username").asString(); //возвращаем раскодированное имя пользователя
    }
}
