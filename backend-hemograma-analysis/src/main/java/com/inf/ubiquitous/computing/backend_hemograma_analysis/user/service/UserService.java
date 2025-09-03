package com.inf.ubiquitous.computing.backend_hemograma_analysis.user.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.inf.ubiquitous.computing.backend_hemograma_analysis.user.dao.UserDao;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.user.model.User;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Transactional
    public User create(User input) {
        if (userDao.existsByEmail(input.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }
        return userDao.save(input);
    }


    public User findById(UUID id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    public List<User> findAll() {
        return userDao.findAll();
    }

    @Transactional
    public User update(UUID id, String name, String email) {
        User u = findById(id);

        if (!u.getEmail().equalsIgnoreCase(email) && userDao.existsByEmail(email)) {
            throw new IllegalArgumentException("E-mail já está em uso por outro usuário.");
        }

        u.setName(name);
        u.setEmail(email);
        return userDao.save(u);
    }

    @Transactional
    public void delete(UUID id) {
        if (!userDao.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        userDao.deleteById(id);
    }
}
