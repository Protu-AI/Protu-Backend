package org.protu.contentservice.course;

import jakarta.servlet.http.HttpServletRequest;
import org.protu.contentservice.common.enums.SuccessMessage;
import org.protu.contentservice.common.properties.AppProperties;
import org.protu.contentservice.common.response.ApiResponse;
import org.protu.contentservice.course.dto.CourseRequest;
import org.protu.contentservice.course.dto.CourseResponse;
import org.protu.contentservice.course.dto.CourseSummary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.protu.contentservice.common.response.ApiResponseBuilder.buildApiResponse;

@RestController
@RequestMapping("/api/${app.api.version}")
public class CourseController {

  private final CourseService courseService;
  private final String apiVersion;

  public CourseController(CourseService courseService, AppProperties properties) {
    this.courseService = courseService;
    apiVersion = properties.api().version();
  }

  @GetMapping("/courses")
  public ResponseEntity<ApiResponse<List<CourseSummary>>> getAllCourses(HttpServletRequest request) {
    List<CourseSummary> courses = courseService.getAllCourses();
    return buildApiResponse(SuccessMessage.GET_ALL_ENTITIES.getMessage("Courses"), courses, null, HttpStatus.OK, apiVersion, request);
  }

  @PostMapping("/courses")
  public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@Validated @RequestBody CourseRequest courseRequest, HttpServletRequest request) {
    CourseResponse course = courseService.createCourse(courseRequest);
    return buildApiResponse(SuccessMessage.CREATE_NEW_ENTITY.getMessage("Course"), course, null, HttpStatus.CREATED, apiVersion, request);
  }

  @GetMapping("/courses/{courseName}")
  public ResponseEntity<ApiResponse<CourseResponse>> getSingleCourse(@PathVariable String courseName, HttpServletRequest request) {
    CourseResponse course = courseService.getCourseByName(courseName);
    return buildApiResponse(SuccessMessage.GET_SINGLE_ENTITY.getMessage("Course"), course, null, HttpStatus.OK, apiVersion, request);
  }

  @PatchMapping("/courses/{courseName}")
  public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(@PathVariable String courseName, @Validated @RequestBody CourseRequest courseRequest, HttpServletRequest request) {
    CourseResponse course = courseService.updateCourse(courseName, courseRequest);
    return buildApiResponse(SuccessMessage.UPDATE_ENTITY.getMessage("Course"), course, null, HttpStatus.OK, apiVersion, request);
  }

  @PostMapping("/courses/{courseName}")
  public ResponseEntity<ApiResponse<CourseResponse>> uploadCoursePic(
      @PathVariable String courseName,
      @RequestParam(name = "file") MultipartFile file,
      HttpServletRequest request) {
    CourseResponse course = courseService.uploadCoursePic(courseName, file);
    return buildApiResponse(SuccessMessage.UPDATE_ENTITY.getMessage("Course"), course, null, HttpStatus.OK, apiVersion, request);
  }

  @GetMapping("/tracks/{trackName}/courses")
  public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCoursesForTrack(@PathVariable String trackName, HttpServletRequest request) {
    List<CourseResponse> courses = courseService.getAllCoursesForTrack(trackName);
    return buildApiResponse(SuccessMessage.GET_ALL_ENTITIES.getMessage("Courses"), courses, null, HttpStatus.OK, apiVersion, request);
  }

  @PostMapping("/tracks/{trackName}/courses/{courseName}")
  public ResponseEntity<ApiResponse<List<CourseResponse>>> addExistingCourseToTrack(@PathVariable String trackName, @PathVariable String courseName, HttpServletRequest request) {
    List<CourseResponse> courses = courseService.addExistingCourseToTrack(trackName, courseName);
    return buildApiResponse(SuccessMessage.ADD_ENTITY_TO_PARENT_ENTITY.getMessage(courseName, "course", trackName, "track"), courses, null, HttpStatus.OK, apiVersion, request);
  }

  @DeleteMapping("/tracks/{trackName}/courses/{courseName}")
  public ResponseEntity<Void> deleteCourseFromTrack(@PathVariable String trackName, @PathVariable String courseName) {
    courseService.deleteCourseFromTrack(trackName, courseName);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
