package com.chatapp.chatapp.controller;

import com.chatapp.chatapp.entity.User;
import com.chatapp.chatapp.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{username}")
    public User getUser(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }
}