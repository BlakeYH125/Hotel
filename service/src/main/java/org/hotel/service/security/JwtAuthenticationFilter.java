package org.hotel.service.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hotel.model.constants.ParametersConstants;
import org.hotel.dao.repository.UserRepository;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Сервис для работы с токенами.
     */
    private final JwtService jwtService;

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    /**
     * Репозиторий для работы с таблицей пользователей в БД.
     */
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository,
                                   CustomAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(ParametersConstants.BEARER_CUT);

        try {
            if (jwt.isBlank()) {
                throw new BadCredentialsException("Пустой токен");
            }

            String username = jwtService.extractUsername(jwt);
            if (username == null || username.isBlank()) {
                throw new BadCredentialsException("В токене отсутствует имя пользователя");
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userRepository.findByUsername(username)
                        .orElseThrow(() -> new BadCredentialsException("Аккаунт не найден"));

                if (!jwtService.isTokenValid(jwt, userDetails)) {
                    throw new BadCredentialsException("Недействительный токен");
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException | BadCredentialsException e) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, new BadCredentialsException("Недействительный токен", e));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
