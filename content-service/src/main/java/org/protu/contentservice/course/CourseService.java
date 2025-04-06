package org.protu.contentservice.course;

import lombok.RequiredArgsConstructor;
import org.protu.contentservice.common.exception.custom.EntityAlreadyExistsException;
import org.protu.contentservice.common.exception.custom.EntityNotFoundException;
import org.protu.contentservice.course.dto.CourseRequest;
import org.protu.contentservice.course.dto.CourseResponse;
import org.protu.contentservice.course.dto.CourseSummary;
import org.protu.contentservice.lesson.dto.LessonSummary;
import org.protu.contentservice.track.Track;
import org.protu.contentservice.track.TrackRepository;
import org.protu.contentservice.track.TrackService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CourseService {

  private final TrackService trackService;
  private final TrackRepository trackRepo;
  private final CourseRepository courseRepo;
  private final CourseMapper courseMapper;

  private List<CourseSummary> findAllCoursesWithLessonSummary(List<Object[]> queryResult) {
    Map<Integer, CourseSummary> courseMap = new HashMap<>();

    for (Object[] row : queryResult) {
      Integer courseId = (Integer) row[0];
      CourseSummary courseSummary = courseMap.computeIfAbsent(courseId,
          id -> new CourseSummary(id, (String) row[1], (String) row[2], (Timestamp) row[3], (Timestamp) row[4], new ArrayList<>()));

      courseMap.put(courseId, courseSummary);
      if (row[5] != null) {
        LessonSummary lessonSummary = new LessonSummary(
            (Integer) row[5],
            (String) row[6],
            (Integer) row[7],
            (Timestamp) row[8],
            (Timestamp) row[9]
        );

        courseSummary.lessons().add(lessonSummary);
      }
    }

    return new ArrayList<>(courseMap.values());
  }

  public Course fetchCourseByNameOrThrow(String courseName) {
    return courseRepo.findCourseByName(courseName).orElseThrow(() -> new EntityNotFoundException("Course", courseName));
  }

  public CourseResponse createCourse(CourseRequest courseRequest) {
    courseRepo.findCourseByName(courseRequest.name()).ifPresent(course -> {
      throw new EntityAlreadyExistsException(courseRequest.name());
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

  public List<CourseSummary> getAllCourses() {
    List<Object[]> queryResult = courseRepo.findAllProjectedBy();
    return findAllCoursesWithLessonSummary(queryResult);
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
    if (track.getCourses().remove(course)) {
      course.setTrack(null);
    }

    trackRepo.save(track);
    courseRepo.save(course);
  }
}
