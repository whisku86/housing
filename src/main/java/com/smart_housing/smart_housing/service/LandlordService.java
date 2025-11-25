package com.smart_housing.smart_housing.service;

import com.smart_housing.smart_housing.model.Landlord;
import com.smart_housing.smart_housing.repository.LandlordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.util.List;


@Service
public class LandlordService {

    private final LandlordRepository landlordRepository;

    public LandlordService(LandlordRepository landlordRepository) {
        this.landlordRepository = landlordRepository;
    }

    public String register(Landlord landlord) {
        if (landlordRepository.findByEmail(landlord.getEmail()) != null) {
            return "Email already exists!";
        }
        landlordRepository.save(landlord);
        return "Landlord registered successfully!";
    }

    public String login(String email, String password) {
        Landlord landlord = landlordRepository.findByEmail(email);
        if (landlord == null) {
            return "Email not found!";
        }
        if (!landlord.getPassword().equals(password)) {
            return "Incorrect password!";
        }
        return "Login successful!";
    }

    // admin

    public long getTotalLandlords() {
        return landlordRepository.count();
    }

    public List<Landlord> getAllLandlords() {
        return landlordRepository.findAll();
    }

    @Transactional
    public void activateLandlord(Long id, Long adminId) {
        Landlord landlord = landlordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Landlord not found"));
        landlord.setStatus("ACTIVE");
        landlord.setApprovedAt(Timestamp.valueOf(java.time.LocalDateTime.now()));
        landlord.setApprovedBy(adminId);
        landlordRepository.save(landlord);
    }
    @Transactional
    public void suspendLandlord(Long id, Long adminId) {
        Landlord landlord = landlordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Landlord not found"));
        landlord.setStatus("SUSPENDED");
        landlord.setApprovedAt(Timestamp.valueOf(java.time.LocalDateTime.now()));
        landlord.setApprovedBy(adminId);
        landlordRepository.save(landlord);
    }
}