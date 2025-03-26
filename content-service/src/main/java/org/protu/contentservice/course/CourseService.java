package org.protu.contentservice.course;

import lombok.RequiredArgsConstructor;
import org.protu.contentservice.common.enums.FailureMessage;
import org.protu.contentservice.course.dto.CourseRequest;
import org.protu.contentservice.course.dto.CourseResponse;
import org.protu.contentservice.track.Track;
import org.protu.contentservice.track.TrackRepository;
import org.protu.contentservice.track.TrackService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseService {

  private final TrackService trackService;
  private final TrackRepository trackRepo;
  private final CourseRepository courseRepo;
  private final CourseMapper courseMapper;

  private Course fetchCourseByNameOrThrow(String courseName) {
    return courseRepo.findCourseByName(courseName).orElseThrow(() -> new RuntimeException(FailureMessage.ENTITY_NOT_FOUND.getMessage("Course", courseName)));
  }

  public CourseResponse createCourse(CourseRequest courseRequest) {
    courseRepo.findCourseByName(courseRequest.name()).ifPresent(course -> {
      throw new RuntimeException(FailureMessage.ENTITY_ALREADY_EXISTS.getMessage(courseRequest.name()));
    });

    Course course = courseMapper.toCourseEntity(courseRequest);
    courseRepo.save(course);
    return courseMapper.toCourseDto(course);
  }

  public CourseResponse getCourseByName(String courseName) {
    Course course = fetchCourseByNameOrThrow(courseName);
    return courseMapper.toCourseDto(course);
  }

  public CourseResponse updateCourse(String courseName, CourseRequest courseRequest) {
    Course course = fetchCourseByNameOrThrow(courseName);
    Optional.ofNullable(courseRequest.name()).ifPresent(course::setName);
    Optional.ofNullable(courseRequest.description()).ifPresent(course::setDescription);
    courseRepo.save(course);
    return courseMapper.toCourseDto(course);
  }

  public List<CourseResponse> getAllCourses() {
    List<Course> courses = courseRepo.findAll();
    return courseMapper.toCourseDtoList(courses);
  }

  public List<CourseResponse> getAllCoursesForTrack(String trackName) {
    Track track = trackService.fetchTrackByNameOrThrow(trackName);
    return courseMapper.toCourseDtoList(track.getCourses());
  }

  public List<CourseResponse> addExistingCourseToTrack(String trackName, String courseName) {
    Track track = trackService.fetchTrackByNameOrThrow(trackName);
    Course course = fetchCourseByNameOrThrow(courseName);
    track.getCourses().add(course);
    course.setTrack(track);
    trackRepo.save(track);
    return courseMapper.toCourseDtoList(track.getCourses());
  }

  public void deleteCourseFromTrack(String trackName, String courseName) {
    Track track = trackService.fetchTrackByNameOrThrow(trackName);
    Course course = fetchCourseByNameOrThrow(courseName);
    track.getCourses().remove(course);
    course.setTrack(null);
    trackRepo.save(track);
  }
}
