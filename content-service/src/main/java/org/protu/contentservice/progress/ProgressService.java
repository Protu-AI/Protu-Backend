package org.protu.contentservice.progress;

import lombok.RequiredArgsConstructor;
import org.protu.contentservice.common.exception.custom.EntityNotFoundException;
import org.protu.contentservice.course.Course;
import org.protu.contentservice.course.CourseRepository;
import org.protu.contentservice.lesson.Lesson;
import org.protu.contentservice.lesson.LessonHelper;
import org.protu.contentservice.lesson.LessonRepository;
import org.protu.contentservice.progress.dto.UserProgressInCourse;
import org.protu.contentservice.progress.user.User;
import org.protu.contentservice.progress.user.UserHelper;
import org.protu.contentservice.progress.usercourse.UserCourseRepository;
import org.protu.contentservice.progress.usercourse.UserNotEnrolledInCourseException;
import org.protu.contentservice.progress.usercourse.UsersCourses;
import org.protu.contentservice.progress.usercourse.UsersCoursesPK;
import org.protu.contentservice.progress.userlesson.UserLessonRepository;
import org.protu.contentservice.progress.userlesson.UsersLessons;
import org.protu.contentservice.progress.userlesson.UsersLessonsPK;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgressService {

  private final UserCourseRepository userCourseRepo;
  private final LessonRepository lessonRepo;
  private final CourseRepository courseRepository;
  private final UserLessonRepository userLessonRepository;
  private final UserHelper userHelper;
  private final LessonHelper lessonHelper;

  private UsersLessons buildUserLessons(Long userId, Integer lessonId) {
    User user = userHelper.fetchUserByIdOrThrow(userId);
    Lesson lesson = lessonHelper.fetchLessonByIdOrThrow(lessonId);
    return userLessonRepository.findById(new UsersLessonsPK(userId, lessonId))
        .orElseGet(() -> UsersLessons.builder().user(user).lesson(lesson).build());
  }

  public UserProgressInCourse getUserProgressInCourse(Long userId, Integer courseId) {
    int completedLessons = userCourseRepo.getCompletedLessonsByUserForCourse(userId, courseId);
    int totalNumberOfLessons = lessonRepo.findNumberOfLessonsForCourseWithId(courseId);
    return new UserProgressInCourse(courseId, completedLessons, totalNumberOfLessons);
  }

  public UsersCourses enrollUserInCourse(Long userId, Integer courseId) {
    UsersCoursesPK usersCoursesPK = new UsersCoursesPK(userId, courseId);
    User user = userHelper.fetchUserByIdOrThrow(userId);
    Course course1 = courseRepository.findById(courseId).orElseThrow(() -> new EntityNotFoundException("Course", courseId));
    UsersCourses usersCourses = userCourseRepo.findById(usersCoursesPK)
        .orElseGet(() -> UsersCourses.builder().id(usersCoursesPK).completedLessons(0).user(user).course(course1).build());
    return userCourseRepo.save(usersCourses);
  }

  public void cancelUserEnrollmentInCourse(Long userId, Integer courseId) {
    UsersCourses usersCourses = userCourseRepo.findById(new UsersCoursesPK(userId, courseId))
        .orElseThrow(UserNotEnrolledInCourseException::new);
    usersCourses.setCompletedLessons(0);
    userCourseRepo.save(usersCourses);
  }

  public void incrementCompletedLessonsForUser(Long userId, Integer courseId) {
    UsersCoursesPK usersCoursesPK = new UsersCoursesPK(userId, courseId);
    UsersCourses usersCourses = userCourseRepo.findById(usersCoursesPK).orElseGet(() -> enrollUserInCourse(userId, courseId));
    int totalNumberOfLessons = lessonRepo.findNumberOfLessonsForCourseWithId(courseId);
    if (totalNumberOfLessons == usersCourses.getCompletedLessons()) {
      return;
    }
    usersCourses.setCompletedLessons(usersCourses.getCompletedLessons() + 1);
    userCourseRepo.save(usersCourses);
  }

  public void decrementCompletedLessonsForUser(Long userId, Integer courseId) {
    UsersCoursesPK usersCoursesPK = new UsersCoursesPK(userId, courseId);
    UsersCourses usersCourses = userCourseRepo.findById(usersCoursesPK)
        .orElseThrow(UserNotEnrolledInCourseException::new);
    if (usersCourses.getCompletedLessons() == 0) {
      return;
    }
    usersCourses.setCompletedLessons(usersCourses.getCompletedLessons() - 1);
    userCourseRepo.save(usersCourses);
  }

  public void markLessonCompleted(Long userId, Integer lessonId) {
    UsersLessons userLesson = buildUserLessons(userId, lessonId);
    userLesson.setIsCompleted(true);
    userLessonRepository.save(userLesson);
  }

  public void markLessonNotCompleted(Long userId, Integer lessonId) {
    UsersLessons userLesson = buildUserLessons(userId, lessonId);
    userLesson.setIsCompleted(false);
    userLessonRepository.save(userLesson);
  }
}
