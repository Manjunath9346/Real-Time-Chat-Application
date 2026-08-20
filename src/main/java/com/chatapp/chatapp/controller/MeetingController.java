package com.chatapp.chatapp.controller;

import com.chatapp.chatapp.entity.Meeting;
import com.chatapp.chatapp.service.MeetingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meetings")
@CrossOrigin
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(
            MeetingService meetingService) {

        this.meetingService =
                meetingService;
    }

    // =========================================================
    // CREATE
    // =========================================================

    @PostMapping
    public ResponseEntity<?> createMeeting(
            @RequestBody Map<String, Object> request) {

        try {

            String title =
                    (String) request.get("title");

            String dateTimeString =
                    (String) request.get("dateTime");

            String description =
                    (String) request.get("description");

            Number createdByNumber =
                    (Number) request.get("createdBy");

            Number groupIdNumber =
                    (Number) request.get("groupId");

            Number durationNumber =
                    (Number) request.get("durationMinutes");

            if (createdByNumber == null) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Creator is required"
                        ));
            }

            if (dateTimeString == null) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Date and time are required"
                        ));
            }

            Long createdBy =
                    createdByNumber.longValue();

            Long groupId =
                    groupIdNumber == null
                            ? null
                            : groupIdNumber.longValue();

            Integer duration =
                    durationNumber == null
                            ? 60
                            : durationNumber.intValue();

            LocalDateTime dateTime =
                    LocalDateTime.parse(
                            dateTimeString
                    );

            Meeting meeting =
                    meetingService.createMeeting(
                            title,
                            dateTime,
                            description,
                            createdBy,
                            groupId,
                            duration
                    );

            return ResponseEntity.ok(meeting);

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // USER MEETINGS
    // =========================================================

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserMeetings(
            @PathVariable Long userId) {

        try {

            return ResponseEntity.ok(
                    meetingService.getUserMeetings(
                            userId
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // REFRESH USER MEETING STATUS
    // =========================================================

    @PostMapping("/user/{userId}/refresh")
    public ResponseEntity<?> refreshUserMeetings(
            @PathVariable Long userId) {

        try {

            return ResponseEntity.ok(
                    meetingService
                            .refreshUserMeetings(
                                    userId
                            )
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // GROUP MEETINGS
    // =========================================================

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getGroupMeetings(
            @PathVariable Long groupId) {

        try {

            return ResponseEntity.ok(
                    meetingService
                            .getGroupMeetings(
                                    groupId
                            )
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // GET MEETING
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<?> getMeeting(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    meetingService.getMeeting(id)
            );

        } catch (Exception e) {

            return ResponseEntity.notFound()
                    .build();
        }
    }

    // =========================================================
    // GET BY CODE
    // =========================================================

    @GetMapping("/code/{code}")
    public ResponseEntity<?> getByCode(
            @PathVariable String code) {

        try {

            return ResponseEntity.ok(
                    meetingService.getByCode(code)
            );

        } catch (Exception e) {

            return ResponseEntity.notFound()
                    .build();
        }
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMeeting(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        try {

            String title =
                    (String) request.get("title");

            String dateTimeString =
                    (String) request.get("dateTime");

            String description =
                    (String) request.get("description");

            Number durationNumber =
                    (Number) request.get(
                            "durationMinutes"
                    );

            Number requesterNumber =
                    (Number) request.get(
                            "requesterId"
                    );

            if (requesterNumber == null) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Requester is required"
                        ));
            }

            LocalDateTime dateTime =
                    LocalDateTime.parse(
                            dateTimeString
                    );

            Integer duration =
                    durationNumber == null
                            ? 60
                            : durationNumber.intValue();

            Meeting meeting =
                    meetingService.updateMeeting(
                            id,
                            title,
                            dateTime,
                            description,
                            duration,
                            requesterNumber.longValue()
                    );

            return ResponseEntity.ok(meeting);

        } catch (Exception e) {

            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // DELETE
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMeeting(
            @PathVariable Long id,
            @RequestParam Long requesterId) {

        try {

            meetingService.deleteMeeting(
                    id,
                    requesterId
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Meeting deleted successfully"
                    )
            );

        } catch (Exception e) {

            return ResponseEntity.status(403)
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }

    // =========================================================
    // UPDATE STATUS
    // =========================================================

    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    meetingService
                            .updateMeetingStatus(id)
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            e.getMessage()
                    ));
        }
    }
}