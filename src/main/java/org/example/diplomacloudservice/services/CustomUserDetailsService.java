package org.example.diplomacloudservice.services;

import lombok.AllArgsConstructor;
import org.example.diplomacloudservice.entities.User;
import org.example.diplomacloudservice.repositories.UserRepository;
import org.example.diplomacloudservice.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

//    public CustomUserDetailsService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username).orElseThrow(
                () -> new UsernameNotFoundException("User with login '" + username + "' not found")
        );

        return new CustomUserDetails(user);
    }
}
