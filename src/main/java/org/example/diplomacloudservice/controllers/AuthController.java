package org.example.diplomacloudservice.controllers;

import lombok.AllArgsConstructor;
import org.example.diplomacloudservice.dto.AuthDto;
import org.example.diplomacloudservice.exceptions.JsonResponse;
import org.example.diplomacloudservice.servises.JWTServise;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTServise jwtServise;

//    public AuthController(AuthenticationManager authenticationManager, JWTServise jwtServise) {
//        this.authenticationManager = authenticationManager;
//        this.jwtServise = jwtServise;
//    }

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

    @PostMapping("/logout")
    public ResponseEntity<JsonResponse> performLogout() {
        SecurityContextHolder.clearContext();
        JsonResponse response = new JsonResponse("Successful logout", 200);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/test")
    public ResponseEntity<HttpStatus> performTest() {
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @ExceptionHandler
    public ResponseEntity<JsonResponse> handleException(BadCredentialsException e) {
        JsonResponse jsonResponse = new JsonResponse(e.getMessage(), 400);

        return new ResponseEntity<>(jsonResponse, HttpStatus.BAD_REQUEST);
    }
}
