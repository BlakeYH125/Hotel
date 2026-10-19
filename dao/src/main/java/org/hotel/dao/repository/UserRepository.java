package org.hotel.dao.repository;

import org.hotel.model.entities.User;

import java.util.Optional;

public interface UserRepository {
    void save(User user);
    Optional<User> findByUsername(String username);
    boolean isOwner(String username, String guestId);
    boolean existsAnyUser();
}
