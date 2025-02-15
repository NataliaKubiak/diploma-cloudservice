package org.example.diplomacloudservice.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.diplomacloudservice.services.CustomUserDetailsService;
import org.example.diplomacloudservice.services.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTFilterTest {

    @InjectMocks
    private JWTFilter jwtFilter;

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    private static final String VALID_JWT = "valid.jwt.token";
    private static final String INVALID_JWT = "invalid.jwt.token";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateWhenValidTokenProvided() throws Exception {
        when(request.getHeader("auth-token")).thenReturn("Bearer " + VALID_JWT);
        when(jwtService.isTokenInBlacklist(VALID_JWT)).thenReturn(false);
        when(jwtService.validateTokenAndRetrieveClaim(VALID_JWT)).thenReturn("user");
        when(customUserDetailsService.loadUserByUsername("user")).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldRejectWhenTokenIsInBlacklist() throws Exception {
        when(request.getHeader("auth-token")).thenReturn("Bearer " + VALID_JWT);
        when(jwtService.isTokenInBlacklist(VALID_JWT)).thenReturn(true);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"message\":\"JWT Token is invalid\",\"status\":401}");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldRejectWhenTokenIsInvalid() throws Exception {
        when(request.getHeader("auth-token")).thenReturn("Bearer " + INVALID_JWT);
        when(jwtService.isTokenInBlacklist(INVALID_JWT)).thenReturn(false);
        when(jwtService.validateTokenAndRetrieveClaim(INVALID_JWT)).thenThrow(new JWTVerificationException("Invalid token"));
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"message\":\"Invalid JWT Token: Invalid token\",\"status\":401}");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldPassThroughWhenNoTokenProvided() throws Exception {
        when(request.getHeader("auth-token")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}