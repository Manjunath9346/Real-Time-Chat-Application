package com.chatapp.chatapp.controller;

import com.chatapp.chatapp.entity.Message;
import com.chatapp.chatapp.repository.MessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin
public class MessageController {

    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageController(
            MessageRepository messageRepository,
            SimpMessagingTemplate messagingTemplate) {

        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /*
     * Get private chat history
     *
     * GET:
     * /api/messages/private/1/2
     */
    @GetMapping("/private/{user1}/{user2}")
    public ResponseEntity<List<Message>> getPrivateMessages(
            @PathVariable Long user1,
            @PathVariable Long user2) {

        return ResponseEntity.ok(
                messageRepository.findPrivateMessages(user1, user2)
        );
    }

    /*
     * Get group chat history
     *
     * GET:
     * /api/messages/group/1
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Message>> getGroupMessages(
            @PathVariable Long groupId) {

        return ResponseEntity.ok(
                messageRepository
                        .findByGroupIdOrderByCreatedAtAsc(groupId)
        );
    }

    /*
     * Send a message using REST
     *
     * POST:
     * /api/messages
     */
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

        if ((message.getContent() == null ||
             message.getContent().isBlank()) &&
            message.getFileUrl() == null) {

            return ResponseEntity.badRequest()
                    .body("Message content cannot be empty");
        }

        message.setId(null);

        Message saved =
                messageRepository.save(message);

        /*
         * PRIVATE MESSAGE
         */
        if (saved.getReceiverId() != null) {

            messagingTemplate.convertAndSend(
                    "/topic/user/" + saved.getReceiverId(),
                    saved
            );

            /*
             * Also send back to sender.
             * This allows multiple browser sessions
             * to stay synchronized.
             */
            messagingTemplate.convertAndSend(
                    "/topic/user/" + saved.getSenderId(),
                    saved
            );
        }

        /*
         * GROUP MESSAGE
         */
        if (saved.getGroupId() != null) {

            messagingTemplate.convertAndSend(
                    "/topic/group/" + saved.getGroupId(),
                    saved
            );
        }

        return ResponseEntity.ok(saved);
    }

    /*
     * Star / unstar message
     */
    @PutMapping("/{id}/star")
    public ResponseEntity<?> toggleStar(
            @PathVariable Long id) {

        Message message =
                messageRepository.findById(id)
                        .orElse(null);

        if (message == null) {
            return ResponseEntity.notFound().build();
        }

        message.setStarred(
                !message.isStarred()
        );

        return ResponseEntity.ok(
                messageRepository.save(message)
        );
    }

    /*
     * Delete message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessage(
            @PathVariable Long id) {

        if (!messageRepository.existsById(id)) {

            return ResponseEntity.notFound().build();
        }

        messageRepository.deleteById(id);

        return ResponseEntity.ok(
                "Message deleted successfully"
        );
    }
}