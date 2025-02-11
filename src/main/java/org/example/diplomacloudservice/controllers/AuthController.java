package org.example.diplomacloudservice.controllers;

import lombok.AllArgsConstructor;
import org.example.diplomacloudservice.dto.AuthDto;
import org.example.diplomacloudservice.exceptions.ErrorResponse;
import org.example.diplomacloudservice.servises.JWTServise;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTServise jwtServise;

    public AuthController(AuthenticationManager authenticationManager, JWTServise jwtServise) {
        this.authenticationManager = authenticationManager;
        this.jwtServise = jwtServise;
    }

    @PostMapping("/login")
    public Map<String, String> performLogin(@RequestBody AuthDto authDto) {

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        authDto.getLogin(),
                        authDto.getPassword()
                );

        authenticationManager.authenticate(authToken);

        String token = jwtServise.generateToken(authDto.getLogin());
        return Map.of("auth-token", token);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(BadCredentialsException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage(),
                400
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
