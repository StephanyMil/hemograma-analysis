package com.inf.ubiquitous.computing.backend_hemograma_analysis.user.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desabilita CSRF para APIs
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/fhir/**").permitAll() // Permite acesso aos endpoints FHIR
                .requestMatchers("/test/**").permitAll() // Permite acesso aos endpoints de teste
                .anyRequest().authenticated() // Outros endpoints precisam autenticação
            );
        
        return http.build();
    }
}