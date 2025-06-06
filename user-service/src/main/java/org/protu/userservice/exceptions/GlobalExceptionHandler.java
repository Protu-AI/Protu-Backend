package org.protu.userservice.exceptions;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.protu.userservice.config.AppProperties;
import org.protu.userservice.dto.ApiResponse;
import org.protu.userservice.exceptions.custom.*;
import org.protu.userservice.helper.FailureResponseHelper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
  private final AppProperties properties;

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(properties.api().version(), request, HttpStatus.UNAUTHORIZED, e.getMessage(), "Authentication failed");
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleUserNotFoundException(UserNotFoundException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(properties.api().version(), request, HttpStatus.NOT_FOUND, e.getMessage(), "User not found");
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleUserAlreadyExistsException(UserAlreadyExistsException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(properties.api().version(), request, HttpStatus.CONFLICT, e.getMessage(), "User already exists");
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(properties.api().version(), request, HttpStatus.FORBIDDEN, e.getMessage(), "Access denied");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
    BindingResult bindingResult = ex.getBindingResult();
    List<String> errorMessages = bindingResult.getAllErrors().stream()
        .map(ObjectError::getDefaultMessage)
        .collect(Collectors.toList());
    return FailureResponseHelper.buildResponse(properties.api().version(), request, HttpStatus.BAD_REQUEST, String.join(", ", errorMessages), "Validation failed");
  }

  @ExceptionHandler(UnauthorizedAccessException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleUnauthorizedAccessException(UnauthorizedAccessException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(properties.api().version(), request, HttpStatus.UNAUTHORIZED, e.getMessage(), "Unauthorized access");
  }

  @ExceptionHandler(InvalidOrExpiredOtpException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleInvalidVerificationTokenException(InvalidOrExpiredOtpException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(properties.api().version(), request, HttpStatus.BAD_REQUEST, e.getMessage(), "Invalid verification code");
  }

  @ExceptionHandler(UserEmailAlreadyVerifiedException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleUserEmailAlreadyVerified(UserEmailAlreadyVerifiedException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(properties.api().version(), request, HttpStatus.CONFLICT, e.getMessage(), "Email already verified");
  }

  @ExceptionHandler(EmailNotVerifiedException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleUserEmailNotVerified(EmailNotVerifiedException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(properties.api().version(), request, HttpStatus.CONFLICT, e.getMessage(), "Email verification pending");
  }

  @ExceptionHandler(PasswordMismatchException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handlePasswordMismatchException(PasswordMismatchException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(properties.api().version(), request, HttpStatus.BAD_REQUEST, e.getMessage(), "Password change failed");
  }

  @ExceptionHandler(OldAndNewPasswordMatchException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleOldAndNewPasswordMatchException(OldAndNewPasswordMatchException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(properties.api().version(), request, HttpStatus.BAD_REQUEST, e.getMessage(), "Password change failed");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleDataIntegrityViolation(HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(properties.api().version(), request, HttpStatus.CONFLICT, "An error occurred while processing your request. Please try again later.", "Data integrity issue");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleException(Exception e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(properties.api().version(), request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), "Internal server error");
  }
}