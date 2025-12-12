package com.smart_housing.smart_housing.Controller;

import com.smart_housing.smart_housing.model.Booking;
import com.smart_housing.smart_housing.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/booking") // FIXED: Matches your Frontend logic
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Allows frontend to access this
public class BookingController {

    private final BookingService bookingService;

    // 1. CREATE BOOKING (Fixed to accept JSON Body)
    @PostMapping("/book")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        try {
            // We use the ID sent from the frontend (simpler and less prone to errors)
            Booking booking = bookingService.createBooking(
                    request.studentId,
                    request.propertyId,
                    request.startDate,
                    request.endDate
            );
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // 2. GET BOOKINGS FOR A STUDENT
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Booking>> getMyBookings(@PathVariable Long studentId) {
        List<Booking> bookings = bookingService.myBookings(studentId);
        return ResponseEntity.ok(bookings);
    }

    // 3. CANCEL BOOKING
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id, @RequestParam Long studentId) {
        bookingService.cancelBooking(id, studentId);
        return ResponseEntity.noContent().build();
    }

    // DTO Helper Class
    public static class BookingRequest {
        public Long studentId;
        public Long propertyId;
        public LocalDate startDate;
        public LocalDate endDate;
    }
}