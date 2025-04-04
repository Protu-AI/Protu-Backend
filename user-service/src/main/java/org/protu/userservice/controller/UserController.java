package org.protu.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.protu.userservice.config.AppProperties;
import org.protu.userservice.constants.SuccessMessages;
import org.protu.userservice.dto.ApiResponse;
import org.protu.userservice.dto.request.ChangePasswordReqDto;
import org.protu.userservice.dto.request.FullUpdateReqDto;
import org.protu.userservice.dto.request.PartialUpdateReqDto;
import org.protu.userservice.dto.response.ProfilePicResDto;
import org.protu.userservice.dto.response.UserResDto;
import org.protu.userservice.service.JWTService;
import org.protu.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.protu.userservice.helper.SuccessResponseHelper.buildResponse;

@RestController
@RequestMapping("/api/${app.api.version}/users")
@RequiredArgsConstructor
public class UserController {
  private final JWTService jwtService;
  private final UserService userService;
  private final AppProperties properties;

  private String getIdFromAuthHeader(String authHeader) {
    String token = jwtService.getTokenFromHeader(authHeader);
    return jwtService.getUserIdFromToken(token);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResDto>> getUserById(
      @PathVariable("id") String userId,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest request) {

    UserResDto userResDto = userService.getUserById(userId, getIdFromAuthHeader(authHeader));
    return buildResponse(properties.api().version(), request, HttpStatus.OK, userResDto, SuccessMessages.GET_USER_MSG.message);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResDto>> fullUpdateUser(
      @PathVariable("id") String userId,
      @Validated @RequestBody FullUpdateReqDto userUpdateDto,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest request) {

    UserResDto userResDto = userService.fullUpdateUser(userId, getIdFromAuthHeader(authHeader), userUpdateDto);
    return buildResponse(properties.api().version(), request, HttpStatus.OK, userResDto, SuccessMessages.UPDATE_USER_MSG.message);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResDto>> partialUpdateUser(
      @PathVariable("id") String userId,
      @Validated @RequestBody PartialUpdateReqDto userUpdateDto,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest request) {

    UserResDto userResDto = userService.partialUpdateUser(userId, getIdFromAuthHeader(authHeader), userUpdateDto);
    return buildResponse(properties.api().version(), request, HttpStatus.OK, userResDto, SuccessMessages.UPDATE_USER_MSG.message);
  }

  @PostMapping("/{id}/profile-picture")
  public ResponseEntity<ApiResponse<ProfilePicResDto>> uploadProfilePic(
      @PathVariable("id") String userId,
      @RequestParam("file") MultipartFile file,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest request) {

    ProfilePicResDto profilePicResDto = userService.uploadProfilePic(file, userId, getIdFromAuthHeader(authHeader));
    return buildResponse(properties.api().version(), request, HttpStatus.CREATED, profilePicResDto, SuccessMessages.UPLOAD_PROFILE_PIC.message);
  }

  @PostMapping("/{id}/change-password")
  ResponseEntity<ApiResponse<Void>> changePassword(
      @PathVariable("id") String userId,
      @RequestHeader("Authorization") String authHeader,
      @Validated @RequestBody ChangePasswordReqDto reqDto,
      HttpServletRequest request
  ) {
    userService.changePassword(reqDto, userId, getIdFromAuthHeader(authHeader));
    return buildResponse(properties.api().version(), request, HttpStatus.OK, null, SuccessMessages.CHANGE_PASSWORD_MSG.message);
  }
}
