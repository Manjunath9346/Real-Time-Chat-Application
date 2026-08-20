package com.chatapp.chatapp.controller;

import com.chatapp.chatapp.entity.Message;
import com.chatapp.chatapp.repository.MessageRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;

    public ChatController(
            SimpMessagingTemplate messagingTemplate,
            MessageRepository messageRepository) {

        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
    }

    /*
     * PRIVATE REAL-TIME MESSAGE
     *
     * Client sends:
     *
     * /app/chat.private
     *
     * Server sends to:
     *
     * /topic/user/{userId}
     */
    @MessageMapping("/chat.private")
    public void sendPrivateMessage(
            @Payload Message message) {

        if (message.getSenderId() == null ||
            message.getReceiverId() == null) {

            return;
        }

        if ((message.getContent() == null ||
             message.getContent().isBlank()) &&
            message.getFileUrl() == null) {

            return;
        }

        message.setId(null);

        Message saved =
                messageRepository.save(message);

        /*
         * Send to receiver
         */
        messagingTemplate.convertAndSend(
                "/topic/user/" +
                saved.getReceiverId(),
                saved
        );

        /*
         * Send to sender
         */
        messagingTemplate.convertAndSend(
                "/topic/user/" +
                saved.getSenderId(),
                saved
        );
    }

    /*
     * GROUP REAL-TIME MESSAGE
     *
     * Client sends:
     *
     * /app/chat.group
     *
     * Server sends to:
     *
     * /topic/group/{groupId}
     */
    @MessageMapping("/chat.group")
    public void sendGroupMessage(
            @Payload Message message) {

        if (message.getSenderId() == null ||
            message.getGroupId() == null) {

            return;
        }

        if ((message.getContent() == null ||
             message.getContent().isBlank()) &&
            message.getFileUrl() == null) {

            return;
        }

        message.setId(null);

        Message saved =
                messageRepository.save(message);

        messagingTemplate.convertAndSend(
                "/topic/group/" +
                saved.getGroupId(),
                saved
        );
    }
}