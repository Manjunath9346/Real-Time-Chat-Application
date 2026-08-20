package com.chatapp.chatapp.controller;

import com.chatapp.chatapp.entity.User;
import com.chatapp.chatapp.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        if (user.getUsername() == null ||
            user.getUsername().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Username is required"));
        }

        if (user.getEmail() == null ||
            user.getEmail().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email is required"));
        }

        if (user.getPassword() == null ||
            user.getPassword().length() < 6) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Password must contain at least 6 characters"
                    ));
        }

        if (userRepository.existsByUsername(user.getUsername())) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Username already exists"
                    ));
        }

        if (userRepository.existsByEmail(user.getEmail())) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Email already exists"
                    ));
        }

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        user.setStatus("OFFLINE");
        user.setLastSeen(null);

        User savedUser = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();

        response.put("message", "Registration successful");
        response.put("id", savedUser.getId());
        response.put("username", savedUser.getUsername());
        response.put("email", savedUser.getEmail());

        return ResponseEntity.ok(response);
    }


    // ===============================
    // LOGIN
    // ===============================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> request) {

        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Username and password are required"
                    ));
        }

        User user = userRepository
                .findByUsername(username)
                .orElse(null);

        if (user == null) {

            return ResponseEntity.status(401)
                    .body(Map.of(
                            "message",
                            "Invalid username or password"
                    ));
        }

        if (!passwordEncoder.matches(
                password,
                user.getPassword())) {

            return ResponseEntity.status(401)
                    .body(Map.of(
                            "message",
                            "Invalid username or password"
                    ));
        }

        // User is online
        user.setStatus("ONLINE");
        user.setLastSeen(LocalDateTime.now());

        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();

        response.put("message", "Login successful");
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("status", user.getStatus());

        return ResponseEntity.ok(response);
    }


    // ===============================
    // HEARTBEAT
    // ===============================

    @PostMapping("/heartbeat/{id}")
    public ResponseEntity<?> heartbeat(
            @PathVariable Long id) {

        User user = userRepository
                .findById(id)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        user.setStatus("ONLINE");
        user.setLastSeen(LocalDateTime.now());

        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of(
                        "status", "ONLINE",
                        "lastSeen", user.getLastSeen()
                )
        );
    }


    // ===============================
    // STATUS
    // ===============================

    @GetMapping("/status/{id}")
    public ResponseEntity<?> getStatus(
            @PathVariable Long id) {

        User user = userRepository
                .findById(id)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        boolean online = false;

        if (user.getLastSeen() != null) {

            LocalDateTime now = LocalDateTime.now();

            online =
                    user.getLastSeen()
                            .isAfter(
                                    now.minusSeconds(30)
                            );
        }

        if (online) {
            user.setStatus("ONLINE");
        } else {
            user.setStatus("OFFLINE");
        }

        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of(
                        "id", user.getId(),
                        "status", user.getStatus()
                )
        );
    }


    // ===============================
    // LOGOUT
    // ===============================

    @PostMapping("/logout/{id}")
    public ResponseEntity<?> logout(
            @PathVariable Long id) {

        User user =
                userRepository.findById(id)
                        .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        user.setStatus("OFFLINE");
        user.setLastSeen(null);

        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Logged out successfully"
                )
        );
    }
}