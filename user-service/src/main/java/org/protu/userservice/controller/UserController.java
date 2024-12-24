package org.protu.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.dto.UpdateRequestDto;
import org.protu.userservice.dto.UserResponseDto;
import org.protu.userservice.service.impl.JWTServiceImpl;
import org.protu.userservice.service.impl.UserServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserServiceImpl userService;
  private final JWTServiceImpl jwtServiceImpl;

  @GetMapping("/{id}")
  public ResponseEntity<UserResponseDto> getUserById(
      @PathVariable("id") Long userId,
      @RequestHeader("Authorization") String authHeader) {

    String token = jwtServiceImpl.getTokenFromHeader(authHeader);
    Long authUserId = jwtServiceImpl.getUserIdFromToken(token);
    UserResponseDto userResponseDto = userService.getUserById(userId, authUserId);
    return ResponseEntity.ok(userResponseDto);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserResponseDto> updateUser(
      @PathVariable("id") Long userId,
      @Validated @RequestBody UpdateRequestDto userUpdateDto,
      @RequestHeader("Authorization") String authHeader) {

    String token = jwtServiceImpl.getTokenFromHeader(authHeader);
    Long authUserId = jwtServiceImpl.getUserIdFromToken(token);
    UserResponseDto updatedUser = userService.updateUser(userId, authUserId, userUpdateDto);
    return ResponseEntity.ok(updatedUser);
  }


  @PatchMapping("/{id}/deactivate")
  public ResponseEntity<String> deactivateUser(
      @PathVariable("id") Long userId,
      @RequestHeader("Authorization") String authHeader) {

    String token = jwtServiceImpl.getTokenFromHeader(authHeader);
    Long authUserId = jwtServiceImpl.getUserIdFromToken(token);
    userService.deactivateUser(userId, authUserId);
    return ResponseEntity.ok("User deactivated successfully");
  }
}
