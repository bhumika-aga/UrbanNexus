/*
 * Copyright (c) 2026 Bhumika Agarwal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.urbannexus.config;

import com.urbannexus.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
        throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           @Value("${app.cors.allowed-origins:}") String allowedOrigins,
                                           @Value("${spring.h2.console.enabled:false}") boolean h2ConsoleEnabled)
        throws Exception {
        // Exact origins (or patterns such as http://localhost:*) of the frontend, comma-separated.
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                                   .map(String::trim)
                                   .filter(origin -> !origin.isEmpty())
                                   .toList();
        if (origins.isEmpty()) {
            log.warn("No CORS origins configured (CORS_ALLOWED_ORIGINS); browsers will refuse cross-origin API calls.");
        } else {
            log.info("CORS allowed origins: {}", origins);
        }
        
        List<String> publicPaths = new ArrayList<>(List.of("/", "/error", "/api/login", "/actuator/**"));
        if (h2ConsoleEnabled) {
            publicPaths.add("/h2-console/**");
        }
        
        http.cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOriginPatterns(origins);
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                // The JWT travels in the Authorization header, never in cookies, so credentialed
                // (cookie-carrying) cross-origin requests are not needed.
                config.setAllowCredentials(false);
                return config;
            })).csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(requests -> requests
                                                   .requestMatchers(publicPaths.toArray(String[]::new)).permitAll()
                                                   .anyRequest().authenticated())
            // The H2 console (local development only) renders in a frame; otherwise keep framing denied.
            .headers(headers -> headers.frameOptions(frame -> {
                if (h2ConsoleEnabled) {
                    frame.sameOrigin();
                } else {
                    frame.deny();
                }
            }))
            .exceptionHandling(exceptions -> exceptions
                                                 .authenticationEntryPoint((request, response, authException) -> {
                                                     log.warn("Unauthenticated access to {}: {}", request.getRequestURI(),
                                                         authException.getMessage());
                                                     response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                                                 })
                                                 .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                     log.warn("Access denied for {}: {}", request.getRequestURI(),
                                                         accessDeniedException.getMessage());
                                                     response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                                                 }));
        
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
