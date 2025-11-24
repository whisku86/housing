package com.smart_housing.smart_housing.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "roommate_links")
@Data
public class RoommateLink {

    public enum InviteStatus { INVITED, ACCEPTED, DECLINED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id_inviter", nullable = false)
    private Long bookingIdInviter;

    @Column(name = "booking_id_invitee", nullable = false)
    private Long bookingIdInvitee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InviteStatus status = InviteStatus.INVITED;

    @Column(name = "invited_at", updatable = false)
    private LocalDateTime invitedAt;

    @PrePersist
    protected void onCreate() {
        invitedAt = LocalDateTime.now();
    }
}