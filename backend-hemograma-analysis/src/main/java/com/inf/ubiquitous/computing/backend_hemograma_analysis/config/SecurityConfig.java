package com.inf.ubiquitous.computing.backend_hemograma_analysis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.auth.service.JwtRequestFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        // Endpoints de autenticação - sem autenticação
                        .requestMatchers("/api/auth/**").permitAll()

                        // Endpoints FHIR - sem autenticação
                        .requestMatchers("/fhir/**").permitAll()
                        .requestMatchers("/fhir/synthetic/**").permitAll()
                        .requestMatchers("/fhir/subscription/**").permitAll()
                        .requestMatchers("/fhir/test-hemograma").permitAll()
                        .requestMatchers("/fhir/test").permitAll()

                        // API de hemogramas - sem autenticação
                        .requestMatchers("/api/hemogramas/**").permitAll()
                        .requestMatchers("/api/hemogram/**").permitAll()

                        // API de estatísticas - sem autenticação (ADICIONADO)
                        .requestMatchers("/api/estatisticas/**").permitAll()

                        // Endpoints de teste - sem autenticação
                        .requestMatchers("/test/**").permitAll()
                        .requestMatchers("/api/teste-basico/**").permitAll()

                        // Endpoints de ferramentas (tools) - sem autenticação
                        .requestMatchers("/tools/**").permitAll()

                        // Actuator (health check) - sem autenticação
                        .requestMatchers("/actuator/**").permitAll()

                        // Documentação (Swagger) - sem autenticação
                        .requestMatchers("/error", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Qualquer outra requisição requer autenticação via JWT
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Adiciona o filtro JWT antes do filtro padrão de autenticação
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}