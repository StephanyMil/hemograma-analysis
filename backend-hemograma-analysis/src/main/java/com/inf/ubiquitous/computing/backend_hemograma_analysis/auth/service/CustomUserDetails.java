package com.inf.ubiquitous.computing.backend_hemograma_analysis.auth.service;


import com.inf.ubiquitous.computing.backend_hemograma_analysis.user.dao.UserDao;
import com.inf.ubiquitous.computing.backend_hemograma_analysis.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetails implements UserDetailsService {

    @Autowired
    private UserDao userRepository;

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + name));

        return new org.springframework.security.core.userdetails.User(
                user.getName(),
                user.getPasswordHash(),
                Collections.emptyList()
        );
    }
}
