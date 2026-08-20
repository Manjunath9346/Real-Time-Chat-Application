package com.chatapp.chatapp.controller;

import com.chatapp.chatapp.entity.ChatGroup;
import com.chatapp.chatapp.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<?> createGroup(
            @RequestParam String name,
            @RequestParam Long creatorId) {

        try {
            return ResponseEntity.ok(
                    groupService.createGroup(name, creatorId)
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserGroups(
            @PathVariable Long userId) {

        try {
            List<ChatGroup> groups =
                    groupService.getUserGroups(userId);

            return ResponseEntity.ok(groups);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ============================
    // GROUP INFO
    // ============================

    @GetMapping("/{groupId}/info")
    public ResponseEntity<?> getGroupInfo(
            @PathVariable Long groupId) {

        try {
            return ResponseEntity.ok(
                    groupService.getGroupInfo(groupId)
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ============================
    // ADD MEMBER
    // ============================

    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<?> addMember(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestParam Long requesterId) {

        try {
            return ResponseEntity.ok(
                    groupService.addMember(
                            groupId,
                            userId,
                            requesterId
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ============================
    // REMOVE MEMBER
    // ============================

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<?> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestParam Long requesterId) {

        try {
            return ResponseEntity.ok(
                    groupService.removeMember(
                            groupId,
                            userId,
                            requesterId
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ============================
    // DELETE GROUP
    // ============================

    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(
            @PathVariable Long groupId,
            @RequestParam Long requesterId) {

        try {

            groupService.deleteGroup(
                    groupId,
                    requesterId
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Group deleted successfully"
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

    // ============================
    // LEAVE GROUP
    // ============================

    @DeleteMapping("/{groupId}/leave/{userId}")
    public ResponseEntity<?> leaveGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId) {

        try {

            groupService.leaveGroup(
                    groupId,
                    userId
            );

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Left group successfully"
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
}   