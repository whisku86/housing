package com.smart_housing.smart_housing.Controller;

import com.smart_housing.smart_housing.model.Landlord;
import com.smart_housing.smart_housing.repository.LandlordRepository;
import com.smart_housing.smart_housing.service.LandlordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
@Data
@CrossOrigin(origins = {"http://127.0.0.1:8080", "http://localhost:8080"})
@RestController
@RequestMapping("/api/landlords")
public class LandlordController {

    @Autowired
    private LandlordService landlordService;

    @Autowired
    private LandlordRepository landlordRepository;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Landlord landlord) {
        // Check if email exists
        if (landlordRepository.findByEmail(landlord.getEmail()) != null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists!"));
        }

        // Save landlord
        landlord.setStatus("active");
        Landlord saved = landlordRepository.save(landlord);

        // Return success + landlord ID
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Landlord registered successfully!");
        response.put("landlordId", saved.getLandlordId()); // ← critical!
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Landlord landlord) {
        String result = landlordService.login(landlord.getEmail(), landlord.getPassword());
        if (result.equals("Login successful!")) {
            // Fetch landlord to get ID
            Landlord found = landlordRepository.findByEmail(landlord.getEmail());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful!");
            response.put("landlordId", found.getLandlordId());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(Map.of("message", result));
    }
}