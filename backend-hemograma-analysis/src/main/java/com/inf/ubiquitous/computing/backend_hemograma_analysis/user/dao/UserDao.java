package com.inf.ubiquitous.computing.backend_hemograma_analysis.user.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.user.model.User;

@Repository
public interface UserDao extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
