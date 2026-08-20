package com.chatapp.chatapp.controller;

import com.chatapp.chatapp.entity.Contact;
import com.chatapp.chatapp.entity.User;
import com.chatapp.chatapp.repository.ContactRepository;
import com.chatapp.chatapp.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public ContactController(
            ContactRepository contactRepository,
            UserRepository userRepository
    ) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }


    // =====================================================
    // SEND CONTACT REQUEST
    // =====================================================

    @PostMapping("/request")
    public ResponseEntity<?> sendRequest(
            @RequestBody Map<String, Long> body
    ) {

        Long userId = body.get("userId");
        Long contactUserId = body.get("contactUserId");

        if (userId == null || contactUserId == null) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "User ID and contact user ID are required."
                    ));
        }

        if (userId.equals(contactUserId)) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "You cannot add yourself."
                    ));
        }

        User user =
                userRepository.findById(userId)
                        .orElse(null);

        User contactUser =
                userRepository.findById(contactUserId)
                        .orElse(null);

        if (user == null || contactUser == null) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "User not found."
                    ));
        }


        // Check if I already sent a request
        Contact existing =
                contactRepository
                        .findByUserIdAndContactUserId(
                                userId,
                                contactUserId
                        )
                        .orElse(null);

        if (existing != null) {

            if ("ACCEPTED".equals(existing.getStatus())) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "This user is already in your contacts."
                        ));
            }

            if ("PENDING".equals(existing.getStatus())) {

                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "message",
                                "Contact request already sent."
                        ));
            }

            // If rejected previously, allow a new request
            existing.setStatus("PENDING");
            contactRepository.save(existing);

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Contact request sent again."
                    )
            );
        }


        // Check if the other person already sent me a request
        Contact reverse =
                contactRepository
                        .findByUserIdAndContactUserId(
                                contactUserId,
                                userId
                        )
                        .orElse(null);

        if (reverse != null &&
                "PENDING".equals(reverse.getStatus())) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "This user has already sent you a request. Check your requests."
                    ));
        }


        Contact contact =
                new Contact(
                        userId,
                        contactUserId,
                        contactUser.getUsername()
                );

        contact.setStatus("PENDING");

        contactRepository.save(contact);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Contact request sent."
                )
        );
    }


    // =====================================================
    // GET ACCEPTED CONTACTS
    // =====================================================

    @GetMapping("/{userId}")
    public ResponseEntity<?> getContacts(
            @PathVariable Long userId
    ) {

        List<Contact> sentContacts =
                contactRepository
                        .findByUserIdAndStatus(
                                userId,
                                "ACCEPTED"
                        );

        List<Contact> receivedContacts =
                contactRepository
                        .findByContactUserIdAndStatus(
                                userId,
                                "ACCEPTED"
                        );

        List<Map<String, Object>> result =
                new ArrayList<>();


        // Contacts I added
        for (Contact contact : sentContacts) {

            User user =
                    userRepository
                            .findById(
                                    contact.getContactUserId()
                            )
                            .orElse(null);

            if (user == null) {
                continue;
            }

            Map<String, Object> data =
                    new HashMap<>();

            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("status", user.getStatus());

            result.add(data);
        }


        // People who added me
        for (Contact contact : receivedContacts) {

            User user =
                    userRepository
                            .findById(
                                    contact.getUserId()
                            )
                            .orElse(null);

            if (user == null) {
                continue;
            }

            Map<String, Object> data =
                    new HashMap<>();

            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("status", user.getStatus());

            result.add(data);
        }

        return ResponseEntity.ok(result);
    }


    // =====================================================
    // INCOMING REQUESTS
    // =====================================================

    @GetMapping("/requests/{userId}")
    public ResponseEntity<?> getRequests(
            @PathVariable Long userId
    ) {

        List<Contact> requests =
                contactRepository
                        .findByContactUserIdAndStatus(
                                userId,
                                "PENDING"
                        );

        List<Map<String, Object>> result =
                new ArrayList<>();

        for (Contact contact : requests) {

            User requester =
                    userRepository
                            .findById(
                                    contact.getUserId()
                            )
                            .orElse(null);

            if (requester == null) {
                continue;
            }

            Map<String, Object> data =
                    new HashMap<>();

            data.put("id", contact.getId());
            data.put("userId", requester.getId());
            data.put(
                    "username",
                    requester.getUsername()
            );

            result.add(data);
        }

        return ResponseEntity.ok(result);
    }


    // =====================================================
    // ACCEPT REQUEST
    // =====================================================

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptRequest(
            @PathVariable Long id
    ) {

        Contact contact =
                contactRepository
                        .findById(id)
                        .orElse(null);

        if (contact == null) {

            return ResponseEntity.notFound()
                    .build();
        }

        contact.setStatus("ACCEPTED");

        contactRepository.save(contact);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Contact request accepted."
                )
        );
    }


    // =====================================================
    // REJECT REQUEST
    // =====================================================

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long id
    ) {

        Contact contact =
                contactRepository
                        .findById(id)
                        .orElse(null);

        if (contact == null) {

            return ResponseEntity.notFound()
                    .build();
        }

        contact.setStatus("REJECTED");

        contactRepository.save(contact);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Contact request rejected."
                )
        );
    }
}