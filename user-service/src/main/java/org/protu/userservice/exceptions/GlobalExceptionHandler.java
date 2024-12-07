package org.protu.userservice.exceptions;


import io.jsonwebtoken.ExpiredJwtException;
import org.protu.userservice.exceptions.custom.UnauthorizedAccessException;
import org.protu.userservice.exceptions.custom.UserAlreadyExistsException;
import org.protu.userservice.exceptions.custom.UserNotFoundException;
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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorDetails> handleBadCredentialsException(BadCredentialsException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.UNAUTHORIZED.value())
        .message("Bad credentials")
        .details(e.getMessage())
        .timestamp(Timestamp.from(Instant.now()))
        .build();

    return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorDetails> handleUserNotFoundException(UserNotFoundException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.NOT_FOUND.value())
        .message("User not found")
        .details(e.getMessage())
        .timestamp(Timestamp.from(Instant.now()))
        .build();

    return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorDetails> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.CONFLICT.value())
        .message("User already exists")
        .details(e.getMessage())
        .timestamp(Timestamp.from(Instant.now()))
        .build();

    return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorDetails> handleAccessDeniedException(AccessDeniedException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.FORBIDDEN.value())
        .message("Access denied")
        .details(e.getMessage())
        .timestamp(Timestamp.from(Instant.now()))
        .build();

    return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorDetails> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    BindingResult bindingResult = ex.getBindingResult();

    List<String> errorMessages = bindingResult.getAllErrors().stream()
        .map(ObjectError::getDefaultMessage)
        .collect(Collectors.toList());

    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.BAD_REQUEST.value())
        .message("Validation failed")
        .details(String.join(", ", errorMessages))
        .timestamp(Timestamp.from(Instant.now()))
        .build();

    return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(UnauthorizedAccessException.class)
  public ResponseEntity<ErrorDetails> handleUnauthorizedAccessException(UnauthorizedAccessException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.UNAUTHORIZED.value())
        .message("Unauthorized access")
        .details(e.getMessage())
        .timestamp(Timestamp.from(Instant.now()))
        .build();

    return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(ExpiredJwtException.class)
  public ResponseEntity<ErrorDetails> handleExpiredJwtException(ExpiredJwtException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.UNAUTHORIZED.value())
        .message("Expired JWT token")
        .details("Your session has expired. Please log in again.")
        .timestamp(Timestamp.from(Instant.now()))
        .build();

    return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
  }


  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorDetails> handleDataIntegrityViolation(DataIntegrityViolationException e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.CONFLICT.value())
        .message("Data integrity violation")
        .details(e.getMostSpecificCause().getMessage())
        .timestamp(Timestamp.from(Instant.now()))
        .build();

    return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDetails> handleException(Exception e) {
    ErrorDetails errorDetails = ErrorDetails.builder()
        .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .message("Internal server error")
        .details(e.getMessage())
        .timestamp(Timestamp.from(Instant.now()))
        .build();

    return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}