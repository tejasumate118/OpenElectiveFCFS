package edu.kdkce.openelectivefcfs.src.config.security;

import edu.kdkce.openelectivefcfs.src.config.security.filter.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Autowired
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws/**").permitAll()  // WebSocket endpoints
                        .requestMatchers(HttpMethod.GET, "/api/auth/**").permitAll() // Auth-related GET endpoints
                        .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll() // Auth-related POST endpoints
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow OPTIONS requests
                        .requestMatchers(HttpMethod.PATCH, "/api/staff/**").hasRole("STAFF")  // Staff patch permissions
                        .requestMatchers("/api/student/**").hasRole("STUDENT")  // Student endpoints
                        .requestMatchers("/api/staff/**").hasRole("STAFF")  // Staff endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")  // Admin endpoints
                        .requestMatchers("/error").permitAll()  // Allow access to error pages
                        .anyRequest().authenticated() // All other requests must be authenticated
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) // Add JWT filter
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Stateless sessions
                );

        return http.build();
    }
}