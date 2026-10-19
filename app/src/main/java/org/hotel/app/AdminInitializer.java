package org.hotel.app;

import org.hotel.dao.repository.UserRepository;
import org.hotel.model.entities.User;
import org.hotel.model.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                            @Value("${hotel.admin.username}") String username,
                            @Value("${hotel.admin.password}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsAnyUser()) {
            return;
        }

        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Для создания первого администратора задайте HOTEL_ADMIN_USERNAME и HOTEL_ADMIN_PASSWORD в properties");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.ROLE_ADMIN);
        userRepository.save(user);
    }
}