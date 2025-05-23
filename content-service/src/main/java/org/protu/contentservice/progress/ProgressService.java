package org.protu.contentservice.progress;

import org.protu.contentservice.common.exception.custom.LessonAlreadyCompletedException;
import org.protu.contentservice.common.exception.custom.LessonAlreadyNotCompletedException;
import org.protu.contentservice.course.CourseDto;
import org.protu.contentservice.course.CourseService;
import org.protu.contentservice.lesson.LessonService;
import org.protu.contentservice.lesson.dto.LessonWithoutContent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressService {

  private final CourseService courseService;
  private final LessonService lessonService;
  private final UserReplicaService userReplicaService;
  private final ProgressRepository progressRepo;

  public ProgressService(CourseService courseService, LessonService lessonService, UserReplicaService userReplicaService, ProgressRepository progressRepo) {
    this.courseService = courseService;
    this.lessonService = lessonService;
    this.userReplicaService = userReplicaService;
    this.progressRepo = progressRepo;
  }

  @Transactional(readOnly = true)
  public UserCourseProgress getUserProgressInCourse(Long userId, String courseName) {
    userReplicaService.getUserById(userId);
    CourseDto course = courseService.getCourseByNameOrThrow(courseName);

    int completedLessons = progressRepo.getTotalNumberOfCompletedLessonsInCourse(userId, course.id());
    int totalLessons = progressRepo.getNumberOfLessonsInCourse(course.id());
    return new UserCourseProgress(course.id(), completedLessons, totalLessons);
  }

  @Transactional
  public void enrollUserInCourse(Long userId, String courseName) {
    userReplicaService.getUserById(userId);
    CourseDto course = courseService.getCourseByNameOrThrow(courseName);
    progressRepo.addCourseForUser(userId, course.id());
  }

  @Transactional
  public void cancelUserEnrollmentInCourse(Long userId, String courseName) {
    userReplicaService.getUserById(userId);
    CourseDto course = courseService.getCourseByNameOrThrow(courseName);
    progressRepo.removeCourseForUser(userId, course.id());
  }

  private boolean markLessonCompleted(Long userId, String lessonName) {
    LessonWithoutContent lesson = lessonService.findByNameWithoutContent(lessonName);
    return progressRepo.markLessonCompleted(userId, lesson.id());
  }

  private boolean markLessonNotCompleted(Long userId, String lessonName) {
    LessonWithoutContent lesson = lessonService.findByNameWithoutContent(lessonName);
    return progressRepo.markLessonUncompleted(userId, lesson.id());
  }

  @Transactional
  public void incrementCompletedLessonsByUser(Long userId, String courseName, String lessonName) {
    userReplicaService.getUserById(userId);
    CourseDto course = courseService.getCourseByNameOrThrow(courseName);

    if (!markLessonCompleted(userId, lessonName)) {
      throw new LessonAlreadyCompletedException();
    }

    int lessonsCount = progressRepo.getNumberOfLessonsInCourse(course.id());
    int completedLessonsCount = progressRepo.getTotalNumberOfCompletedLessonsInCourse(userId, course.id());

    if (completedLessonsCount < lessonsCount) {
      progressRepo.incrementCompletedLessonByUser(userId, course.id());
    }
  }

  @Transactional
  public void decrementCompletedLessonsByUser(Long userId, String courseName, String lessonName) {
    userReplicaService.getUserById(userId);
    CourseDto course = courseService.getCourseByNameOrThrow(courseName);

    if (!markLessonNotCompleted(userId, lessonName)) {
      throw new LessonAlreadyNotCompletedException();
    }

    int completedLessonsCount = progressRepo.getTotalNumberOfCompletedLessonsInCourse(userId, course.id());
    if (completedLessonsCount > 0) {
      progressRepo.decrementCompletedLessonsByUser(userId, course.id());
    }
  }
}
