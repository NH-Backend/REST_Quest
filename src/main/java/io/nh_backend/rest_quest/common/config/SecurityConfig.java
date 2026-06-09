package io.nh_backend.rest_quest.common.config;

import io.nh_backend.rest_quest.common.security.JwtAuthenticationFilter;
import io.nh_backend.rest_quest.common.security.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(basic -> basic.disable())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers(HttpMethod.GET, EndPoints.GET_PUBLIC).permitAll()
                                .requestMatchers(HttpMethod.POST, EndPoints.POST_PUBLIC).permitAll()

                                .requestMatchers(HttpMethod.GET, EndPoints.GET_AUTHENTICATED).authenticated()

                                .requestMatchers(HttpMethod.POST, EndPoints.POST_AUTHENTICATED).authenticated()

                                .requestMatchers(HttpMethod.DELETE, EndPoints.DELETE_AUTHENTICATED).authenticated()

                                .anyRequest().denyAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    static public class EndPoints {

        public static final String[] GET_PUBLIC = {
                "/api/v1/items",
                "/api/v1/items/*",
                "/api/v1/npcs",
                "/api/v1/npcs/*"
        };

        public static final String[] GET_AUTHENTICATED = {
                "/api/v1/users/me",
                "/api/v1/users/me/data",
                "/api/v1/users/me/friends",
                "/api/v1/users/me/friends/requests",
                "/api/v1/users/me/inventory",
                "/api/v1/users/me/profile",
                "/api/v1/users/me/wallet"
        };

        public static final String[] POST_PUBLIC = {
                "/api/v1/auth/login",
                "/api/v1/auth/refresh",
                "/api/v1/users/register"
        };

        public static final String[] POST_AUTHENTICATED = {
                "/api/v1/auth/logout",
                "/api/v1/users/me/friends/requests",
                "/api/v1/users/me/friends/requests/*/accept",
                "/api/v1/users/me/friends/requests/*/decline",
                "/api/v1/users/me/inventory/*/unequip",
                "/api/v1/users/me/inventory/*/use",
                "/api/v1/users/me/inventory/pickup",
                "/api/v1/users/me/npcs/*/items/*/purchase",
                "/api/v1/users/me/npcs/*/items/*/gacha"
        };

        public static final String[] DELETE_AUTHENTICATED = {
                "/api/v1/users/me/friends/requests/*",
                "/api/v1/users/me/friends/*",
                "/api/v1/users/me/inventory/*/discard"
        };

    }
}
