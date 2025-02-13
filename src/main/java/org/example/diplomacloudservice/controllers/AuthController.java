package org.example.diplomacloudservice.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.diplomacloudservice.dto.AuthDto;
import org.example.diplomacloudservice.dto.JsonResponse;
import org.example.diplomacloudservice.services.JWTService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Log4j2
@RestController
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    @PostMapping("/login")
    public Map<String, String> performLogin(@RequestBody AuthDto authDto) {
        log.info("Login attempt for user: {}", authDto.getLogin());

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        authDto.getLogin(),
                        authDto.getPassword()
                );

        try {
            authenticationManager.authenticate(authToken);
            log.info("Authentication successful for user: {}", authDto.getLogin());
        } catch (Exception e) {
            log.warn("Authentication failed for user: {}", authDto.getLogin());
            throw e;
        }

        String token = jwtService.generateToken(authDto.getLogin());
        log.debug("Generated JWT token for user: {}", authDto.getLogin());

        return Map.of("auth-token", token);
    }

    @PostMapping("/logout")
    public ResponseEntity<JsonResponse> performLogout() {
        log.info("Logout request received.");
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(new JsonResponse("Successful logout", 200));
    }
}
