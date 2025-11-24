package com.smart_housing.smart_housing.repository;

import com.smart_housing.smart_housing.model.Booking;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByStudentIdAndStatus(Long studentId, Booking.Status status);

    List<Booking> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<Booking> findByRoomId(Long roomId);

    long countByRoomIdAndStatus(Long roomId, Booking.Status status);
}