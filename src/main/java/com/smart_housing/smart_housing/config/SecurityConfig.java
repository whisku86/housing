package com.smart_housing.smart_housing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Add BOTH beans - one for BCryptPasswordEncoder and one for PasswordEncoder
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. STATIC RESOURCES
                        .requestMatchers("/css/**", "/js/**", "/images/**",
                                "/webjars/**", "/assets/**", "/fonts/**").permitAll()

                        // 2. PUBLIC PAGES
                        .requestMatchers("/", "/index.html", "/about.html", "/contact.html").permitAll()

                        // 3. PUBLIC API ENDPOINTS
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/students/register", "/api/students/login").permitAll()

                        // 4. BOOKING – only authenticated users
                        .requestMatchers("/api/booking/**").authenticated()

                        // 5. EVERYTHING ELSE – permit all for now
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .defaultSuccessUrl("/index.html", true)
                        .permitAll()
                )
                .logout(logout -> logout.permitAll());

        return http.build();
    }
}