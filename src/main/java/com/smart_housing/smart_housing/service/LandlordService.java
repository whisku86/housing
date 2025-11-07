package com.smart_housing.smart_housing.service;

import com.smart_housing.smart_housing.model.Landlord;
import com.smart_housing.smart_housing.repository.LandlordRepository;
import org.springframework.stereotype.Service;

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
}