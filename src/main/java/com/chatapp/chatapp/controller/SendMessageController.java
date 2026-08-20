package com.chatapp.chatapp.controller;

import com.chatapp.chatapp.entity.Message;
import com.chatapp.chatapp.repository.MessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/send-message")
@CrossOrigin
public class SendMessageController {

    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public SendMessageController(
            MessageRepository messageRepository,
            SimpMessagingTemplate messagingTemplate) {

        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public ResponseEntity<?> sendMessage(
            @RequestBody Message message) {

        if (message.getSenderId() == null) {
            return ResponseEntity.badRequest()
                    .body("senderId is required");
        }

        if (message.getReceiverId() == null &&
                message.getGroupId() == null) {

            return ResponseEntity.badRequest()
                    .body("receiverId or groupId is required");
        }

        boolean hasText =
                message.getContent() != null &&
                !message.getContent().trim().isEmpty();

        boolean hasFile =
                message.getFileUrl() != null &&
                !message.getFileUrl().trim().isEmpty();

        if (!hasText && !hasFile) {

            return ResponseEntity.badRequest()
                    .body("Message cannot be empty");
        }

        message.setId(null);

        Message saved =
                messageRepository.save(message);

        /*
         * PRIVATE CHAT
         */
        if (saved.getReceiverId() != null) {

            messagingTemplate.convertAndSend(
                    "/topic/user/" +
                            saved.getReceiverId(),
                    saved
            );

            messagingTemplate.convertAndSend(
                    "/topic/user/" +
                            saved.getSenderId(),
                    saved
            );
        }

        /*
         * GROUP CHAT
         */
        if (saved.getGroupId() != null) {

            messagingTemplate.convertAndSend(
                    "/topic/group/" +
                            saved.getGroupId(),
                    saved
            );
        }

        return ResponseEntity.ok(saved);
    }
}