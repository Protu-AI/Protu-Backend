package org.protu.contentservice.lesson;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.protu.contentservice.common.enums.SuccessMessage;
import org.protu.contentservice.common.properties.AppProperties;
import org.protu.contentservice.common.response.ApiResponse;
import org.protu.contentservice.lesson.dto.LessonRequest;
import org.protu.contentservice.lesson.dto.LessonResponse;
import org.protu.contentservice.lesson.dto.LessonUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.protu.contentservice.common.response.ApiResponseBuilder.buildApiResponse;

@RestController
@RequestMapping("/api/${app.api.version}")
@RequiredArgsConstructor
public class LessonController {

  private final LessonService lessonService;
  private final AppProperties properties;

  @PostMapping("/lessons")
  public ResponseEntity<ApiResponse<LessonResponse>> createLesson(@Validated @RequestBody LessonRequest lessonRequest, HttpServletRequest request) {
    LessonResponse lesson = lessonService.createLesson(lessonRequest);
    return buildApiResponse(SuccessMessage.CREATE_NEW_ENTITY.getMessage("Lesson"), lesson, null, HttpStatus.CREATED, properties.api().version(), request);
  }

  @GetMapping("/lessons/{lessonName}")
  public ResponseEntity<ApiResponse<LessonResponse>> getSingleLesson(@PathVariable String lessonName, HttpServletRequest request) {
    LessonResponse lesson = lessonService.getLessonByName(lessonName);
    return buildApiResponse(SuccessMessage.GET_SINGLE_ENTITY.getMessage("Lesson"), lesson, null, HttpStatus.OK, properties.api().version(), request);
  }

  @PatchMapping("/lessons/{lessonName}")
  public ResponseEntity<ApiResponse<LessonResponse>> updateLesson(@PathVariable String lessonName, @Validated @RequestBody LessonUpdateRequest lessonRequest, HttpServletRequest request) {
    LessonResponse lesson = lessonService.updateLesson(lessonName, lessonRequest);
    return buildApiResponse(SuccessMessage.UPDATE_ENTITY.getMessage("Lesson"), lesson, null, HttpStatus.OK, properties.api().version(), request);
  }

  @GetMapping("/courses/{courseName}/lessons")
  public ResponseEntity<ApiResponse<List<LessonResponse>>> getAllLessonsForCourse(@PathVariable String courseName, HttpServletRequest request) {
    List<LessonResponse> lessons = lessonService.getAllLessonsForCourse(courseName);
    return buildApiResponse(SuccessMessage.GET_ALL_ENTITIES.getMessage("Lessons"), lessons, null, HttpStatus.OK, properties.api().version(), request);
  }

  @PostMapping("/courses/{courseName}/lessons/{lessonName}")
  public ResponseEntity<ApiResponse<List<LessonResponse>>> addExistingLessonToCourse(@PathVariable String courseName, @PathVariable String lessonName, HttpServletRequest request) {
    lessonService.addExistingLessonToCourse(courseName, lessonName);
    return buildApiResponse(SuccessMessage.ADD_ENTITY_TO_PARENT_ENTITY.getMessage(lessonName, "lesson", courseName, "course"), null, null, HttpStatus.OK, properties.api().version(), request);
  }

  @DeleteMapping("/courses/{courseName}/lessons/{lessonName}")
  public ResponseEntity<Void> deleteLessonFromCourse(@PathVariable String courseName, @PathVariable String lessonName) {
    lessonService.deleteLessonFromCourse(courseName, lessonName);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
