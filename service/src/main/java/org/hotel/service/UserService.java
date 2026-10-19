package org.hotel.service;

import jakarta.transaction.Transactional;
import org.hotel.dao.repository.UserRepository;
import org.hotel.model.entities.User;
import org.hotel.model.enums.Role;
import org.hotel.model.exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(final UserRepository userRepositoryP) {
        this.userRepository = userRepositoryP;
    }

    public void setAdmin(String username) {
        changeRole(username, Role.ROLE_ADMIN);
    }

    public void removeAdmin(String username) {
        changeRole(username, Role.ROLE_USER);
    }

    private void changeRole(String username, Role role) {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        user.setRole(role);
        userRepository.save(user);
    }
}