package com.chatapp.chatapp.controller;


import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class MeetingWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /*
     * meetingId -> participants
     *
     * participantId -> participant information
     */
    private final Map<Long, Map<Long, Map<String, Object>>>
            meetingParticipants =
            new ConcurrentHashMap<>();

    public MeetingWebSocketController(
            SimpMessagingTemplate messagingTemplate) {

        this.messagingTemplate = messagingTemplate;
    }


    // =========================================================
    // JOIN MEETING
    // =========================================================

    @MessageMapping("/meeting.join")
    public void joinMeeting(
            Map<String, Object> message) {

        Long meetingId =
                getLong(message.get("meetingId"));

        Long userId =
                getLong(message.get("userId"));

        if (meetingId == null || userId == null) {
            return;
        }

        String username =
                getString(
                        message.get("username"),
                        "User"
                );

        Map<Long, Map<String, Object>> participants =
                meetingParticipants.computeIfAbsent(
                        meetingId,
                        id -> new ConcurrentHashMap<>()
                );

        Map<String, Object> participant =
                new HashMap<>();

        participant.put("userId", userId);
        participant.put("username", username);

        participants.put(userId, participant);


        /*
         * Tell the new participant about
         * users already inside.
         */
        for (
                Map.Entry<Long, Map<String, Object>> entry
                : participants.entrySet()
        ) {

            Long existingUserId =
                    entry.getKey();

            if (existingUserId.equals(userId)) {
                continue;
            }

            Map<String, Object> existing =
                    entry.getValue();

            Map<String, Object> response =
                    baseEvent(
                            "PARTICIPANT",
                            meetingId
                    );

            response.put(
                    "userId",
                    existing.get("userId")
            );

            response.put(
                    "username",
                    existing.get("username")
            );

            response.put(
                    "targetUserId",
                    userId
            );

            sendEvent(
                    meetingId,
                    response
            );
        }


        /*
         * Tell everybody that this user joined.
         */
        Map<String, Object> joinEvent =
                baseEvent(
                        "JOIN",
                        meetingId
                );

        joinEvent.put(
                "userId",
                userId
        );

        joinEvent.put(
                "username",
                username
        );

        sendEvent(
                meetingId,
                joinEvent
        );
    }


    // =========================================================
    // LEAVE MEETING
    // =========================================================

    @MessageMapping("/meeting.leave")
    public void leaveMeeting(
            Map<String, Object> message) {

        Long meetingId =
                getLong(message.get("meetingId"));

        Long userId =
                getLong(message.get("userId"));

        if (meetingId == null || userId == null) {
            return;
        }

        Map<Long, Map<String, Object>> participants =
                meetingParticipants.get(meetingId);

        if (participants != null) {

            participants.remove(userId);

            if (participants.isEmpty()) {

                meetingParticipants.remove(
                        meetingId
                );
            }
        }


        Map<String, Object> response =
                baseEvent(
                        "LEAVE",
                        meetingId
                );

        response.put(
                "userId",
                userId
        );

        response.put(
                "username",
                getString(
                        message.get("username"),
                        "User"
                )
        );

        sendEvent(
                meetingId,
                response
        );
    }


    // =========================================================
    // WEBRTC SIGNALING
    // =========================================================

    @MessageMapping("/meeting.signal")
        public void meetingSignal(
                Map<String, Object> message) {

        Long meetingId =
                getLong(message.get("meetingId"));

        Long fromUserId =
                getLong(message.get("fromUserId"));

        Long toUserId =
                getLong(message.get("toUserId"));

        if (
                meetingId == null ||
                fromUserId == null ||
                toUserId == null
        ) {
                return;
        }

        String signalType =
                getString(
                        message.get("signalType"),
                        ""
                );

        if (signalType.isBlank()) {
                return;
        }

        Map<String, Object> response =
                baseEvent(
                        "SIGNAL",
                        meetingId
                );

        // IMPORTANT:
        // Keep event type as SIGNAL.
        response.put(
                "type",
                "SIGNAL"
        );

        // Put OFFER / ANSWER / ICE separately.
        response.put(
                "signalType",
                signalType
        );

        response.put(
                "fromUserId",
                fromUserId
        );

        response.put(
                "toUserId",
                toUserId
        );

        response.put(
                "data",
                message.get("data")
        );

        response.put(
                "username",
                getString(
                        message.get("username"),
                        "User"
                )
        );

        sendEvent(
                meetingId,
                response
        );
        }


    // =========================================================
    // MEETING CHAT
    // =========================================================

    @MessageMapping("/meeting.chat")
    public void meetingChat(
            Map<String, Object> message) {

        Long meetingId =
                getLong(message.get("meetingId"));

        Long userId =
                getLong(message.get("userId"));

        if (meetingId == null || userId == null) {
            return;
        }

        String chatMessage =
                getString(
                        message.get("message"),
                        ""
                ).trim();

        if (chatMessage.isEmpty()) {
            return;
        }

        String username =
                getString(
                        message.get("username"),
                        "User"
                );


        Map<String, Object> response =
                baseEvent(
                        "CHAT",
                        meetingId
                );

        response.put(
                "userId",
                userId
        );

        response.put(
                "username",
                username
        );

        response.put(
                "message",
                chatMessage
        );


        sendEvent(
                meetingId,
                response
        );
    }


    // =========================================================
    // SCREEN SHARING
    // =========================================================

    @MessageMapping("/meeting.screen")
    public void screenShare(
            Map<String, Object> message) {

        Long meetingId =
                getLong(message.get("meetingId"));

        Long userId =
                getLong(message.get("userId"));

        if (meetingId == null || userId == null) {
            return;
        }


        Map<String, Object> response =
                baseEvent(
                        "SCREEN",
                        meetingId
                );

        response.put(
                "userId",
                userId
        );

        response.put(
                "username",
                getString(
                        message.get("username"),
                        "User"
                )
        );


        Object activeValue =
                message.get("active");

        boolean active =
                Boolean.parseBoolean(
                        String.valueOf(
                                activeValue
                        )
                );

        response.put(
                "active",
                active
        );


        sendEvent(
                meetingId,
                response
        );
    }


    // =========================================================
    // SEND EVENT
    // =========================================================

    private void sendEvent(
                Long meetingId,
                Map<String, Object> response) {

        messagingTemplate.convertAndSend(
                "/topic/meeting/" + meetingId,
                (Object) response
        );
        }

    // =========================================================
    // BASE EVENT
    // =========================================================

    private Map<String, Object> baseEvent(
            String type,
            Long meetingId) {

        Map<String, Object> event =
                new HashMap<>();

        event.put(
                "type",
                type
        );

        event.put(
                "meetingId",
                meetingId
        );

        event.put(
                "timestamp",
                LocalDateTime.now()
                        .toString()
        );

        return event;
    }


    // =========================================================
    // NUMBER CONVERSION
    // =========================================================

    private Long getLong(
            Object value) {

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        try {

            return Long.parseLong(
                    value.toString()
            );

        } catch (Exception e) {

            return null;
        }
    }


    // =========================================================
    // STRING CONVERSION
    // =========================================================

    private String getString(
            Object value,
            String defaultValue) {

        if (value == null) {
            return defaultValue;
        }

        String result =
                value.toString().trim();

        if (result.isEmpty()) {
            return defaultValue;
        }

        return result;
    }
}