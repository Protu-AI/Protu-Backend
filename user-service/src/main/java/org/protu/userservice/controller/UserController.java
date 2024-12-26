package org.protu.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.dto.ApiResponse;
import org.protu.userservice.dto.request.UpdateReqDto;
import org.protu.userservice.dto.response.DeactivateResDto;
import org.protu.userservice.dto.response.UserResDto;
import org.protu.userservice.service.impl.JWTServiceImpl;
import org.protu.userservice.service.impl.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/${api.version}/users")
@RequiredArgsConstructor
public class UserController {

  private final UserServiceImpl userService;
  private final JWTServiceImpl jwtServiceImpl;

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResDto>> getUserById(
      @PathVariable("id") Long userId,
      @RequestHeader("Authorization") String authHeader) {

    String token = jwtServiceImpl.getTokenFromHeader(authHeader);
    Long authUserId = jwtServiceImpl.getUserIdFromToken(token);
    UserResDto userResDto = userService.getUserById(userId, authUserId);
    String message = "User details retrieved successfully";
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(userResDto, message));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResDto>> updateUser(
      @PathVariable("id") Long userId,
      @Validated @RequestBody UpdateReqDto userUpdateDto,
      @RequestHeader("Authorization") String authHeader) {

    String token = jwtServiceImpl.getTokenFromHeader(authHeader);
    Long authUserId = jwtServiceImpl.getUserIdFromToken(token);
    UserResDto userResDto = userService.updateUser(userId, authUserId, userUpdateDto);
    String message = "User profile has been updated successfully";
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(userResDto, message));
  }


  @PatchMapping("/{id}/deactivate")
  public ResponseEntity<ApiResponse<DeactivateResDto>> deactivateUser(
      @PathVariable("id") Long userId,
      @RequestHeader("Authorization") String authHeader) {

    String token = jwtServiceImpl.getTokenFromHeader(authHeader);
    Long authUserId = jwtServiceImpl.getUserIdFromToken(token);
    DeactivateResDto deactivateResDto = userService.deactivateUser(userId, authUserId);
    String message = "User account has been deactivated successfully";
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(deactivateResDto, message));
  }
}
