package com.chatapp.chatapp.service;

import com.chatapp.chatapp.entity.ChatGroup;
import com.chatapp.chatapp.entity.Meeting;
import com.chatapp.chatapp.entity.User;
import com.chatapp.chatapp.repository.ChatGroupRepository;
import com.chatapp.chatapp.repository.MeetingRepository;
import com.chatapp.chatapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final ChatGroupRepository groupRepository;

    public MeetingService(
            MeetingRepository meetingRepository,
            UserRepository userRepository,
            ChatGroupRepository groupRepository) {

        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    // =========================================================
    // CREATE MEETING
    // =========================================================

    @Transactional
    public Meeting createMeeting(
            String title,
            LocalDateTime dateTime,
            String description,
            Long createdBy,
            Long groupId,
            Integer durationMinutes) {

        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException(
                    "Meeting title is required"
            );
        }

        if (dateTime == null) {
            throw new RuntimeException(
                    "Meeting date and time are required"
            );
        }

        if (createdBy == null) {
            throw new RuntimeException(
                    "Creator is required"
            );
        }

        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException(
                    "Meeting date and time must be in the future"
            );
        }

        if (durationMinutes == null ||
                durationMinutes <= 0) {

            durationMinutes = 60;
        }

        if (durationMinutes > 1440) {
            throw new RuntimeException(
                    "Meeting duration cannot exceed 24 hours"
            );
        }

        User creator = userRepository
                .findById(createdBy)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Creator user not found"
                        )
                );

        // =====================================================
        // GROUP MEETING
        // =====================================================

        if (groupId != null) {

            ChatGroup group = groupRepository
                    .findById(groupId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Group not found"
                            )
                    );

            boolean member = group
                    .getMembers()
                    .stream()
                    .anyMatch(user ->
                            user.getId()
                                    .equals(createdBy)
                    );

            if (!member) {
                throw new RuntimeException(
                        "You must be a group member to schedule a group meeting"
                );
            }
        }

        Meeting meeting = new Meeting();

        meeting.setTitle(title.trim());
        meeting.setDateTime(dateTime);

        meeting.setDescription(
                description == null
                        ? ""
                        : description.trim()
        );

        meeting.setCreatedBy(createdBy);
        meeting.setGroupId(groupId);

        meeting.setDurationMinutes(
                durationMinutes
        );

        meeting.setMeetingCode(
                generateMeetingCode()
        );

        meeting.setStatus("UPCOMING");

        return meetingRepository.save(meeting);
    }

    // =========================================================
    // GENERATE UNIQUE MEETING CODE
    // =========================================================

    private String generateMeetingCode() {

        String code;

        do {

            code = String.format(
                    "%08d",
                    new Random().nextInt(100000000)
            );

        } while (
                meetingRepository
                        .findByMeetingCode(code)
                        .isPresent()
        );

        return code;
    }

    // =========================================================
    // GET USER MEETINGS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Meeting> getUserMeetings(
            Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        List<ChatGroup> groups =
                groupRepository.findByMembers_Id(userId);

        List<Long> groupIds =
                groups.stream()
                        .map(ChatGroup::getId)
                        .toList();

        if (groupIds.isEmpty()) {

            return meetingRepository
                    .findByCreatedByOrderByDateTimeAsc(
                            userId
                    );
        }

        return meetingRepository.findUserMeetings(
                userId,
                groupIds
        );
    }

    // =========================================================
    // GET GROUP MEETINGS
    // =========================================================

    @Transactional(readOnly = true)
    public List<Meeting> getGroupMeetings(
            Long groupId) {

        return meetingRepository
                .findByGroupIdOrderByDateTimeAsc(
                        groupId
                );
    }

    // =========================================================
    // GET SINGLE MEETING
    // =========================================================

    @Transactional(readOnly = true)
    public Meeting getMeeting(Long id) {

        return meetingRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Meeting not found"
                        )
                );
    }

    // =========================================================
    // GET BY CODE
    // =========================================================

    @Transactional(readOnly = true)
    public Meeting getByCode(String code) {

        if (code == null ||
                code.trim().isEmpty()) {

            throw new RuntimeException(
                    "Meeting code is required"
            );
        }

        return meetingRepository
                .findByMeetingCode(
                        code.trim().toUpperCase()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Meeting not found"
                        )
                );
    }

    // =========================================================
    // DELETE MEETING
    // =========================================================

    @Transactional
    public void deleteMeeting(
            Long meetingId,
            Long requesterId) {

        Meeting meeting =
                getMeeting(meetingId);

        if (!meeting.getCreatedBy()
                .equals(requesterId)) {

            throw new RuntimeException(
                    "Only the meeting creator can delete it"
            );
        }

        meetingRepository.delete(meeting);
    }

    // =========================================================
    // UPDATE MEETING
    // =========================================================

    @Transactional
    public Meeting updateMeeting(
            Long meetingId,
            String title,
            LocalDateTime dateTime,
            String description,
            Integer durationMinutes,
            Long requesterId) {

        Meeting meeting =
                getMeeting(meetingId);

        if (!meeting.getCreatedBy()
                .equals(requesterId)) {

            throw new RuntimeException(
                    "Only the meeting creator can edit it"
            );
        }

        if (meeting.getStatus()
                .equals("COMPLETED")) {

            throw new RuntimeException(
                    "Completed meetings cannot be edited"
            );
        }

        if (title == null ||
                title.trim().isEmpty()) {

            throw new RuntimeException(
                    "Meeting title is required"
            );
        }

        if (dateTime == null ||
                dateTime.isBefore(LocalDateTime.now())) {

            throw new RuntimeException(
                    "Meeting date and time must be in the future"
            );
        }

        if (durationMinutes == null ||
                durationMinutes <= 0) {

            durationMinutes = 60;
        }

        meeting.setTitle(title.trim());
        meeting.setDateTime(dateTime);

        meeting.setDescription(
                description == null
                        ? ""
                        : description.trim()
        );

        meeting.setDurationMinutes(
                durationMinutes
        );

        meeting.setStatus("UPCOMING");

        return meetingRepository.save(meeting);
    }

    // =========================================================
    // UPDATE STATUS
    // =========================================================

    @Transactional
    public Meeting updateMeetingStatus(
            Long meetingId) {

        Meeting meeting =
                getMeeting(meetingId);

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime start =
                meeting.getDateTime();

        LocalDateTime end =
                start.plusMinutes(
                        meeting.getDurationMinutes()
                );

        if (now.isBefore(start)) {

            meeting.setStatus(
                    "UPCOMING"
            );

        } else if (
                !now.isBefore(start)
                        &&
                now.isBefore(end)
        ) {

            meeting.setStatus(
                    "LIVE"
            );

        } else {

            meeting.setStatus(
                    "COMPLETED"
            );
        }

        return meetingRepository.save(meeting);
    }

    // =========================================================
    // UPDATE ALL STATUSES FOR USER
    // =========================================================

    @Transactional
    public List<Meeting> refreshUserMeetings(
            Long userId) {

        List<Meeting> meetings =
                getUserMeetings(userId);

        List<Meeting> result =
                new ArrayList<>();

        for (Meeting meeting : meetings) {

            result.add(
                    updateMeetingStatus(
                            meeting.getId()
                    )
            );
        }

        return result;
    }
}