package org.protu.userservice.controller;

import org.protu.userservice.dto.RegisterRequestDTO;
import org.protu.userservice.model.User;
import org.protu.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Validated @RequestBody RegisterRequestDTO registerRequest) {
        try {
            User user = userService.registerUser(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully: " + user.getUsername());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
