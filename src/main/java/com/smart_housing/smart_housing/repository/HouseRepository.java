package com.smart_housing.smart_housing.repository;

import com.smart_housing.smart_housing.model.House;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HouseRepository extends JpaRepository<House, Integer> {
    List<House> findByLandlordId(int landlordId);
    List<House> findByStatus(House.Status status);
    List<House> findByLocationContainingIgnoreCase(String location);
}
