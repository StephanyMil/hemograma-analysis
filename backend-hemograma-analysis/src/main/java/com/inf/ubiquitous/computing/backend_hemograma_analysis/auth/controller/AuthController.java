package com.inf.ubiquitous.computing.backend_hemograma_analysis.auth.controller;


import com.inf.ubiquitous.computing.backend_hemograma_analysis.auth.dto.AuthRequest;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.auth.dto.AuthResponse;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.auth.service.JwtTokenUtil;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.user.dao.UserDao;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.user.dto.UserDto;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.user.model.User;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDao userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/is-first-user")
    public ResponseEntity<?> isFirstUser() {
        long userCount = userRepository.count();

        Map<String, Object> response = new HashMap<>();
        response.put("isFirstUser", userCount == 0);
        response.put("userCount", userCount);

        return ResponseEntity.ok(response);
    }

    @PostMapping("  /login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            System.out.println("=== TENTATIVA DE LOGIN ===");
            System.out.println("Email recebido: " + authRequest.getEmail());
            System.out.println("Password recebido: " + authRequest.getPassword());

            Optional<User> userOpt = userRepository.findByEmail(authRequest.getEmail());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                System.out.println("Usuário encontrado: " + user.getEmail());
                System.out.println("PasswordHash no banco: " + user.getPassword());

                boolean passwordMatches = passwordEncoder.matches(authRequest.getPassword(), user.getPassword());
                System.out.println("Password matches: " + passwordMatches);
            } else {
                System.out.println("Usuário NÃO encontrado no banco");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );

            System.out.println("Autenticação bem-sucedida!");
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenUtil.generateToken(authentication);

            return ResponseEntity.ok(new AuthResponse(jwt, "Login successful!"));
        } catch (Exception e) {
            System.out.println("ERRO na autenticação: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logout successful!");
    }

    @PostMapping("/register-first-user")
    public ResponseEntity<?> register(@RequestBody @Valid UserDto userDto) {
        long userCount = userRepository.count();

        if (userCount > 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("System already has users. This endpoint is only for initial setup.");
        }

        Optional<User> existingUser = userRepository.findByEmail(userDto.getEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already in use.");
        }

        User userModel = new User();
        BeanUtils.copyProperties(userDto, userModel);
        userModel.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userRepository.save(userModel);

        return ResponseEntity.status(HttpStatus.CREATED).body(userModel);
    }
}
