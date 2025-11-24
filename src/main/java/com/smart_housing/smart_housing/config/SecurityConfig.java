package com.smart_housing.smart_housing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // TODO upgrade to BCrypt
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 1. STATIC RESOURCES
                        .requestMatchers("/css/**", "/js/**", "/images/**",
                                "/webjars/**", "/assets/**", "/fonts/**").permitAll()

                        // 2. PUBLIC PAGES
                        .requestMatchers("/", "/index.html", "/about.html", "/contact.html").permitAll()

                        // 3. API – allow everything except booking
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/students/register", "/api/students/login").permitAll()

                        // 4. BOOKING – only authenticated users
                        .requestMatchers("/api/booking/**").authenticated()

                        // 5. EVERYTHING ELSE – permit all
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable())          // optional for REST
                .formLogin(form -> form
                        .loginPage("/login.html")          // your own page (optional)
                        .defaultSuccessUrl("/index.html", true)
                        .permitAll()
                )
                .logout(logout -> logout.permitAll());

        return http.build();
    }
}