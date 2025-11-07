package com.smart_housing.smart_housing.repository;


import com.smart_housing.smart_housing.model.Property;
import com.smart_housing.smart_housing.model.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByLandlordId(Long landlordId);
    boolean existsByIdAndLandlordId(Long id, Long landlordId);

    // New methods for public access
    List<Property> findByType(PropertyType type);
    List<Property> findByLocationContainingIgnoreCase(String location);
}