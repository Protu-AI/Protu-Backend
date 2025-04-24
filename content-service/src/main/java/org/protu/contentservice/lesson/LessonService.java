package org.protu.contentservice.lesson;

import lombok.RequiredArgsConstructor;
import org.protu.contentservice.common.exception.custom.EntityAlreadyExistsException;
import org.protu.contentservice.course.Course;
import org.protu.contentservice.course.CourseRepository;
import org.protu.contentservice.course.CourseService;
import org.protu.contentservice.lesson.dto.LessonRequest;
import org.protu.contentservice.lesson.dto.LessonResponse;
import org.protu.contentservice.lesson.dto.LessonUpdateRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LessonService {

  private final CourseService courseService;
  private final CourseRepository courseRepo;
  private final LessonRepository lessonRepo;
  private final LessonHelper lessonHelper;
  private final LessonMapper lessonMapper;

  public LessonResponse createLesson(LessonRequest lessonRequest) {
    lessonRepo.findLessonByName(lessonRequest.name()).ifPresent(lesson -> {
      throw new EntityAlreadyExistsException("Lesson", lessonRequest.name());
    });

    Lesson lesson = lessonMapper.toLessonEntity(lessonRequest);
    lessonRepo.save(lesson);
    return lessonMapper.toLessonDto(lesson);
  }

  public LessonResponse getLessonByName(String lessonName) {
    Lesson lesson = lessonHelper.fetchLessonByNameOrThrow(lessonName);
    return lessonMapper.toLessonDto(lesson);
  }

  public LessonResponse updateLesson(String lessonName, LessonUpdateRequest lessonRequest) {
    Lesson lesson = lessonHelper.fetchLessonByNameOrThrow(lessonName);
    Optional.ofNullable(lessonRequest.name()).ifPresent(lesson::setName);
    Optional.ofNullable(lessonRequest.content()).ifPresent(lesson::setContent);
    Optional.ofNullable(lessonRequest.lessonOrder()).ifPresent(lesson::setLessonOrder);
    lessonRepo.save(lesson);
    return lessonMapper.toLessonDto(lesson);
  }

  public List<LessonResponse> getAllLessonsForCourse(String courseName) {
    Course course = courseService.fetchCourseByNameOrThrow(courseName);
    return lessonMapper.toLessonDtoList(course.getLessons());
  }

  public void addExistingLessonToCourse(String courseName, String lessonName) {
    Course course = courseService.fetchCourseByNameOrThrow(courseName);
    Lesson lesson = lessonHelper.fetchLessonByNameOrThrow(lessonName);
    lesson.setCourse(course);
    course.getLessons().add(lesson);
    courseRepo.save(course);
  }

  public void deleteLessonFromCourse(String courseName, String lessonName) {
    Course course = courseService.fetchCourseByNameOrThrow(courseName);
    Lesson lesson = lessonHelper.fetchLessonByNameOrThrow(lessonName);
    if (course.getLessons().remove(lesson)) {
      lesson.setCourse(null);
    }

    courseRepo.save(course);
    lessonRepo.save(lesson);
  }
}
