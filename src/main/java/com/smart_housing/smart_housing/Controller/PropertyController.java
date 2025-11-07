package com.smart_housing.smart_housing.Controller;



import com.smart_housing.smart_housing.dto.PropertyRequest;
import com.smart_housing.smart_housing.model.Property;
import com.smart_housing.smart_housing.model.PropertyType;
import com.smart_housing.smart_housing.service.FileStorageService;
import com.smart_housing.smart_housing.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/landlord")
@CrossOrigin(origins = {"http://127.0.0.1:8080", "http://localhost:8080"})
public class PropertyController {

    private final PropertyService propertyService;
    private final FileStorageService fileStorageService;


    public PropertyController(PropertyService propertyService, FileStorageService fileStorageService) {
        this.propertyService = propertyService;
        this.fileStorageService = fileStorageService;
    }
    // Helper to get landlordId from header (temporary)
    private Long getLandlordIdFromHeader(@RequestHeader("X-Landlord-Id") Long landlordId) {
        return landlordId;
    }
    private void verifyPropertyOwnership(Long propertyId, Long landlordId) {
        if (!propertyService.isPropertyOwnedByLandlord(propertyId, landlordId)) {
            throw new RuntimeException("Property not found or access denied");
        }
    }

    @GetMapping("/properties")
    public ResponseEntity<List<Property>> getAllProperties(@RequestHeader("X-Landlord-Id") Long landlordId) {
        List<Property> properties = propertyService.getAllPropertiesByLandlord(landlordId);
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/properties/{id}")
    public ResponseEntity<Property> getProperty(
            @PathVariable Long id,
            @RequestHeader("X-Landlord-Id") Long landlordId) {
        Property property = propertyService.getPropertyByIdAndLandlord(id, landlordId);
        return ResponseEntity.ok(property);
    }
    @GetMapping("/properties/{id}/vacant-count")
    public ResponseEntity<Long> getVacantRoomCount(
            @PathVariable Long id,
            @RequestHeader("X-Landlord-Id") Long landlordId) {
        verifyPropertyOwnership(id, landlordId); // ensure landlord owns property
        long count = propertyService.getVacantRoomCount(id);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/properties")
    public ResponseEntity<Property> createProperty(
            @Valid @RequestBody PropertyRequest request,
            @RequestHeader("X-Landlord-Id") Long landlordId) {

        System.out.println("=== RECEIVED PROPERTY REQUEST ===");
        System.out.println("Name: " + request.getName());
        System.out.println("Location: " + request.getLocation());
        System.out.println("Type: " + request.getType());
        System.out.println("Bills: " + request.getBills());
        System.out.println("Amenities: " + request.getAmenities());
        System.out.println("Security Details: " + request.getSecurityDetails());

        try {
            Property saved = propertyService.createProperty(request, landlordId);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            System.err.println("=== ERROR CREATING PROPERTY ===");
            e.printStackTrace();
            throw e;
        }
    }

    @PutMapping("/properties/{id}")
    public ResponseEntity<Property> updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody PropertyRequest request,
            @RequestHeader("X-Landlord-Id") Long landlordId) {
        Property updated = propertyService.updateProperty(id, request, landlordId);
        return ResponseEntity.ok(updated);
    }

    // Upload images for a property
    @PostMapping("/properties/{id}/images")
    public ResponseEntity<?> uploadPropertyImages(
            @PathVariable Long id,
            @RequestParam("images") MultipartFile[] files,
            @RequestHeader("X-Landlord-Id") Long landlordId) {

        try {
            // Verify ownership
            Property property = propertyService.getPropertyByIdAndLandlord(id, landlordId);

            // Store uploaded files
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                // Validate image type
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Only image files are allowed"));
                }

                String filename = fileStorageService.storeFile(file);
                imageUrls.add("/uploads/properties/" + filename);
            }

            // Update property with new image URLs
            propertyService.addImages(id, imageUrls);

            return ResponseEntity.ok(Map.of(
                    "message", "Images uploaded successfully",
                    "images", imageUrls
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Failed to upload images: " + e.getMessage()));
        }
    }

    // Delete a specific image
    @DeleteMapping("/properties/{id}/images")
    public ResponseEntity<?> deletePropertyImage(
            @PathVariable Long id,
            @RequestParam("imageUrl") String imageUrl,
            @RequestHeader("X-Landlord-Id") Long landlordId) {

        try {
            propertyService.removeImage(id, landlordId, imageUrl);

            // Extract filename and delete file
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            fileStorageService.deleteFile(filename);

            return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Failed to delete image: " + e.getMessage()));
        }
    }


    @DeleteMapping("/properties/{id}")
    public ResponseEntity<Void> deleteProperty(
            @PathVariable Long id,
            @RequestHeader("X-Landlord-Id") Long landlordId) {
        propertyService.deleteProperty(id, landlordId);
        return ResponseEntity.noContent().build();
    }
}