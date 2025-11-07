package com.smart_housing.smart_housing.repository;


import com.smart_housing.smart_housing.model.Room;
import com.smart_housing.smart_housing.model.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByPropertyId(Long propertyId);
    List<Room> findByPropertyIdAndStatus(Long propertyId, RoomStatus status);
    boolean existsByPropertyIdAndId(Long propertyId, Long roomId);
    long countByPropertyIdAndStatus(Long propertyId, RoomStatus status);
}