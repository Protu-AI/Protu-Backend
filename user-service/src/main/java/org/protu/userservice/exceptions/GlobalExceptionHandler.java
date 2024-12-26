package org.protu.userservice.exceptions;


import io.jsonwebtoken.ExpiredJwtException;
import org.eclipse.angus.mail.util.MailConnectException;
import org.protu.userservice.dto.ApiResponse;
import org.protu.userservice.exceptions.custom.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
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
public class GlobalExceptionHandler {

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleBadCredentialsException(BadCredentialsException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.UNAUTHORIZED.value())
        .details(e.getMessage())
        .build();

    ApiResponse<ErrorDetails> apiResponse = new ApiResponse<>("FAILURE", "Authentication failed", errorDetails);
    return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleUserNotFoundException(UserNotFoundException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.NOT_FOUND.value())
        .details(e.getMessage())
        .build();

    ApiResponse<ErrorDetails> apiResponse = new ApiResponse<>("FAILURE", "User not found", errorDetails);
    return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.CONFLICT.value())
        .details(e.getMessage())
        .build();

    ApiResponse<ErrorDetails> apiResponse = new ApiResponse<>("FAILURE", "User already exists", errorDetails);
    return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleAccessDeniedException(AccessDeniedException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.FORBIDDEN.value())
        .details(e.getMessage())
        .build();

    ApiResponse<ErrorDetails> apiResponse = new ApiResponse<>("FAILURE", "Access denied", errorDetails);
    return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    BindingResult bindingResult = ex.getBindingResult();

    List<String> errorMessages = bindingResult.getAllErrors().stream()
        .map(ObjectError::getDefaultMessage)
        .collect(Collectors.toList());

    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.BAD_REQUEST.value())
        .details(String.join(", ", errorMessages))
        .build();

    ApiResponse<ErrorDetails> apiResponse = new ApiResponse<>("FAILURE", "Validation failed", errorDetails);
    return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(UnauthorizedAccessException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleUnauthorizedAccessException(UnauthorizedAccessException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.UNAUTHORIZED.value())
        .details(e.getMessage())
        .build();

    ApiResponse<ErrorDetails> apiResponse = new ApiResponse<>("FAILURE", "Unauthorized access", errorDetails);
    return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(ExpiredJwtException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleExpiredJwtException(ExpiredJwtException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.UNAUTHORIZED.value())
        .details("Your session has expired. Please log in again.")
        .build();

    ApiResponse<ErrorDetails> apiResponse = new ApiResponse<>("FAILURE", "Session expired", errorDetails);
    return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.CONFLICT.value())
        .details(e.getMostSpecificCause().getMessage())
        .build();

    ApiResponse<ErrorDetails> apiResponse = new ApiResponse<>("FAILURE", "Data integrity issue", errorDetails);
    return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(InvalidVerificationCodeException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleInvalidVerificationTokenException(InvalidVerificationCodeException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.BAD_REQUEST.value())
        .details(e.getMessage())
        .build();

    ApiResponse<ErrorDetails> apiResponse = new ApiResponse<>("FAILURE", "Invalid verification code", errorDetails);
    return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleIllegalStateException(IllegalStateException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.CONFLICT.value())
        .details(e.getMessage())
        .build();

    ApiResponse<ErrorDetails> apiResponse = new ApiResponse<>("FAILURE", "Unexpected state", errorDetails);
    return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(UserEmailAlreadyVerifiedException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleUserEmailAlreadyVerified(UserEmailAlreadyVerifiedException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.CONFLICT.value())
        .details(e.getMessage())
        .build();

    ApiResponse<ErrorDetails> apiResponse = new ApiResponse<>("FAILURE", "Email already verified", errorDetails);
    return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(UserEmailNotVerifiedException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleUserEmailNotVerified(UserEmailNotVerifiedException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.CONFLICT.value())
        .details(e.getMessage())
        .build();

    ApiResponse<ErrorDetails> apiResponse = new ApiResponse<>("FAILURE", "Email verification pending", errorDetails);
    return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler({MailConnectException.class, MailSendException.class, MailException.class})
  public ResponseEntity<ApiResponse<ErrorDetails>> handleMailSendException(MailSendException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.CONFLICT.value())
        .details(e.getMessage())
        .build();

    ApiResponse<ErrorDetails> apiResponse = new ApiResponse<>("FAILURE", "Email delivery failed", errorDetails);
    return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleException(Exception e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .details(e.getMessage())
        .build();

    ApiResponse<ErrorDetails> apiResponse = new ApiResponse<>("FAILURE", "Internal server error", errorDetails);
    return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}