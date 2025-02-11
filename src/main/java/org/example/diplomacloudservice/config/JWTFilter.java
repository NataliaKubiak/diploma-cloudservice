package org.example.diplomacloudservice.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.example.diplomacloudservice.exceptions.ErrorResponse;
import org.example.diplomacloudservice.servises.CustomUserDetailsService;
import org.example.diplomacloudservice.servises.JWTServise;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTServise jwtServise;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper;

    public JWTFilter(JWTServise jwtServise, CustomUserDetailsService customUserDetailsService, ObjectMapper objectMapper) {
        this.jwtServise = jwtServise;
        this.customUserDetailsService = customUserDetailsService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);

            if (jwt.isBlank()) {
                // TODO: 11/02/2025 может тут выбросить исключение с сообщением "Missing JWT Token" и собрать обработку всех исключений с установкой статусов в одно место (чтобы sendErrorResponse например был один на всю прилу)?
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing JWT Token", 401);
                return;
            }

            try {
                String username = jwtServise.validateTokenAndRetrieveClaim(jwt);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (JWTVerificationException e) {
                // TODO: 11/02/2025 может тут выбросить исключение с сообщением "Invalid JWT Token"?
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token", 401);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message, int id) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);

        response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(message, id)));
    }
}
