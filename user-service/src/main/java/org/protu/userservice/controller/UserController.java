package org.protu.userservice.controller;

import org.protu.userservice.dto.UpdateRequestDto;
import org.protu.userservice.dto.UserResponseDto;
import org.protu.userservice.service.JWTServiceImpl;
import org.protu.userservice.service.UserServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserServiceImpl userService;
  private final JWTServiceImpl jwtServiceImpl;

  public UserController(UserServiceImpl userService, JWTServiceImpl jwtServiceImpl) {
    this.userService = userService;
    this.jwtServiceImpl = jwtServiceImpl;
  }

  private String getTokenFromAuthHeader(String authHeader) {
    return authHeader.split(" ")[1];
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponseDto> getUserById(
      @PathVariable("id") Long userId
      , @RequestHeader("Authorization") String authHeader) throws AccessDeniedException {

    String username = jwtServiceImpl.getUsernameFromToken(getTokenFromAuthHeader(authHeader));
    UserResponseDto userResponseDto = userService.getUserById(userId, username);
    return ResponseEntity.ok(userResponseDto);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserResponseDto> updateUser(
      @PathVariable("id") Long userId,
      @Validated @RequestBody UpdateRequestDto userUpdateDto
      , @RequestHeader("Authorization") String authHeader) throws AccessDeniedException {

    String username = jwtServiceImpl.getUsernameFromToken(getTokenFromAuthHeader(authHeader));
    UserResponseDto updatedUser = userService.updateUser(userId, userUpdateDto, username);
    return ResponseEntity.ok(updatedUser);
  }


  @PatchMapping("/{id}/deactivate")
  public ResponseEntity<String> deactivateUser(
      @PathVariable("id") Long userId
      , @RequestHeader("Authorization") String authHeader) throws AccessDeniedException {

    String username = jwtServiceImpl.getUsernameFromToken(getTokenFromAuthHeader(authHeader));
    userService.deactivateUser(userId, username);
    return ResponseEntity.ok("User deactivated successfully");
  }
}
