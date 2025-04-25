package org.protu.contentservice.progress;

import jakarta.servlet.http.HttpServletRequest;
import org.protu.contentservice.common.helpers.JwtHelper;
import org.protu.contentservice.common.properties.AppProperties;
import org.protu.contentservice.common.response.ApiResponse;
import org.protu.contentservice.progress.dto.UserProgressInCourse;
import org.protu.contentservice.progress.enums.SuccessMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.protu.contentservice.common.response.ApiResponseBuilder.buildApiResponse;

@RestController
@RequestMapping("/api/${app.api.version}/progress")
public class ProgressController {

  private final ProgressService progressService;
  private final JwtHelper jwtHelper;
  private final String apiVersion;

  public ProgressController(ProgressService progressService, AppProperties props, JwtHelper jwtHelper) {
    this.progressService = progressService;
    this.jwtHelper = jwtHelper;
    apiVersion = props.api().version();
  }

  private Long getUserIdFromBearer(String bearerToken) {
    String token = bearerToken.split(" ")[1];
    return jwtHelper.extractUserId(token);
  }

  @GetMapping("/courses/{courseId}")
  public ResponseEntity<ApiResponse<UserProgressInCourse>> getUserProgressInCourse(
      @PathVariable Integer courseId,
      @RequestHeader("Authorization") String bearerToken,
      HttpServletRequest request) {

    Long userId = getUserIdFromBearer(bearerToken);
    UserProgressInCourse userProgressInCourse = progressService.getUserProgressInCourse(userId, courseId);
    return buildApiResponse(SuccessMessage.GET_USER_PROGRESS_IN_COURSE.message, userProgressInCourse, null, HttpStatus.OK, apiVersion, request);
  }

  @PostMapping("/courses/{courseId}/enrollments")
  public ResponseEntity<ApiResponse<Void>> enrollUserInACourse(
      @PathVariable Integer courseId,
      @RequestHeader("Authorization") String bearerToken,
      HttpServletRequest request) {

    Long userId = getUserIdFromBearer(bearerToken);
    progressService.enrollUserInCourse(userId, courseId);
    return buildApiResponse(SuccessMessage.USER_ENROLLED_IN_COURSE.message, null, null, HttpStatus.CREATED, apiVersion, request);
  }

  @DeleteMapping("/courses/{courseId}/enrollments")
  public ResponseEntity<ApiResponse<Void>> cancelUserEnrollmentInCourse(
      @PathVariable Integer courseId,
      @RequestHeader("Authorization") String bearerToken,
      HttpServletRequest request) {

    Long userId = getUserIdFromBearer(bearerToken);
    progressService.cancelUserEnrollmentInCourse(userId, courseId);
    return buildApiResponse(SuccessMessage.USER_CANCELLED_ENROLLMENT_IN_COURSE.message, null, null, HttpStatus.OK, apiVersion, request);
  }

  @PostMapping("/courses/{courseId}/lessons/{lessonId}/completed")
  public ResponseEntity<ApiResponse<Void>> markLessonCompleted(
      @PathVariable Integer courseId,
      @PathVariable Integer lessonId,
      @RequestHeader("Authorization") String bearerToken,
      HttpServletRequest request) {

    Long userId = getUserIdFromBearer(bearerToken);
    progressService.markLessonCompleted(userId, lessonId);
    progressService.incrementCompletedLessonsForUser(userId, courseId);
    return buildApiResponse(SuccessMessage.USER_COMPLETED_A_COURSE_LESSON.message, null, null, HttpStatus.OK, apiVersion, request);
  }

  @DeleteMapping("/courses/{courseId}/lessons/{lessonId}/completed")
  public ResponseEntity<ApiResponse<Void>> markLessonNotCompleted(
      @PathVariable Integer courseId,
      @PathVariable Integer lessonId,
      @RequestHeader("Authorization") String bearerToken,
      HttpServletRequest request) {

    Long userId = getUserIdFromBearer(bearerToken);
    progressService.markLessonNotCompleted(userId, lessonId);
    progressService.decrementCompletedLessonsForUser(userId, courseId);
    return buildApiResponse(SuccessMessage.USER_COMPLETED_A_COURSE_LESSON.message, null, null, HttpStatus.OK, apiVersion, request);
  }
}
