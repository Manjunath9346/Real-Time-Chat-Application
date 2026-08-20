package com.chatapp.chatapp.repository;

import com.chatapp.chatapp.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContactRepository
        extends JpaRepository<Contact, Long> {

    // All contacts created by a user
    List<Contact> findByUserId(Long userId);

    // Accepted contacts created by a user
    List<Contact> findByUserIdAndStatus(
            Long userId,
            String status
    );

    // Find direct request
    Optional<Contact> findByUserIdAndContactUserId(
            Long userId,
            Long contactUserId
    );

    // Check direct request
    boolean existsByUserIdAndContactUserId(
            Long userId,
            Long contactUserId
    );

    // Incoming requests
    List<Contact> findByContactUserIdAndStatus(
            Long contactUserId,
            String status
    );

    // Delete contact
    void deleteByUserIdAndContactUserId(
            Long userId,
            Long contactUserId
    );
}