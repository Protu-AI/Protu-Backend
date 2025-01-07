package org.protu.userservice.exceptions;


import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.angus.mail.util.MailConnectException;
import org.protu.userservice.dto.ApiResponse;
import org.protu.userservice.exceptions.custom.*;
import org.protu.userservice.helper.FailureResponseHelper;
import org.springframework.beans.factory.annotation.Value;
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
  @Value("${api.version}")
  private String apiVersion;

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(apiVersion, request,HttpStatus.UNAUTHORIZED,e.getMessage(),"Authentication failed");
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleUserNotFoundException(UserNotFoundException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(apiVersion, request,HttpStatus.NOT_FOUND,e.getMessage(),"User not found");
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleUserAlreadyExistsException(UserAlreadyExistsException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(apiVersion, request,HttpStatus.CONFLICT,e.getMessage(),"User already exists");
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(apiVersion, request,HttpStatus.FORBIDDEN,e.getMessage(),"Access denied");
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
    BindingResult bindingResult = ex.getBindingResult();
    List<String> errorMessages = bindingResult.getAllErrors().stream()
        .map(ObjectError::getDefaultMessage)
        .collect(Collectors.toList());
    return FailureResponseHelper.buildResponse(apiVersion, request,HttpStatus.BAD_REQUEST,String.join(", ", errorMessages), "Validation failed");
  }

  @ExceptionHandler(UnauthorizedAccessException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleUnauthorizedAccessException(UnauthorizedAccessException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(apiVersion, request,HttpStatus.UNAUTHORIZED,e.getMessage(),"Unauthorized access");
  }

  @ExceptionHandler(ExpiredJwtException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleExpiredJwtException(ExpiredJwtException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(apiVersion, request,HttpStatus.UNAUTHORIZED,e.getMessage().split("\\.")[0],"Session expired");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(apiVersion, request,HttpStatus.CONFLICT,e.getMostSpecificCause().getMessage(),"Data integrity issue");
  }

  @ExceptionHandler(InvalidVerificationCodeException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleInvalidVerificationTokenException(InvalidVerificationCodeException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(apiVersion, request,HttpStatus.BAD_REQUEST,e.getMessage(),"Invalid verification code");
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleIllegalStateException(IllegalStateException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(apiVersion, request,HttpStatus.CONFLICT,e.getMessage(),"Unexpected state");
  }

  @ExceptionHandler(UserEmailAlreadyVerifiedException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleUserEmailAlreadyVerified(UserEmailAlreadyVerifiedException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(apiVersion, request,HttpStatus.CONFLICT,e.getMessage(),"Email already verified");
  }

  @ExceptionHandler(UserEmailNotVerifiedException.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleUserEmailNotVerified(UserEmailNotVerifiedException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(apiVersion, request,HttpStatus.CONFLICT,e.getMessage(),"Email verification pending");
  }

  @ExceptionHandler({MailConnectException.class, MailSendException.class, MailException.class})
  public ResponseEntity<ApiResponse<ErrorDetails>> handleMailSendException(MailSendException e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(apiVersion, request,HttpStatus.CONFLICT,e.getMessage(),"Email delivery failed");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<ErrorDetails>> handleException(Exception e, HttpServletRequest request) {
    return FailureResponseHelper.buildResponse(apiVersion, request,HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),"Internal server error");
  }
}