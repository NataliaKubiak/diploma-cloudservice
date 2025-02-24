package org.example.diplomacloudservice.services;

import org.example.diplomacloudservice.entities.User;
import org.example.diplomacloudservice.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @Test
 * void название-тестируемого-метода_что-проверяет-тест() {...}
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private static final String VALID_USERNAME = "validUser";
    private static final String INVALID_USERNAME = "invalidUser";

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setLogin(VALID_USERNAME);
        mockUser.setPassword("password123");
    }

    @Test
    void loadUserByUsername_shouldLoadUserByUsernameWhenUserExists() {
        when(userRepository.findByLogin(VALID_USERNAME)).thenReturn(Optional.of(mockUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(VALID_USERNAME);

        assertNotNull(userDetails);
        assertEquals(VALID_USERNAME, userDetails.getUsername());
        assertEquals(mockUser.getPassword(), userDetails.getPassword());

        verify(userRepository, times(1)).findByLogin(VALID_USERNAME);
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findByLogin(INVALID_USERNAME)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(INVALID_USERNAME);
        });

        assertEquals("User with login '" + INVALID_USERNAME + "' not found", exception.getMessage());
        verify(userRepository, times(1)).findByLogin(INVALID_USERNAME);
    }

}