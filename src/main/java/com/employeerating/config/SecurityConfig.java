package com.employeerating.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .cors().configurationSource(corsConfigurationSource()) // Explicitly apply CORS configuration
            .and()
            .authorizeRequests()
            .antMatchers(
                "/api/health",
                "/api/register",
                "/api/employees/export",
                "/auth/**",
                "/excel/**",
                "/api/v1/tasks/**",
                    "/api/teamlead/**"
            ).permitAll() // Allow these without authentication
            .anyRequest().permitAll() // Permit all other requests
            .and()
            .httpBasic().disable(); // Disable HTTP Basic auth (if not needed)
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins (no "*" when using credentials)
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "*" // Your production backend URL
        ));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(Arrays.asList("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        // strength 10 is default; increase if you need more work factor
        return new BCryptPasswordEncoder();
    }
}
