package org.protu.contentservice.progress;

import jakarta.servlet.http.HttpServletRequest;
import org.protu.contentservice.common.enums.SuccessMessage;
import org.protu.contentservice.common.helpers.JwtHelper;
import org.protu.contentservice.common.properties.AppProperties;
import org.protu.contentservice.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.protu.contentservice.common.response.ApiResponseBuilder.buildSuccessApiResponse;

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

  @GetMapping("/courses/{courseName}")
  public ResponseEntity<ApiResponse<UserCourseProgress>> getUserProgressInCourse(
      @PathVariable String courseName,
      @RequestHeader("Authorization") String bearerToken,
      HttpServletRequest request) {

    Long userId = getUserIdFromBearer(bearerToken);
    UserCourseProgress userCourseProgress = progressService.getUserProgressInCourse(userId, courseName);
    final String message = SuccessMessage.GET_USER_PROGRESS_IN_COURSE.message;
    return buildSuccessApiResponse(message, userCourseProgress, HttpStatus.OK, apiVersion, request);
  }

  @PostMapping("/courses/{courseName}/enrollments")
  public ResponseEntity<ApiResponse<Void>> enrollUserInACourse(
      @PathVariable String courseName,
      @RequestHeader("Authorization") String bearerToken,
      HttpServletRequest request) {

    Long userId = getUserIdFromBearer(bearerToken);
    progressService.enrollUserInCourse(userId, courseName);
    final String message = SuccessMessage.USER_ENROLLED_IN_COURSE.message;
    return buildSuccessApiResponse(message, null, HttpStatus.CREATED, apiVersion, request);
  }

  @DeleteMapping("/courses/{courseName}/enrollments")
  public ResponseEntity<ApiResponse<Void>> cancelUserEnrollmentInCourse(
      @PathVariable String courseName,
      @RequestHeader("Authorization") String bearerToken,
      HttpServletRequest request) {

    Long userId = getUserIdFromBearer(bearerToken);
    progressService.cancelUserEnrollmentInCourse(userId, courseName);
    final String message = SuccessMessage.USER_CANCELLED_ENROLLMENT_IN_COURSE.message;
    return buildSuccessApiResponse(message, null, HttpStatus.OK, apiVersion, request);
  }

  @PostMapping("/courses/{courseName}/lessons/{lessonName}/completed")
  public ResponseEntity<ApiResponse<Void>> markLessonCompleted(
      @PathVariable String courseName,
      @PathVariable String lessonName,
      @RequestHeader("Authorization") String bearerToken,
      HttpServletRequest request) {

    Long userId = getUserIdFromBearer(bearerToken);
    progressService.incrementCompletedLessonsByUser(userId, courseName, lessonName);
    final String message = SuccessMessage.USER_COMPLETED_A_COURSE_LESSON.message;
    return buildSuccessApiResponse(message, null, HttpStatus.OK, apiVersion, request);
  }

  @DeleteMapping("/courses/{courseName}/lessons/{lessonName}/completed")
  public ResponseEntity<ApiResponse<Void>> markLessonNotCompleted(
      @PathVariable String courseName,
      @PathVariable String lessonName,
      @RequestHeader("Authorization") String bearerToken,
      HttpServletRequest request) {

    Long userId = getUserIdFromBearer(bearerToken);
    progressService.decrementCompletedLessonsByUser(userId, courseName, lessonName);
    final String message = SuccessMessage.USER_UNCOMPLETED_A_COURSE_LESSON.message;
    return buildSuccessApiResponse(message, null, HttpStatus.OK, apiVersion, request);
  }
}
