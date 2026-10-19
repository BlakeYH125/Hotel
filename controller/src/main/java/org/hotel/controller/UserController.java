package org.hotel.controller;

import org.hotel.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hotel/users")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class UserController {
    private final UserService userService;

    public UserController(final UserService userServiceP) {
        this.userService = userServiceP;
    }

    @PutMapping("/{username}/admin")
    public ResponseEntity<Void> setAdmin(@PathVariable("username") String username) {
        userService.setAdmin(username);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{username}/admin")
    public ResponseEntity<Void> removeAdmin(@PathVariable("username") String username) {
        userService.removeAdmin(username);
        return ResponseEntity.noContent().build();
    }
}