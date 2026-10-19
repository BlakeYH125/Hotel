package org.hotel.service.security;

import org.hotel.model.entities.User;
import org.hotel.model.enums.Role;
import org.hotel.model.exceptions.UserAlreadyExistsException;
import org.hotel.model.exceptions.UserNotFoundException;
import org.hotel.model.exceptions.WrongPasswordException;
import org.hotel.dao.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_ShouldSaveUserWithUserRole_WhenAllCorrect() {
        String username = "user";
        String password = "password";

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        Mockito.when(passwordEncoder.encode(password)).thenReturn("encoded_password");
        Mockito.when(jwtService.generateToken(ArgumentMatchers.any(User.class))).thenReturn("token");

        String actual = authenticationService.register(username, password);

        Assertions.assertEquals("token", actual);

        Mockito.verify(userRepository).save(ArgumentMatchers.argThat(user ->
                username.equals(user.getUsername())
                        && "encoded_password".equals(user.getPassword())
                        && user.getRole() == Role.ROLE_USER
        ));

        Mockito.verify(passwordEncoder).encode(password);
        Mockito.verify(jwtService).generateToken(ArgumentMatchers.any(User.class));
    }

    @Test
    void register_ShouldThrowUserAlreadyExistsException_WhenUserAlreadyExists() {
        String username = "user";
        String password = "user";
        Role role = Role.ROLE_USER;
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Assertions.assertThrows(UserAlreadyExistsException.class, () -> authenticationService.register(username, password));

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(username);
    }

    @Test
    void authenticate_ShouldGenerateUsersToken_WhenAllCorrect() {
        String username = "user";
        String password = "user";
        Role role = Role.ROLE_USER;
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Mockito.when(jwtService.generateToken(user)).thenReturn("token");

        String actual = authenticationService.authenticate(username, password);

        Assertions.assertEquals("token", actual);

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(username);
        Mockito.verify(authenticationManager, Mockito.times(1)).authenticate(ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class));
        Mockito.verify(jwtService, Mockito.times(1)).generateToken(user);
    }

    @Test
    void authenticate_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        String username = "user";
        String password = "user";

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class, () -> authenticationService.authenticate(username, password));

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(username);
    }

    @Test
    void authenticate_ShouldThrowWrongPasswordException_WhenPasswordIsIncorrect() {
        String username = "user";
        String wrongPassword = "user";

        User user = new User();
        user.setUsername(username);

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Mockito.when(authenticationManager.authenticate(ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Bad credentials"));

        Assertions.assertThrows(WrongPasswordException.class, () -> authenticationService.authenticate(username, wrongPassword));

        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(username);
        Mockito.verify(authenticationManager, Mockito.times(1)).authenticate(ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class));
    }
}
