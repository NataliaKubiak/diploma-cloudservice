package org.example.diplomacloudservice.controllers;

import lombok.AllArgsConstructor;
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

@RestController
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    @PostMapping("/login")
    public Map<String, String> performLogin(@RequestBody AuthDto authDto) {

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        authDto.getLogin(),
                        authDto.getPassword()
                );

        authenticationManager.authenticate(authToken);

        String token = jwtService.generateToken(authDto.getLogin());
        return Map.of("auth-token", token);
    }

    @PostMapping("/logout")
    public ResponseEntity<JsonResponse> performLogout() {
        SecurityContextHolder.clearContext();
        JsonResponse response = new JsonResponse("Successful logout", 200);

        return ResponseEntity.ok(response);
    }
}
