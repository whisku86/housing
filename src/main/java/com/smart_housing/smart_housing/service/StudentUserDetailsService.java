package com.smart_housing.smart_housing.service;

import com.smart_housing.smart_housing.model.Student;
import com.smart_housing.smart_housing.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class StudentUserDetailsService implements UserDetailsService {

    @Autowired
    private StudentRepository repo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Student s = repo.findByEmail(email);
        if (s == null) {
            throw new UsernameNotFoundException(email);
        }
        return User.withUsername(s.getEmail())
                .password(s.getPassword())   // {noop} plain text for now
                .roles("STUDENT")
                .build();
    }
}