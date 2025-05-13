package org.protu.contentservice.course;

import jakarta.servlet.http.HttpServletRequest;
import org.protu.contentservice.common.enums.SuccessMessage;
import org.protu.contentservice.common.helpers.JwtHelper;
import org.protu.contentservice.common.properties.AppProperties;
import org.protu.contentservice.common.response.ApiResponse;
import org.protu.contentservice.lesson.Lesson;
import org.protu.contentservice.lesson.dto.LessonWithoutContent;
import org.protu.contentservice.lesson.dto.LessonsWithCompletion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.protu.contentservice.common.response.ApiResponseBuilder.buildSuccessApiResponse;

@RestController
@RequestMapping("/api/${app.api.version}/courses")
public class CourseController {

  private final CourseService courseService;
  private final String apiVersion;
  private final JwtHelper jwtHelper;

  public CourseController(CourseService courseService, AppProperties properties, JwtHelper jwtHelper) {
    this.courseService = courseService;
    apiVersion = properties.api().version();
    this.jwtHelper = jwtHelper;
  }

  private Long getUserIdFromBearer(String bearerToken) {
    String token = bearerToken.split(" ")[1];
    return jwtHelper.extractUserId(token);
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<CourseWithLessons>>> getAllCourses(HttpServletRequest request) {

    List<CourseWithLessons> courses = courseService.getAllCourses();
    final String message = SuccessMessage.GET_ALL_ENTITIES.getMessage("Courses");
    return buildSuccessApiResponse(message, courses, HttpStatus.OK, apiVersion, request);
  }

  @PostMapping
  public ResponseEntity<ApiResponse<Course>> createCourse(
      @Validated @RequestBody CourseRequest courseRequest,
      HttpServletRequest request) {

    courseService.createCourse(courseRequest);
    final String message = SuccessMessage.CREATE_NEW_ENTITY.getMessage("Course");
    return buildSuccessApiResponse(message, null, HttpStatus.CREATED, apiVersion, request);
  }

  @GetMapping("/{courseName}")
  public ResponseEntity<ApiResponse<CourseWithLessons>> getSingleCourse(
      @PathVariable String courseName,
      HttpServletRequest request) {

    CourseWithLessons course = courseService.getCourseByNameOrThrow(courseName);
    final String message = SuccessMessage.GET_SINGLE_ENTITY.getMessage("Course");
    return buildSuccessApiResponse(message, course, HttpStatus.OK, apiVersion, request);
  }

  @PatchMapping("/{courseName}")
  public ResponseEntity<ApiResponse<Course>> updateCourse(
      @PathVariable String courseName,
      @Validated @RequestBody CourseRequest courseRequest,
      HttpServletRequest request) {

    courseService.updateCourse(courseName, courseRequest);
    final String message = SuccessMessage.UPDATE_ENTITY.getMessage("Course");
    return buildSuccessApiResponse(message, null, HttpStatus.OK, apiVersion, request);
  }

  @DeleteMapping("/{courseName}")
  public ResponseEntity<Void> deleteCourse(@PathVariable String courseName) {

    courseService.deleteCourse(courseName);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PostMapping("/{courseName}")
  public ResponseEntity<ApiResponse<Course>> uploadCoursePic(
      @PathVariable String courseName,
      @RequestParam(name = "file") MultipartFile file,
      HttpServletRequest request) {

    courseService.uploadCoursePic(courseName, file);
    final String message = SuccessMessage.UPDATE_ENTITY.getMessage("Course");
    return buildSuccessApiResponse(message, null, HttpStatus.OK, apiVersion, request);
  }

  @GetMapping("/{courseName}/lessons")
  public ResponseEntity<ApiResponse<List<LessonWithoutContent>>> getAllLessonsForCourse(
      @PathVariable String courseName,
      HttpServletRequest request) {

    List<LessonWithoutContent> lessons = courseService.getAllLessonsForCourse(courseName);
    return buildSuccessApiResponse(SuccessMessage.GET_ALL_ENTITIES.getMessage("Lessons"), lessons, HttpStatus.OK, apiVersion, request);
  }

  @PostMapping("/{courseName}/lessons/{lessonName}")
  public ResponseEntity<ApiResponse<List<Lesson>>> addExistingLessonToCourse(
      @PathVariable String courseName,
      @PathVariable String lessonName,
      HttpServletRequest request) {

    courseService.addExistingLessonToCourse(courseName, lessonName);
    return buildSuccessApiResponse(SuccessMessage.ADD_ENTITY_TO_PARENT_ENTITY.getMessage(lessonName, "lesson", courseName, "course"), null, HttpStatus.OK, apiVersion, request);
  }

  @DeleteMapping("/{courseName}/lessons/{lessonName}")
  public ResponseEntity<Void> deleteLessonFromCourse(
      @PathVariable String courseName,
      @PathVariable String lessonName) {

    courseService.deleteLessonFromCourse(courseName, lessonName);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/{courseName}/lessons/progress")
  public ResponseEntity<ApiResponse<List<LessonsWithCompletion>>> getAllLessonsWithCompletionStatus(
      @PathVariable String courseName,
      @RequestHeader("Authorization") String bearer,
      HttpServletRequest request) {

    Long userId = getUserIdFromBearer(bearer);
    List<LessonsWithCompletion> lessons = courseService.getAllLessonsWithCompletionStatusForCourse(userId, courseName);
    final String message = SuccessMessage.GET_ALL_ENTITIES.getMessage("Lessons");
    return buildSuccessApiResponse(message, lessons, HttpStatus.OK, apiVersion, request);
  }
}
