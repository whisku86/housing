package com.smart_housing.smart_housing.service;

import com.smart_housing.smart_housing.model.*;
import com.smart_housing.smart_housing.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepo;
    private final RoomRepository roomRepo;
    private final PropertyRepository propertyRepo;

    @Transactional
    public Booking createBooking(Long studentId, Long propertyId, LocalDate start, LocalDate end) {

        // 1. Check if student already has a confirmed booking
        if (bookingRepo.findByStudentIdAndStatus(studentId, Booking.Status.CONFIRMED).isPresent()) {
            throw new IllegalStateException("You already have a confirmed booking.");
        }

        // 2. Find the Property first (to get max occupancy rules)
        Property property = propertyRepo.findById(propertyId)
                .orElseThrow(() -> new NoSuchElementException("Property not found"));

        // 3. AUTO-ASSIGN: Find a valid room within this property
        // We look for all rooms in this property and check which one has space.
        List<Room> rooms = roomRepo.findByPropertyId(propertyId); // You need to add this method to RoomRepository

        Room selectedRoom = null;

        for (Room room : rooms) {
            long currentBookings = bookingRepo.countByRoomIdAndStatus(room.getId(), Booking.Status.CONFIRMED);

            // If room is not full, pick it!
            if (currentBookings < property.getMaxOccupancy()) {
                selectedRoom = room;
                break;
            }
        }

        if (selectedRoom == null) {
            throw new IllegalStateException("No rooms available in this property.");
        }

        // 4. Create the Booking
        Booking b = new Booking();
        b.setStudentId(studentId);
        b.setRoomId(selectedRoom.getId()); // We save the specific Room ID
        b.setStartDate(start);
        b.setEndDate(end);
        b.setStatus(Booking.Status.CONFIRMED);

        // 5. Update Room Status (Only mark FULL if max occupancy is reached)
        long newCount = bookingRepo.countByRoomIdAndStatus(selectedRoom.getId(), Booking.Status.CONFIRMED) + 1;
        if (newCount >= property.getMaxOccupancy()) {
            selectedRoom.setStatus(RoomStatus.OCCUPIED); // Mark as full
            roomRepo.save(selectedRoom);
        }

        return bookingRepo.save(b);
    }

    public List<Booking> myBookings(Long studentId) {
        return bookingRepo.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Transactional
    public void cancelBooking(Long bookingId, Long studentId) {
        Booking b = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));

        if (!b.getStudentId().equals(studentId))
            throw new IllegalStateException("Not your booking");

        b.setStatus(Booking.Status.CANCELLED);
        bookingRepo.save(b);

        // Free up the room
        Room r = roomRepo.findById(b.getRoomId()).orElseThrow();

        // If it was full, it is now VACANT (or available)
        // Ideally, change status back to VACANT only if count < max
        if (r.getStatus() == RoomStatus.OCCUPIED) {
            r.setStatus(RoomStatus.VACANT);
            roomRepo.save(r);
        }
    }
}