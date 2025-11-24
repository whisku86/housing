package com.smart_housing.smart_housing.service;

import com.smart_housing.smart_housing.model.*;
import com.smart_housing.smart_housing.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RoommateService {

    private final RoommateLinkRepository linkRepo;
    private final BookingRepository bookingRepo;
    private final RoomRepository roomRepo;
    private final PropertyRepository propertyRepo;

    @Transactional
    public RoommateLink invite(Long inviterBookingId, Long inviteeStudentId) {
        Booking inviter = bookingRepo.findById(inviterBookingId)
                .orElseThrow(() -> new NoSuchElementException("Inviter booking not found"));
        if (inviter.getStatus() != Booking.Status.CONFIRMED)
            throw new IllegalStateException("Only confirmed bookings can invite");

        // invitee must not have another active booking
        if (bookingRepo.findByStudentIdAndStatus(inviteeStudentId, Booking.Status.CONFIRMED).isPresent())
            throw new IllegalStateException("Invitee already has a confirmed booking");

        Room room = roomRepo.findById(inviter.getRoomId()).orElseThrow();
        Property prop = propertyRepo.findById(room.getPropertyId()).orElseThrow();

        long occupants = bookingRepo.countByRoomIdAndStatus(room.getId(), Booking.Status.CONFIRMED);
        if (occupants >= prop.getMaxOccupancy())
            throw new IllegalStateException("Room already full");

        // create mirror booking for invitee (same room, same dates)
        Booking inviteeBooking = new Booking();
        inviteeBooking.setStudentId(inviteeStudentId);
        inviteeBooking.setRoomId(inviter.getRoomId());
        inviteeBooking.setStartDate(inviter.getStartDate());
        inviteeBooking.setEndDate(inviter.getEndDate());
        inviteeBooking.setStatus(Booking.Status.CONFIRMED);
        inviteeBooking = bookingRepo.save(inviteeBooking);

        RoommateLink link = new RoommateLink();
        link.setBookingIdInviter(inviter.getId());
        link.setBookingIdInvitee(inviteeBooking.getId());
        return linkRepo.save(link);
    }

    @Transactional
    public void accept(Long linkId) {
        RoommateLink link = linkRepo.findById(linkId)
                .orElseThrow(() -> new NoSuchElementException("Invitation not found"));
        if (link.getStatus() != RoommateLink.InviteStatus.INVITED)
            throw new IllegalStateException("Invitation already processed");
        link.setStatus(RoommateLink.InviteStatus.ACCEPTED);
        linkRepo.save(link);
    }

    public List<RoommateLink> sentInvites(Long studentId) {
        return bookingRepo.findByStudentIdOrderByCreatedAtDesc(studentId)
                .stream()
                .map(b -> linkRepo.findByBookingIdInviter(b.getId()))
                .findFirst()
                .orElse(List.of());
    }
}