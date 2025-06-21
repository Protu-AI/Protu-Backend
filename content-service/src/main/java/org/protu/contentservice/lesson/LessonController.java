package org.protu.contentservice.lesson;

import jakarta.servlet.http.HttpServletRequest;
import org.protu.contentservice.common.enums.SuccessMessage;
import org.protu.contentservice.common.helpers.JwtHelper;
import org.protu.contentservice.common.properties.AppProperties;
import org.protu.contentservice.common.response.ApiResponse;
import org.protu.contentservice.lesson.dto.LessonRequest;
import org.protu.contentservice.lesson.dto.LessonUpdateRequest;
import org.protu.contentservice.lesson.dto.LessonWithContent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.protu.contentservice.common.response.ApiResponseBuilder.buildSuccessApiResponse;

@RestController
@RequestMapping("/api/${app.api.version}/lessons")
public class LessonController {

  private final LessonService lessonService;
  private final String apiVersion;
  private final JwtHelper jwtHelper;

  public LessonController(LessonService lessonService, AppProperties props, JwtHelper jwtHelper) {
    this.lessonService = lessonService;
    this.jwtHelper = jwtHelper;
    apiVersion = props.api().version();
  }

  private Long getUserIdFromBearer(String bearerToken) {
    String token = bearerToken.split(" ")[1];
    return jwtHelper.extractUserId(token);
  }

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createLesson(
      @Validated @RequestBody LessonRequest lessonRequest,
      HttpServletRequest request) {

    lessonService.createLesson(lessonRequest);
    final String message = SuccessMessage.CREATE_NEW_ENTITY.getMessage("Lesson");
    return buildSuccessApiResponse(message, null, HttpStatus.CREATED, apiVersion, request);
  }

  @GetMapping("/{lessonName}")
  public ResponseEntity<ApiResponse<LessonWithContent>> getSingleLesson(
      @PathVariable String lessonName,
      HttpServletRequest request) {

    LessonWithContent lesson = lessonService.findByName(lessonName);
    final String message = SuccessMessage.GET_SINGLE_ENTITY.getMessage("Lesson");
    return buildSuccessApiResponse(message, lesson, HttpStatus.OK, apiVersion, request);
  }

  @PatchMapping("/{lessonName}")
  public ResponseEntity<ApiResponse<Void>> updateLesson(
      @PathVariable String lessonName,
      @Validated @RequestBody LessonUpdateRequest lessonRequest,
      HttpServletRequest request) {

    lessonService.updateLesson(lessonName, lessonRequest);
    final String message = SuccessMessage.UPDATE_ENTITY.getMessage("Lesson");
    return buildSuccessApiResponse(message, null, HttpStatus.OK, apiVersion, request);
  }

  @DeleteMapping("/{lessonName}")
  public ResponseEntity<Void> deleteLesson(@PathVariable String lessonName) {
    lessonService.deleteLesson(lessonName);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
