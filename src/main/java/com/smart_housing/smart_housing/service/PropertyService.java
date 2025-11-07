package com.smart_housing.smart_housing.service;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart_housing.smart_housing.dto.PropertyRequest;
import com.smart_housing.smart_housing.model.Property;
import com.smart_housing.smart_housing.model.PropertyType;
import com.smart_housing.smart_housing.model.RoomStatus;
import com.smart_housing.smart_housing.repository.RoomRepository;
import com.smart_housing.smart_housing.repository.PropertyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PropertyService(PropertyRepository propertyRepository, RoomRepository roomRepository) {
        this.propertyRepository = propertyRepository;
        this.roomRepository = roomRepository; // ← INJECT IT
    }

    public boolean isPropertyOwnedByLandlord(Long propertyId, Long landlordId) {
        return propertyRepository.existsByIdAndLandlordId(propertyId, landlordId);
    }

    public List<Property> getAllPropertiesByLandlord(Long landlordId) {
        return propertyRepository.findByLandlordId(landlordId);
    }

    public Property getPropertyByIdAndLandlord(Long id, Long landlordId) {
        return propertyRepository.findById(id)
                .filter(p -> p.getLandlordId().equals(landlordId))
                .orElseThrow(() -> new NoSuchElementException("Property not found or access denied"));
    }
    public long getVacantRoomCount(Long propertyId) {
        return roomRepository.countByPropertyIdAndStatus(propertyId, RoomStatus.VACANT);
    }


        // Get all properties for public viewing
        public List<Property> getAllPublicProperties() {
            return propertyRepository.findAll();
        }

        // Get properties by type
        public List<Property> getPropertiesByType(PropertyType type) {
            return propertyRepository.findByType(type);
        }

        // Get single property by ID (public view)
        public Property getPropertyById(Long id) {
            return propertyRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Property not found"));
        }

        // Search properties by location
        public List<Property> searchByLocation(String location) {
            return propertyRepository.findByLocationContainingIgnoreCase(location);
        }

    @Transactional
    public Property createProperty(PropertyRequest request, Long landlordId) {
        Property property = new Property();
        property.setLandlordId(landlordId);
        property.setName(request.getName());
        property.setLocation(request.getLocation());
        property.setType(request.getType());
        property.setMaxOccupancy(request.getMaxOccupancy());
        property.setPrice(request.getPrice());
        property.setBills(request.getBills());
        property.setSecurityDetails(request.getSecurityDetails());
        property.setAmenities(request.getAmenities());


        return propertyRepository.save(property);
    }

    @Transactional
    public Property updateProperty(Long id, PropertyRequest request, Long landlordId) {
        Property property = getPropertyByIdAndLandlord(id, landlordId);
        property.setName(request.getName());
        property.setLocation(request.getLocation());
        property.setType(request.getType());
        property.setMaxOccupancy(request.getMaxOccupancy());
        property.setPrice(request.getPrice());
        property.setBills(request.getBills());
        property.setSecurityDetails(request.getSecurityDetails());
        property.setAmenities(request.getAmenities());


        return propertyRepository.save(property);
    }

    @Transactional
    public void deleteProperty(Long id, Long landlordId) {
        if (!propertyRepository.existsByIdAndLandlordId(id, landlordId)) {
            throw new NoSuchElementException("Property not found or access denied");
        }
        propertyRepository.deleteById(id);
    }




    @Transactional
    public void addImages(Long propertyId, List<String> newImageUrls) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new NoSuchElementException("Property not found"));

        try {
            // Get existing images
            List<String> existingImages = new ArrayList<>();
            if (property.getImages() != null && !property.getImages().isEmpty()) {
                existingImages = objectMapper.readValue(property.getImages(), List.class);
            }

            // Add new images
            existingImages.addAll(newImageUrls);

            // Save back as JSON
            property.setImages(objectMapper.writeValueAsString(existingImages));
            propertyRepository.save(property);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update images", e);
        }
    }

    @Transactional
    public void removeImage(Long propertyId, Long landlordId, String imageUrl) {
        Property property = getPropertyByIdAndLandlord(propertyId, landlordId);

        try {
            if (property.getImages() != null && !property.getImages().isEmpty()) {
                List<String> images = objectMapper.readValue(property.getImages(), List.class);
                images.remove(imageUrl);
                property.setImages(objectMapper.writeValueAsString(images));
                propertyRepository.save(property);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove image", e);
        }
    }
}
