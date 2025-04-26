package org.protu.contentservice.progress;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.protu.contentservice.course.Course;
import org.protu.contentservice.course.CourseService;
import org.protu.contentservice.lesson.Lesson;
import org.protu.contentservice.lesson.LessonHelper;
import org.protu.contentservice.lesson.LessonRepository;
import org.protu.contentservice.lesson.dto.LessonsWithCompletion;
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

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProgressService {

  private final UserCourseRepository userCourseRepo;
  private final LessonRepository lessonRepo;
  private final CourseService courseService;
  private final UserLessonRepository userLessonRepository;
  private final UserHelper userHelper;
  private final LessonHelper lessonHelper;

  private UsersLessons buildUserLessons(Long userId, String lessonName) {
    User user = userHelper.fetchUserByIdOrThrow(userId);
    Lesson lesson = lessonHelper.fetchLessonByNameOrThrow(lessonName);

    UsersLessonsPK pk = new UsersLessonsPK(userId, lesson.getId());
    return userLessonRepository.findById(pk)
        .orElseGet(() -> UsersLessons.builder()
            .id(pk)
            .user(user)
            .lesson(lesson)
            .isCompleted(false).build());
  }

  public UserProgressInCourse getUserProgressInCourse(Long userId, String courseName) {
    userHelper.checkIfUserExistsOrThrow(userId);
    Course course = courseService.fetchCourseByNameOrThrow(courseName);

    int completedLessons = userCourseRepo.getCompletedLessonsByUserForCourse(userId, course.getId());
    int totalNumberOfLessons = lessonRepo.findNumberOfLessonsForCourseWithId(course.getId());
    return new UserProgressInCourse(course.getId(), completedLessons, totalNumberOfLessons);
  }

  @Transactional
  public void enrollUserInCourse(Long userId, String courseName) {
    Course course = courseService.fetchCourseByNameOrThrow(courseName);
    User user = userHelper.fetchUserByIdOrThrow(userId);

    UsersCoursesPK pk = new UsersCoursesPK(userId, course.getId());
    if (!userCourseRepo.existsById(pk)) {
      UsersCourses usersCourses = UsersCourses.builder()
          .id(pk)
          .completedLessons(0)
          .user(user)
          .course(course).build();
      userCourseRepo.save(usersCourses);
    }
  }

  @Transactional
  public void cancelUserEnrollmentInCourse(Long userId, String courseName) {
    Course course = courseService.fetchCourseByNameOrThrow(courseName);
    userHelper.checkIfUserExistsOrThrow(userId);

    Optional<UsersCourses> usersCoursesOpt = userCourseRepo.findById(new UsersCoursesPK(userId, course.getId()));
    if (usersCoursesOpt.isEmpty())
      return;
    
    usersCoursesOpt.get().setCompletedLessons(0);
    userCourseRepo.save(usersCoursesOpt.get());
  }

  @Transactional
  public void incrementCompletedLessonsForUser(Long userId, String courseName) {
    Course course = courseService.fetchCourseByNameOrThrow(courseName);
    User user = userHelper.fetchUserByIdOrThrow(userId);

    UsersCoursesPK pk = new UsersCoursesPK(userId, course.getId());
    UsersCourses usersCourses = userCourseRepo.findById(pk)
        .orElseGet(() -> UsersCourses.builder()
            .id(pk)
            .completedLessons(0)
            .user(user)
            .course(course).build());

    int totalNumberOfLessons = lessonRepo.findNumberOfLessonsForCourseWithId(course.getId());
    if (usersCourses.getCompletedLessons() < totalNumberOfLessons) {
      usersCourses.setCompletedLessons(usersCourses.getCompletedLessons() + 1);
      userCourseRepo.save(usersCourses);
    }
  }

  @Transactional
  public void decrementCompletedLessonsForUser(Long userId, String courseName) {
    Course course = courseService.fetchCourseByNameOrThrow(courseName);
    userHelper.checkIfUserExistsOrThrow(userId);

    UsersCoursesPK pk = new UsersCoursesPK(userId, course.getId());
    UsersCourses usersCourses = userCourseRepo.findById(pk)
        .orElseThrow(UserNotEnrolledInCourseException::new);

    if (usersCourses.getCompletedLessons() == 0) {
      return;
    }
    usersCourses.setCompletedLessons(usersCourses.getCompletedLessons() - 1);
    userCourseRepo.save(usersCourses);
  }

  @Transactional
  public void markLessonCompleted(Long userId, String lessonName) {
    UsersLessons userLesson = buildUserLessons(userId, lessonName);
    userLesson.setIsCompleted(true);
    userLessonRepository.save(userLesson);
  }

  @Transactional
  public void markLessonNotCompleted(Long userId, String lessonName) {
    UsersLessons userLesson = buildUserLessons(userId, lessonName);
    userLesson.setIsCompleted(false);
    userLessonRepository.save(userLesson);
  }

  public List<LessonsWithCompletion> getAllLessonsWithCompletionStatus(Long userId, String courseName) {
    Course course = courseService.fetchCourseByNameOrThrow(courseName);
    return lessonRepo.findLessonsWithCompletionStatus(userId, course.getId());
  }
}
