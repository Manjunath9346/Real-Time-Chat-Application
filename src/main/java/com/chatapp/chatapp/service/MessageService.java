package com.chatapp.chatapp.service;

import com.chatapp.chatapp.entity.Message;
import com.chatapp.chatapp.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public List<Message> getConversation(Long user1, Long user2) {

        return messageRepository
                .findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
                        user1,
                        user2,
                        user1,
                        user2
                );
    }
}