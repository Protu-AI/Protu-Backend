package org.protu.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.protu.userservice.config.AppProperties;
import org.protu.userservice.constants.SuccessMessages;
import org.protu.userservice.dto.ApiResponse;
import org.protu.userservice.dto.request.UserRolesRequestDto;
import org.protu.userservice.dto.response.DeactivateResDto;
import org.protu.userservice.dto.response.UserDetailsForAdminDto;
import org.protu.userservice.service.AdminService;
import org.protu.userservice.service.JWTService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.protu.userservice.helper.SuccessResponseHelper.buildResponse;

@RestController
@RequestMapping("/api/${app.api.version}/admin")
@RequiredArgsConstructor
public class AdminController {

  private final AdminService adminService;
  private final JWTService jwtService;
  private final AppProperties properties;

  @PatchMapping("/users/{userId}/deactivate")
  public ResponseEntity<ApiResponse<DeactivateResDto>> deactivateUser(
      @PathVariable String userId,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest request) {

    String token = jwtService.getTokenFromHeader(authHeader);
    DeactivateResDto responseDto = adminService.deactivateUser(userId, token);
    return buildResponse(properties.api().version(), request, HttpStatus.OK, responseDto, SuccessMessages.DEACTIVATE_USER_MSG.message);
  }

  @PatchMapping("/users/{userId}/activate")
  public ResponseEntity<ApiResponse<Void>> activateUser(
      @PathVariable String userId,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest request) {

    String token = jwtService.getTokenFromHeader(authHeader);
    adminService.activateUser(userId, token);
    return buildResponse(properties.api().version(), request, HttpStatus.OK, null, SuccessMessages.ACTIVATE_USER_MSG.message);
  }

  @GetMapping("/users/{userId}")
  public ResponseEntity<ApiResponse<UserDetailsForAdminDto>> getUserDetailsForAdmin(
      @PathVariable String userId,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest request) {

    String token = jwtService.getTokenFromHeader(authHeader);
    UserDetailsForAdminDto userDetails = adminService.getUserDetailsForAdmin(userId, token);
    return buildResponse(properties.api().version(), request, HttpStatus.OK, userDetails, SuccessMessages.GET_USER_MSG.message);
  }

  @PostMapping("/users/{userId}/roles")
  public ResponseEntity<ApiResponse<UserDetailsForAdminDto>> addRoleToUser(
      @PathVariable String userId,
      @RequestHeader("Authorization") String authHeader,
      @RequestBody @Valid UserRolesRequestDto requestDto,
      HttpServletRequest request) {

    String token = jwtService.getTokenFromHeader(authHeader);
    UserDetailsForAdminDto userDetails = adminService.addRoleToUser(requestDto.role(), userId, token);
    return buildResponse(properties.api().version(), request, HttpStatus.OK, userDetails, SuccessMessages.NEW_ROLE_FOR_USER.message);
  }

  @DeleteMapping("/users/{userId}")
  public ResponseEntity<Void> deleteUser(
      @PathVariable String userId,
      @RequestHeader("Authorization") String authHeader) {

    String token = jwtService.getTokenFromHeader(authHeader);
    adminService.deleteUser(userId, token);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
