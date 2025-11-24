package com.smart_housing.smart_housing.Controller;

import com.smart_housing.smart_housing.model.RoommateLink;
import com.smart_housing.smart_housing.service.RoommateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/roommate")
@RequiredArgsConstructor
@CrossOrigin
public class RoommateController {

    private final RoommateService roommateService;

    @PostMapping("/invite")
    public ResponseEntity<RoommateLink> invite(@RequestParam Long bookingId,
                                               @RequestParam String inviteeEmail,
                                               @AuthenticationPrincipal UserDetails user) {
        Long inviterId = Long.valueOf(user.getUsername());
        // look up invitee studentId by email (quick StudentRepository method)
        Long inviteeId = 1L; // TODO StudentRepository.findIdByEmail(inviteeEmail)
        return ResponseEntity.ok(roommateService.invite(bookingId, inviteeId));
    }

    @PatchMapping("/invitations/{id}/accept")
    public ResponseEntity<Void> accept(@PathVariable Long id) {
        roommateService.accept(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/invitations/sent")
    public ResponseEntity<List<RoommateLink>> sent(@AuthenticationPrincipal UserDetails user) {
        Long studentId = Long.valueOf(user.getUsername());
        return ResponseEntity.ok(roommateService.sentInvites(studentId));
    }
}
