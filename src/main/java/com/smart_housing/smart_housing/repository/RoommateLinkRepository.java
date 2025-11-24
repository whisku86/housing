package com.smart_housing.smart_housing.repository;

import com.smart_housing.smart_housing.model.RoommateLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RoommateLinkRepository extends JpaRepository<RoommateLink, Long> {

    List<RoommateLink> findByBookingIdInviter(Long id);

    Optional<RoommateLink> findByBookingIdInviterAndBookingIdInvitee(Long a, Long b);
}
