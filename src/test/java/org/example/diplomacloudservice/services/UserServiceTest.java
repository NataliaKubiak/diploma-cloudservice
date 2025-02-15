package org.example.diplomacloudservice.services;

import org.example.diplomacloudservice.entities.User;
import org.example.diplomacloudservice.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private static final String USERNAME = "testUser";
    private static final String INVALID_USERNAME = "invalidUser";

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setLogin(USERNAME);
    }

    @Test
    void shouldReturnUserWhenUsernameExists() {
        when(userRepository.findByLogin(USERNAME)).thenReturn(Optional.of(mockUser));

        User user = userService.getUserByUsername(USERNAME);

        assertNotNull(user);
        assertEquals(USERNAME, user.getLogin());

        verify(userRepository, times(1)).findByLogin(USERNAME);
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenUsernameDoesNotExist() {
        when(userRepository.findByLogin(INVALID_USERNAME)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.getUserByUsername(INVALID_USERNAME);
        });

        verify(userRepository, times(1)).findByLogin(INVALID_USERNAME);
    }
}