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
    public Booking createBooking(Long studentId, Long roomId, LocalDate start, LocalDate end) {
        // 1 student – 1 active booking rule
        if (bookingRepo.findByStudentIdAndStatus(studentId, Booking.Status.CONFIRMED).isPresent())
            throw new IllegalStateException("You already have a confirmed booking.");

        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("Room not found"));

        if (room.getStatus() != RoomStatus.VACANT)
            throw new IllegalStateException("Room is not available");

        Property property = propertyRepo.findById(room.getPropertyId())
                .orElseThrow();

        // occupancy check
        long current = bookingRepo.countByRoomIdAndStatus(roomId, Booking.Status.CONFIRMED);
        if (current >= property.getMaxOccupancy())
            throw new IllegalStateException("Room already at max occupancy");

        Booking b = new Booking();
        b.setStudentId(studentId);
        b.setRoomId(roomId);
        b.setStartDate(start);
        b.setEndDate(end);
        b.setStatus(Booking.Status.CONFIRMED);   // skip payment for MVP
        room.setStatus(RoomStatus.OCCUPIED);
        roomRepo.save(room);
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

        // free the room if no other confirmed booking
        if (bookingRepo.countByRoomIdAndStatus(b.getRoomId(), Booking.Status.CONFIRMED) == 0) {
            Room r = roomRepo.findById(b.getRoomId()).orElseThrow();
            r.setStatus(RoomStatus.VACANT);
            roomRepo.save(r);
        }
    }
}