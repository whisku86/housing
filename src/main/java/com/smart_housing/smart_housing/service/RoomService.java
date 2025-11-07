package com.smart_housing.smart_housing.service;



import com.smart_housing.smart_housing.dto.BulkRoomRequest;

import com.smart_housing.smart_housing.dto.RoomRequest;
import com.smart_housing.smart_housing.model.Room;
import com.smart_housing.smart_housing.model.RoomStatus;
import com.smart_housing.smart_housing.repository.PropertyRepository;
import com.smart_housing.smart_housing.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final PropertyRepository propertyRepository;

    public RoomService(RoomRepository roomRepository, PropertyRepository propertyRepository) {
        this.roomRepository = roomRepository;
        this.propertyRepository = propertyRepository;
    }

    // Verify that property belongs to landlord
    private void verifyPropertyOwnership(Long propertyId, Long landlordId) {
        if (!propertyRepository.existsByIdAndLandlordId(propertyId, landlordId)) {
            throw new NoSuchElementException("Property not found or access denied");
        }
    }

    public List<Room> getRoomsByProperty(Long propertyId, Long landlordId) {
        verifyPropertyOwnership(propertyId, landlordId);
        return roomRepository.findByPropertyId(propertyId);
    }

    public Room getRoomById(Long roomId, Long propertyId, Long landlordId) {
        verifyPropertyOwnership(propertyId, landlordId);
        return roomRepository.findById(roomId)
                .filter(r -> r.getPropertyId().equals(propertyId))
                .orElseThrow(() -> new NoSuchElementException("Room not found"));
    }

    @Transactional
    public Room createRoom(RoomRequest request, Long propertyId, Long landlordId) {
        verifyPropertyOwnership(propertyId, landlordId);

        Room room = new Room();
        room.setPropertyId(propertyId);
        room.setRoomNumber(request.getRoomNumber());
        room.setPrice(request.getPrice());
        room.setStatus(request.getStatus());
        return roomRepository.save(room);
    }

    @Transactional
    public Room updateRoom(Long roomId, RoomRequest request, Long propertyId, Long landlordId) {
        Room room = getRoomById(roomId, propertyId, landlordId);
        room.setRoomNumber(request.getRoomNumber());
        room.setPrice(request.getPrice());
        room.setStatus(request.getStatus());
        return roomRepository.save(room);
    }

    @Transactional
    public List<Room> createRoomsBulk(BulkRoomRequest request, Long propertyId, Long landlordId) {
        verifyPropertyOwnership(propertyId, landlordId);

        List<Room> rooms = new ArrayList<>();
        String prefix = request.getRoomNumberPrefix(); // e.g., "10"
        String startSuffix = request.getRoomNumberStart(); // e.g., "1"

        try {
            int start = Integer.parseInt(startSuffix);
            // Numeric suffix: 101, 102, 103...
            for (int i = 0; i < request.getCount(); i++) {
                Room room = new Room();
                room.setPropertyId(propertyId);
                room.setRoomNumber(prefix + (start + i));
                room.setPrice(request.getPrice());
                room.setStatus(request.getStatus());
                rooms.add(room);
            }
        } catch (NumberFormatException e) {
            // Alphanumeric suffix: A1, A2... or BlockA-1, BlockA-2...
            for (int i = 1; i <= request.getCount(); i++) {
                Room room = new Room();
                room.setPropertyId(propertyId);
                room.setRoomNumber(prefix + startSuffix + i);
                room.setPrice(request.getPrice());
                room.setStatus(request.getStatus());
                rooms.add(room);
            }
        }

        return roomRepository.saveAll(rooms);
    }

    @Transactional
    public void deleteRoom(Long roomId, Long propertyId, Long landlordId) {
        if (!roomRepository.existsByPropertyIdAndId(propertyId, roomId)) {
            throw new NoSuchElementException("Room not found or access denied");
        }
        roomRepository.deleteById(roomId);
    }

    public List<Room> getVacantRoomsByProperty(Long propertyId, Long landlordId) {
        verifyPropertyOwnership(propertyId, landlordId);
        return roomRepository.findByPropertyIdAndStatus(propertyId, RoomStatus.VACANT);
    }
}