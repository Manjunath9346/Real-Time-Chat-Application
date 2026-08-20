package com.chatapp.chatapp.controller;

import com.chatapp.chatapp.entity.User;
import com.chatapp.chatapp.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/search")
@CrossOrigin
public class SearchController {

    private final UserRepository userRepository;

    public SearchController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserSearchResult>> searchUsers(
            @RequestParam(defaultValue = "") String q) {

        String query =
                q == null
                        ? ""
                        : q.trim().toLowerCase(Locale.ROOT);

        if (query.isBlank()) {
            return ResponseEntity.ok(List.of());
        }

        List<UserSearchResult> results =
                userRepository.findAll()
                        .stream()
                        .filter(user ->
                                user.getUsername()
                                        .toLowerCase(Locale.ROOT)
                                        .contains(query)
                        )
                        .limit(100)
                        .map(user ->
                                new UserSearchResult(
                                        user.getId(),
                                        user.getUsername(),
                                        user.getStatus()
                                )
                        )
                        .toList();

        return ResponseEntity.ok(results);
    }

    @GetMapping("/users/all")
    public ResponseEntity<List<UserSearchResult>> getAllUsers() {

        List<UserSearchResult> results =
                userRepository.findAll()
                        .stream()
                        .limit(100)
                        .map(user ->
                                new UserSearchResult(
                                        user.getId(),
                                        user.getUsername(),
                                        user.getStatus()
                                )
                        )
                        .toList();

        return ResponseEntity.ok(results);
    }

    public record UserSearchResult(
            Long id,
            String username,
            String status
    ) {
    }
}