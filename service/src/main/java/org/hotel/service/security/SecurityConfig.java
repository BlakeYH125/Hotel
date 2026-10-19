package org.hotel.service.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Фильтр токенов.
     */
    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Провайдер аутентификации.
     */
    private final AuthenticationProvider authenticationProvider;

    /**
     * Обработчик ошибок доступа для авторизованных пользователей.
     */
    private final CustomAccessDeniedHandler accessDeniedHandler;

    /**
     * Точка входа для обработки ошибок аутентификации.
     */
    private final CustomAuthenticationEntryPoint authEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilterP,
                          AuthenticationProvider authenticationProviderP,
                          CustomAccessDeniedHandler accessDeniedHandlerP,
                          CustomAuthenticationEntryPoint authEntryPointP) {
        this.jwtAuthFilter = jwtAuthFilterP;
        this.authenticationProvider = authenticationProviderP;
        this.accessDeniedHandler = accessDeniedHandlerP;
        this.authEntryPoint = authEntryPointP;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/hotel/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(exc -> exc
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }
}
