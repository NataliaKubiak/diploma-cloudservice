package org.example.diplomacloudservice.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.diplomacloudservice.dto.JsonResponse;
import org.example.diplomacloudservice.services.CustomUserDetailsService;
import org.example.diplomacloudservice.services.JWTService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Log4j2
@Component
@AllArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.debug("JWTFilter triggered for request: {}", request.getRequestURI());
        String authHeader = request.getHeader("auth-token");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);

            if (jwt.isBlank()) {
                log.warn("Missing JWT Token in request");

                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing JWT Token", 401);
                return;
            }

            try {
                String username = jwtService.validateTokenAndRetrieveClaim(jwt);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                log.info("Authenticated user: {}", username);
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                userDetails.getPassword(),
                                userDetails.getAuthorities());

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    log.debug("Security context updated for user: {}", username);
                }
            } catch (JWTVerificationException e) {
                log.warn("Invalid JWT Token: {}", e.getMessage());
                SecurityContextHolder.clearContext();

                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token: " + e.getMessage(), 401);
                return;
            }
        } else {
            log.info("No Authorization header found");
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message, int id) throws IOException {
        response.setContentType("application/json");
        response.setStatus(status);

        response.getWriter().write(objectMapper.writeValueAsString(new JsonResponse(message, id)));
    }
}
