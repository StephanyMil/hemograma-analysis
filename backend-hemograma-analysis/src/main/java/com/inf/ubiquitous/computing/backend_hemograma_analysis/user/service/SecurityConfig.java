package com.inf.ubiquitous.computing.backend_hemograma_analysis.user.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Para testes via Insomnia/Postman, sem tokens/CSRF
            .csrf(csrf -> csrf.disable())
            // (opcional) CORS só é necessário se for chamar via browser de outro domínio
            //.cors(cors -> cors.disable())
            .authorizeHttpRequests(auth -> auth
                // liberar gerador sintético
                .requestMatchers("/fhir/synthetic/**").permitAll()
                // liberar o webhook e testes
                .requestMatchers("/fhir/subscription/**").permitAll()
                .requestMatchers("/fhir/test-hemograma").permitAll()
                .requestMatchers("/fhir/test").permitAll()
                .requestMatchers("/api/hemogramas/**").permitAll()
                // pode manter aberto todo /fhir se preferir
                // .requestMatchers("/fhir/**").permitAll()
                .anyRequest().authenticated()
            )
            // Habilita basic auth para o que NÃO estiver liberado
            .httpBasic(withDefaults());

        return http.build();
    }
}
