package com.smart_housing.smart_housing.Controller;

import com.smart_housing.smart_housing.model.Booking;
import com.smart_housing.smart_housing.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/student/bookings")
@RequiredArgsConstructor
@CrossOrigin
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestParam Long roomId,
                                                 @RequestParam LocalDate start,
                                                 @RequestParam LocalDate end,
                                                 @AuthenticationPrincipal UserDetails user) {
        Long studentId = extractStudentId(user);
        Booking booking = bookingService.createBooking(studentId, roomId, start, end);
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getMyBookings(@AuthenticationPrincipal UserDetails user) {
        Long studentId = extractStudentId(user);
        List<Booking> bookings = bookingService.myBookings(studentId);
        return ResponseEntity.ok(bookings);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id,
                                              @AuthenticationPrincipal UserDetails user) {
        Long studentId = extractStudentId(user);
        bookingService.cancelBooking(id, studentId);
        return ResponseEntity.noContent().build();
    }

    private Long extractStudentId(UserDetails user) {
        if (user == null || user.getUsername() == null) {
            throw new IllegalArgumentException("Authenticated user is missing or invalid");
        }
        return Long.valueOf(user.getUsername()); // Assumes JWT subject is studentId
    }
}
