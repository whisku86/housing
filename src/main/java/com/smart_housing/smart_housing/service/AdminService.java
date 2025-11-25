package com.smart_housing.smart_housing.service;

import com.smart_housing.smart_housing.model.Admin;
import com.smart_housing.smart_housing.model.AdminStatus;
import com.smart_housing.smart_housing.repository.AdminRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminService(AdminRepository adminRepository, BCryptPasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String login(String username, String password) {
        Admin admin = adminRepository.findByUsername(username);

        if (admin == null) {
            return "Invalid credentials";
        }
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            return "Invalid credentials";
        }

        if (admin.getStatus() != AdminStatus.ACTIVE) {
            return "Account is inactive";
        }

        // Update last login
        admin.setLastLogin(LocalDateTime.now());
        adminRepository.save(admin);

        return "Login successful";
    }

    public Admin getAdminByUsername(String username) {
        return adminRepository.findByUsername(username);
    }
}