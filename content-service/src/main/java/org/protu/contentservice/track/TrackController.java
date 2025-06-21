package org.protu.contentservice.track;

import jakarta.servlet.http.HttpServletRequest;
import org.protu.contentservice.common.enums.SuccessMessage;
import org.protu.contentservice.common.properties.AppProperties;
import org.protu.contentservice.common.response.ApiResponse;
import org.protu.contentservice.course.Course;
import org.protu.contentservice.course.CourseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.protu.contentservice.common.response.ApiResponseBuilder.buildSuccessApiResponse;

@RestController
@RequestMapping("/api/${app.api.version}/tracks")
public class TrackController {

  private final TrackService trackService;
  private final String apiVersion;

  public TrackController(TrackService trackService, AppProperties properties) {
    this.trackService = trackService;
    this.apiVersion = properties.api().version();
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<TrackWithCourses>>> getAllTracks(HttpServletRequest request) {
    List<TrackWithCourses> tracks = trackService.getAllTracks();
    final String message = SuccessMessage.GET_ALL_ENTITIES.getMessage("Tracks");
    return buildSuccessApiResponse(message, tracks, HttpStatus.OK, apiVersion, request);
  }

  @GetMapping("/{trackName}")
  public ResponseEntity<ApiResponse<TrackWithCourses>> getSingleTrack(
      @PathVariable String trackName,
      HttpServletRequest request) {

    TrackWithCourses trackResponse = trackService.getTrackByName(trackName);
    final String message = SuccessMessage.GET_SINGLE_ENTITY.getMessage("Track");
    return buildSuccessApiResponse(message, trackResponse, HttpStatus.OK, apiVersion, request);
  }

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createTrack(
      @RequestBody @Validated TrackRequest trackRequest,
      HttpServletRequest request) {

    trackService.createTrackIfNotExists(trackRequest);
    final String message = SuccessMessage.CREATE_NEW_ENTITY.getMessage("Track");
    return buildSuccessApiResponse(message, null, HttpStatus.CREATED, apiVersion, request);
  }

  @PatchMapping("/{trackName}")
  public ResponseEntity<ApiResponse<Void>> updateTrack(
      @PathVariable String trackName,
      @RequestBody @Validated TrackRequest trackRequest,
      HttpServletRequest request) {

    trackService.updateTrack(trackName, trackRequest);
    final String message = SuccessMessage.UPDATE_ENTITY.getMessage("Track");
    return buildSuccessApiResponse(message, null, HttpStatus.OK, apiVersion, request);
  }

  @DeleteMapping("/{trackName}")
  public ResponseEntity<Void> deleteTrack(@PathVariable String trackName) {
    trackService.deleteTrack(trackName);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @GetMapping("/{trackName}/courses")
  public ResponseEntity<ApiResponse<List<CourseDto>>> getAllCoursesForTrack(
      @PathVariable String trackName,
      HttpServletRequest request) {

    List<CourseDto> courses = trackService.getAllCoursesForTrack(trackName);
    final String message = SuccessMessage.GET_ALL_ENTITIES.getMessage("Courses");
    return buildSuccessApiResponse(message, courses, HttpStatus.OK, apiVersion, request);
  }

  @PostMapping("/{trackName}/courses/{courseName}")
  public ResponseEntity<ApiResponse<List<Course>>> addExistingCourseToTrack(
      @PathVariable String trackName,
      @PathVariable String courseName,
      HttpServletRequest request) {

    trackService.addExistingCourseToTrack(trackName, courseName);
    final String message = SuccessMessage.ADD_ENTITY_TO_PARENT_ENTITY.getMessage(courseName, "course", trackName, "track");
    return buildSuccessApiResponse(message, null, HttpStatus.OK, apiVersion, request);
  }

  @DeleteMapping("/{trackName}/courses/{courseName}")
  public ResponseEntity<Void> deleteCourseFromTrack(
      @PathVariable String trackName,
      @PathVariable String courseName) {

    trackService.deleteCourseFromTrack(trackName, courseName);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
