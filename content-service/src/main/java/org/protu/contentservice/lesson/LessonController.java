package org.protu.contentservice.lesson;

import jakarta.servlet.http.HttpServletRequest;
import org.protu.contentservice.common.enums.SuccessMessage;
import org.protu.contentservice.common.helpers.JwtHelper;
import org.protu.contentservice.common.properties.AppProperties;
import org.protu.contentservice.common.response.ApiResponse;
import org.protu.contentservice.lesson.dto.*;
import org.protu.contentservice.progress.ProgressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.protu.contentservice.common.response.ApiResponseBuilder.buildApiResponse;

@RestController
@RequestMapping("/api/${app.api.version}")
public class LessonController {

  private final LessonService lessonService;
  private final ProgressService progressService;
  private final String apiVersion;
  private final JwtHelper jwtHelper;

  public LessonController(LessonService lessonService, AppProperties props, ProgressService progressService, JwtHelper jwtHelper) {
    this.lessonService = lessonService;
    this.progressService = progressService;
    this.jwtHelper = jwtHelper;
    apiVersion = props.api().version();
  }

  private Long getUserIdFromBearer(String bearerToken) {
    String token = bearerToken.split(" ")[1];
    return jwtHelper.extractUserId(token);
  }

  @PostMapping("/lessons")
  public ResponseEntity<ApiResponse<LessonResponse>> createLesson(@Validated @RequestBody LessonRequest lessonRequest, HttpServletRequest request) {
    LessonResponse lesson = lessonService.createLesson(lessonRequest);
    return buildApiResponse(SuccessMessage.CREATE_NEW_ENTITY.getMessage("Lesson"), lesson, null, HttpStatus.CREATED, apiVersion, request);
  }

  @GetMapping("/lessons/{lessonName}")
  public ResponseEntity<ApiResponse<LessonResponse>> getSingleLesson(@PathVariable String lessonName, HttpServletRequest request) {
    LessonResponse lesson = lessonService.getLessonByName(lessonName);
    return buildApiResponse(SuccessMessage.GET_SINGLE_ENTITY.getMessage("Lesson"), lesson, null, HttpStatus.OK, apiVersion, request);
  }

  @PatchMapping("/lessons/{lessonName}")
  public ResponseEntity<ApiResponse<LessonResponse>> updateLesson(@PathVariable String lessonName, @Validated @RequestBody LessonUpdateRequest lessonRequest, HttpServletRequest request) {
    LessonResponse lesson = lessonService.updateLesson(lessonName, lessonRequest);
    return buildApiResponse(SuccessMessage.UPDATE_ENTITY.getMessage("Lesson"), lesson, null, HttpStatus.OK, apiVersion, request);
  }

  @GetMapping("/courses/{courseName}/lessons")
  public ResponseEntity<ApiResponse<List<LessonSummary>>> getAllLessonsForCourse(@PathVariable String courseName, HttpServletRequest request) {
    List<LessonSummary> lessons = lessonService.getAllLessonsForCourse(courseName);
    return buildApiResponse(SuccessMessage.GET_ALL_ENTITIES.getMessage("Lessons"), lessons, null, HttpStatus.OK, apiVersion, request);
  }

  @GetMapping("/courses/{courseName}/lessons/progress")
  public ResponseEntity<ApiResponse<List<LessonsWithCompletion>>> getAllLessonsWithCompletionStatus(
      @PathVariable String courseName,
      @RequestHeader("Authorization") String bearer,
      HttpServletRequest request) {

    Long userId = getUserIdFromBearer(bearer);
    List<LessonsWithCompletion> lessons = progressService.getAllLessonsWithCompletionStatus(userId, courseName);
    return buildApiResponse(SuccessMessage.GET_ALL_ENTITIES.getMessage("Lessons"), lessons, null, HttpStatus.OK, apiVersion, request);
  }

  @PostMapping("/courses/{courseName}/lessons/{lessonName}")
  public ResponseEntity<ApiResponse<List<LessonResponse>>> addExistingLessonToCourse(@PathVariable String courseName, @PathVariable String lessonName, HttpServletRequest request) {
    lessonService.addExistingLessonToCourse(courseName, lessonName);
    return buildApiResponse(SuccessMessage.ADD_ENTITY_TO_PARENT_ENTITY.getMessage(lessonName, "lesson", courseName, "course"), null, null, HttpStatus.OK, apiVersion, request);
  }

  @DeleteMapping("/courses/{courseName}/lessons/{lessonName}")
  public ResponseEntity<Void> deleteLessonFromCourse(@PathVariable String courseName, @PathVariable String lessonName) {
    lessonService.deleteLessonFromCourse(courseName, lessonName);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
