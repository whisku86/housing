package com.smart_housing.smart_housing.Controller;


import com.smart_housing.smart_housing.model.Admin;
import com.smart_housing.smart_housing.model.Property;
import com.smart_housing.smart_housing.model.Landlord;
import com.smart_housing.smart_housing.service.AdminService;
import com.smart_housing.smart_housing.service.PropertyService;
import com.smart_housing.smart_housing.service.LandlordService;
import com.smart_housing.smart_housing.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://127.0.0.1:8080", "http://localhost:8080"})

public class AdminController {
    private final AdminService adminService;
    private final PropertyService propertyService;
    private final LandlordService landlordService;
    private final StudentService studentService;

    public AdminController(AdminService adminService,
                           PropertyService propertyService,
                           LandlordService landlordService, StudentService studentService) {
        this.adminService = adminService;
        this.propertyService = propertyService;
        this.landlordService = landlordService;
        this.studentService = studentService;
    }

    // Admin login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        String result = adminService.login(username, password);

        if (result.equals("Login successful")) {
            Admin admin = adminService.getAdminByUsername(username);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("adminId", admin.getId());
            response.put("username", admin.getUsername());
            response.put("role", admin.getRole());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest()
                .body(Map.of("message", result));
    }

    // Get dashboard statistics
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(
            @RequestHeader("X-Admin-Id") Long adminId) {

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProperties", propertyService.getTotalProperties());
        stats.put("totalLandlords", landlordService.getTotalLandlords());
        stats.put("pendingProperties", propertyService.getPendingPropertiesCount());
        stats.put("activeProperties", propertyService.getActivePropertiesCount());

        // Calculate total vacant rooms across all properties
        long totalVacantRooms = calculateTotalVacantRooms();
        stats.put("totalVacantRooms", totalVacantRooms);

        // Get total students
        stats.put("totalStudents", studentService.getTotalStudents());

        return ResponseEntity.ok(stats);
    }

    // Helper method to calculate total vacant rooms
    private long calculateTotalVacantRooms() {
        List<Property> allProperties = propertyService.getAllProperties();
        long totalVacant = 0;

        for (Property property : allProperties) {
            try {
                long vacantCount = propertyService.getVacantRoomCount(property.getId());
                totalVacant += vacantCount;
            } catch (Exception e) {
                // If there's an error getting vacant count for a property, skip it
                System.err.println("Error getting vacant count for property " + property.getId());
            }
        }

        return totalVacant;
    }

    // Get all landlords
    @GetMapping("/landlords")
    public ResponseEntity<List<Landlord>> getAllLandlords(
            @RequestHeader("X-Admin-Id") Long adminId) {
        return ResponseEntity.ok(landlordService.getAllLandlords());
    }

    // Get all properties with vacant room counts
    @GetMapping("/properties")
    public ResponseEntity<List<Map<String, Object>>> getAllProperties(
            @RequestHeader("X-Admin-Id") Long adminId) {
        List<Property> properties = propertyService.getAllProperties();

        // Enhance each property with vacant room count
        List<Map<String, Object>> enhancedProperties = properties.stream()
                .map(property -> {
                    Map<String, Object> propertyMap = new HashMap<>();
                    propertyMap.put("id", property.getId());
                    propertyMap.put("name", property.getName());
                    propertyMap.put("type", property.getType());
                    propertyMap.put("location", property.getLocation());
                    propertyMap.put("price", property.getPrice());
                    propertyMap.put("landlordId", property.getLandlordId());
                    propertyMap.put("status", property.getStatus());
                    propertyMap.put("amenities", property.getAmenities());
                    propertyMap.put("bills", property.getBills());
                    propertyMap.put("securityDetails", property.getSecurityDetails());
                    propertyMap.put("images", property.getImages());

                    // Add vacant room count
                    try {
                        long vacantCount = propertyService.getVacantRoomCount(property.getId());
                        propertyMap.put("vacantRooms", vacantCount);
                    } catch (Exception e) {
                        propertyMap.put("vacantRooms", 0);
                    }

                    return propertyMap;
                })
                .toList();

        return ResponseEntity.ok(enhancedProperties);
    }

    // Approve property
    @PutMapping("/properties/{id}/approve")
    public ResponseEntity<Map<String, String>> approveProperty(
            @PathVariable Long id,
            @RequestHeader("X-Admin-Id") Long adminId) {

        propertyService.approveProperty(id, adminId);
        return ResponseEntity.ok(Map.of("message", "Property approved"));
    }

    // Reject property
    @PutMapping("/properties/{id}/reject")
    public ResponseEntity<Map<String, String>> rejectProperty(
            @PathVariable Long id,
            @RequestHeader("X-Admin-Id") Long adminId) {

        propertyService.rejectProperty(id, adminId);
        return ResponseEntity.ok(Map.of("message", "Property rejected"));
    }

    // Activate landlord
    @PutMapping("/landlords/{id}/activate")
    public ResponseEntity<Map<String, String>> activateLandlord(
            @PathVariable Long id,
            @RequestHeader("X-Admin-Id") Long adminId) {
        landlordService.activateLandlord(id, adminId);
        return ResponseEntity.ok(Map.of("message", "Landlord activated"));
    }
    // Suspend landlord
    @PutMapping("/landlords/{id}/suspend")
    public ResponseEntity<Map<String, String>> suspendLandlord(
            @PathVariable Long id,
            @RequestHeader("X-Admin-Id") Long adminId) {

        landlordService.suspendLandlord(id, adminId);
        return ResponseEntity.ok(Map.of("message", "Landlord suspended"));
    }
}




