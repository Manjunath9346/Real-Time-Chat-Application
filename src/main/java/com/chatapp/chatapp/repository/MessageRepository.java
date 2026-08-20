package com.chatapp.chatapp.repository;

import com.chatapp.chatapp.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderId(Long senderId);

    List<Message> findByReceiverId(Long receiverId);

    /*
     * Used by the existing MessageService.
     *
     * The method name is kept because MessageService
     * already calls it, but the actual query uses
     * createdAt instead of timestamp.
     */
    @Query("""
        SELECT m
        FROM Message m
        WHERE
            (m.senderId = :senderId1 AND m.receiverId = :receiverId1)
            OR
            (m.senderId = :senderId2 AND m.receiverId = :receiverId2)
        ORDER BY m.createdAt ASC
        """)
    List<Message> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
            @Param("senderId1") Long senderId1,
            @Param("receiverId1") Long receiverId1,
            @Param("senderId2") Long senderId2,
            @Param("receiverId2") Long receiverId2
    );

    /*
     * Private chat history
     */
    @Query("""
        SELECT m
        FROM Message m
        WHERE
            (m.senderId = :user1 AND m.receiverId = :user2)
            OR
            (m.senderId = :user2 AND m.receiverId = :user1)
        ORDER BY m.createdAt ASC
        """)
    List<Message> findPrivateMessages(
            @Param("user1") Long user1,
            @Param("user2") Long user2
    );

    /*
     * Group chat history
     */
    List<Message> findByGroupIdOrderByCreatedAtAsc(Long groupId);

    /*
     * Group messages
     */
    List<Message> findByGroupId(Long groupId);
}