package org.protu.contentservice.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.protu.contentservice.common.exception.custom.*;
import org.protu.contentservice.common.properties.AppProperties;
import org.protu.contentservice.common.response.ApiResponse;
import org.protu.contentservice.common.response.ErrorDetails;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static org.protu.contentservice.common.response.ApiResponseBuilder.buildFailureApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private final String apiVersion;

  public GlobalExceptionHandler(AppProperties props) {
    apiVersion = props.api().version();
  }

  private ErrorDetails buildErrorDetails(int statusCode, String details) {
    return new ErrorDetails(statusCode, details);
  }

  @ExceptionHandler(UserNotEnrolledInCourseException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleUserNotEnrolledInCourseException(UserNotEnrolledInCourseException e, HttpServletRequest request) {
    List<ErrorDetails> errors = List.of(buildErrorDetails(HttpStatus.CONFLICT.value(), e.getMessage()));
    return buildFailureApiResponse("Course enrollment error", errors, HttpStatus.CONFLICT, apiVersion, request);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleEntityNotFoundException(EntityNotFoundException e, HttpServletRequest request) {
    List<ErrorDetails> errors = List.of(buildErrorDetails(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    return buildFailureApiResponse("Entity conflict error", errors, HttpStatus.NOT_FOUND, apiVersion, request);
  }

  @ExceptionHandler({LessonAlreadyNotCompletedException.class, LessonAlreadyCompletedException.class})
  public ResponseEntity<ApiResponse<ErrorDetails>> handleLessonCompletionException(RuntimeException e, HttpServletRequest request) {
    List<ErrorDetails> errors = List.of(buildErrorDetails(HttpStatus.CONFLICT.value(), e.getMessage()));
    return buildFailureApiResponse("Lesson Completion conflict", errors, HttpStatus.CONFLICT, apiVersion, request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<ErrorDetails> errors = ex.getBindingResult().getAllErrors().stream()
        .map(error -> buildErrorDetails(HttpStatus.BAD_REQUEST.value(), error.getDefaultMessage()))
        .collect(Collectors.toList());
    return buildFailureApiResponse("Validation failed", errors, HttpStatus.BAD_REQUEST, apiVersion, request);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleDataIntegrityViolationException(HttpServletRequest request) {
    List<ErrorDetails> errors = List.of(
        buildErrorDetails(
            HttpStatus.CONFLICT.value(),
            "An error occurred while processing your request. Please try again later."
        ));
    return buildFailureApiResponse("Data integrity issue", errors, HttpStatus.CONFLICT, apiVersion, request);
  }

  @ExceptionHandler(SQLException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleSQLException(HttpServletRequest request) {
    List<ErrorDetails> errors = List.of(
        buildErrorDetails(
            HttpStatus.CONFLICT.value(),
            "An error occurred while processing your request. Please try again later."
        ));
    return buildFailureApiResponse("Database operation failed", errors, HttpStatus.CONFLICT, apiVersion, request);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleUserNotFoundException(UserNotFoundException e, HttpServletRequest request) {
    List<ErrorDetails> errors = List.of(buildErrorDetails(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    return buildFailureApiResponse("User Not Found", errors, HttpStatus.NOT_FOUND, apiVersion, request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleException(Exception e, HttpServletRequest request) {
    List<ErrorDetails> errors = List.of(buildErrorDetails(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    return buildFailureApiResponse("Internal server error", errors, HttpStatus.INTERNAL_SERVER_ERROR, apiVersion, request);
  }
}
