package com.inf.ubiquitous.computing.backend_hemograma_analysis.user.repository;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String email);


}

