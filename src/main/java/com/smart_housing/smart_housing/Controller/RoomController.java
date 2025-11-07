package com.smart_housing.smart_housing.Controller;

import com.smart_housing.smart_housing.dto.BulkRoomRequest;
import com.smart_housing.smart_housing.dto.RoomRequest;
import com.smart_housing.smart_housing.model.Room;
import com.smart_housing.smart_housing.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/landlord/properties/{propertyId}/rooms")
@CrossOrigin(origins = {"http://127.0.0.1:8080", "http://localhost:8080"})
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms(
            @PathVariable Long propertyId,
            @RequestHeader("X-Landlord-Id") Long landlordId) {
        List<Room> rooms = roomService.getRoomsByProperty(propertyId, landlordId);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/vacant")
    public ResponseEntity<List<Room>> getVacantRooms(
            @PathVariable Long propertyId,
            @RequestHeader("X-Landlord-Id") Long landlordId) {
        List<Room> rooms = roomService.getVacantRoomsByProperty(propertyId, landlordId);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<Room> getRoom(
            @PathVariable Long propertyId,
            @PathVariable Long roomId,
            @RequestHeader("X-Landlord-Id") Long landlordId) {
        Room room = roomService.getRoomById(roomId, propertyId, landlordId);
        return ResponseEntity.ok(room);
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(
            @PathVariable Long propertyId,
            @Valid @RequestBody RoomRequest request,
            @RequestHeader("X-Landlord-Id") Long landlordId) {
        Room saved = roomService.createRoom(request, propertyId, landlordId);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<Room> updateRoom(
            @PathVariable Long propertyId,
            @PathVariable Long roomId,
            @Valid @RequestBody RoomRequest request,
            @RequestHeader("X-Landlord-Id") Long landlordId) {
        Room updated = roomService.updateRoom(roomId, request, propertyId, landlordId);
        return ResponseEntity.ok(updated);
    }
    @PostMapping("/bulk")
    public ResponseEntity<List<Room>> createRoomsBulk(
            @PathVariable Long propertyId,
            @Valid @RequestBody BulkRoomRequest request,
            @RequestHeader("X-Landlord-Id") Long landlordId) {
        List<Room> rooms = roomService.createRoomsBulk(request, propertyId, landlordId);
        return ResponseEntity.ok(rooms);
    }
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @PathVariable Long propertyId,
            @PathVariable Long roomId,
            @RequestHeader("X-Landlord-Id") Long landlordId) {
        roomService.deleteRoom(roomId, propertyId, landlordId);
        return ResponseEntity.noContent().build();
    }
}