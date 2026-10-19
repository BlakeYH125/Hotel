package org.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hotel.model.enums.Role;
import org.hotel.model.exceptions.UserAlreadyExistsException;
import org.hotel.model.exceptions.UserNotFoundException;
import org.hotel.model.exceptions.WrongPasswordException;
import org.hotel.dao.repository.UserRepository;
import org.hotel.service.security.AuthenticationService;
import org.hotel.service.security.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class AuthentificationControllerTest {

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void register_ShouldReturn200AndToken_WhenRequestIsCorrect() throws Exception {
        AuthenticationController.RegisterRequest request = new AuthenticationController.RegisterRequest("newAdmin", "newAdmin");

        Mockito.when(authenticationService.register("newAdmin", "newAdmin")).thenReturn("token");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(MockMvcRequestBuilders.post("/hotel/auth/register")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("token"));
        Mockito.verify(authenticationService, Mockito.times(1)).register("newAdmin", "newAdmin");
    }

    @Test
    void register_ShouldReturn409_WhenUserNameAlreadyExists() throws Exception {
        AuthenticationController.RegisterRequest request = new AuthenticationController.RegisterRequest("newAdmin", "newAdmin");

        Mockito.doThrow(new UserAlreadyExistsException()).when(authenticationService).register("newAdmin", "newAdmin");

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(MockMvcRequestBuilders.post("/hotel/auth/register")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isConflict());
        Mockito.verify(authenticationService, Mockito.times(1)).register("newAdmin", "newAdmin");
    }

    @Test
    void authenticate_ShouldReturn200AndToken_WhenAllCorrect() throws Exception {
        AuthenticationController.AuthRequest authRequest = new AuthenticationController.AuthRequest("user", "user");

        Mockito.when(authenticationService.authenticate("user", "user")).thenReturn("token");

        String json = objectMapper.writeValueAsString(authRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/hotel/auth/login")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("token"));
        Mockito.verify(authenticationService, Mockito.times(1)).authenticate("user", "user");
    }

    @Test
    void authenticate_ShouldReturn401_WhenWrongPassword() throws Exception {
        AuthenticationController.AuthRequest authRequest = new AuthenticationController.AuthRequest("user", "user");

        Mockito.doThrow(new WrongPasswordException()).when(authenticationService).authenticate("user", "user");

        String json = objectMapper.writeValueAsString(authRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/hotel/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        Mockito.verify(authenticationService, Mockito.times(1)).authenticate("user", "user");
    }

    @Test
    void authenticate_ShouldReturn404_WhenWrongUsername() throws Exception {
        AuthenticationController.AuthRequest authRequest = new AuthenticationController.AuthRequest("user", "user");

        Mockito.doThrow(new UserNotFoundException()).when(authenticationService).authenticate("user", "user");

        String json = objectMapper.writeValueAsString(authRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/hotel/auth/login")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
        Mockito.verify(authenticationService, Mockito.times(1)).authenticate("user", "user");
    }
}
