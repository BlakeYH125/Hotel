package org.hotel.service.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    private UserDetails userDetails;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        String testSecret = Base64.getEncoder().encodeToString(new byte[32]);
        jwtService = new JwtService(testSecret);
        userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(userDetails.getUsername()).thenReturn("admin");
    }

    @Test
    void generateToken_ShouldReturnValidTokenString() {
        String token = jwtService.generateToken(userDetails);

        Assertions.assertNotNull(token);
        Assertions.assertFalse(token.isEmpty());

        Assertions.assertEquals(3, token.split("\\.").length);
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsernameFromToken() {
        String token = jwtService.generateToken(userDetails);

        String extractedUsername = jwtService.extractUsername(token);

        Assertions.assertEquals("admin", extractedUsername);
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenTokenMatchesUser() {
        String token = jwtService.generateToken(userDetails);

        Assertions.assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenUsernameDoesNotMatch() {
        String adminToken = jwtService.generateToken(userDetails);

        UserDetails other = Mockito.mock(UserDetails.class);
        Mockito.when(other.getUsername()).thenReturn("abc");

        Assertions.assertFalse(jwtService.isTokenValid(adminToken, other));
    }

    @Test
    void generateToken_WithExtraClaims_ShouldStillBeValid() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "SUPER_ADMIN");

        String token = jwtService.generateToken(extraClaims, userDetails);

        Assertions.assertNotNull(token);
        Assertions.assertTrue(jwtService.isTokenValid(token, userDetails));
        Assertions.assertEquals("admin", jwtService.extractUsername(token));
    }
}
