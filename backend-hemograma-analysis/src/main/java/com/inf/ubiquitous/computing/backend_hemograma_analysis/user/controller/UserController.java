package com.inf.ubiquitous.computing.backend_hemograma_analysis.user.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.user.model.User;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.user.service.UserService;


@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }


    public record CreateUserRequest(String name, String email, String password) {}
    public record UpdateUserRequest(String name, String email) {}
    public record UserResponse(UUID id, String name, String email) {}

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail());
    }

    // ===== Endpoints =====

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody CreateUserRequest body) {
        User u = new User();
        u.setName(body.name());
        u.setEmail(body.email());
        u.setPassword(body.password());
        User saved = service.create(u);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping
    public List<UserResponse> list() {
        return service.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public UserResponse find(@PathVariable UUID id) {
        return toResponse(service.findById(id));
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable UUID id, @RequestBody UpdateUserRequest body) {
        User updated = service.update(id, body.name(), body.email());
        return toResponse(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        String msg = ex.getMessage() == null ? "Requisição inválida." : ex.getMessage();
        // Se a mensagem indicar "não encontrado", devolve 404; senão, 400
        if (msg.toLowerCase().contains("não encontrado")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
        }
        return ResponseEntity.badRequest().body(msg);
    }
}
