package org.protu.contentservice.progress;

import org.protu.contentservice.course.CourseService;
import org.protu.contentservice.course.CourseWithLessons;
import org.protu.contentservice.lesson.Lesson;
import org.protu.contentservice.lesson.LessonService;
import org.protu.contentservice.progress.dto.UserProgressInCourse;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProgressService {

  private final CourseService courseService;
  private final LessonService lessonService;
  private final UserReplicaService userReplicaService;
  private final JdbcClient jdbcClient;

  public ProgressService(CourseService courseService, LessonService lessonService, UserReplicaService userReplicaService, JdbcClient jdbcClient) {
    this.courseService = courseService;
    this.lessonService = lessonService;
    this.userReplicaService = userReplicaService;
    this.jdbcClient = jdbcClient;
  }

  private int getTotalNumberOfLessonsInCourse(Integer courseId) {
    Optional<Integer> countOpt = jdbcClient.sql("SELECT COUNT(*) FROM courses_lessons AS cl WHERE cl.course_id = :courseId")
        .param("courseId", courseId)
        .query(Integer.class)
        .optional();

    return countOpt.orElseThrow(() -> new RuntimeException("No lessons found for this course"));
  }

  private int getTotalNumberOfCompletedLessonsInCourse(Long userId, Integer courseId) {
    Optional<Integer> countOpt = jdbcClient.sql("SELECT completed_lessons FROM users_courses WHERE user_id = :userId AND course_id = :courseId")
        .param("userId", userId)
        .param("courseId", courseId)
        .query(Integer.class)
        .optional();

    return countOpt.orElseThrow(() -> new RuntimeException("This course is not enrolled for this user"));
  }

  public UserProgressInCourse getUserProgressInCourse(Long userId, String courseName) {
    userReplicaService.getUserById(userId);
    CourseWithLessons course = courseService.getCourseByNameOrThrow(courseName);

    int completedLessons = getTotalNumberOfCompletedLessonsInCourse(userId, course.id());
    int totalLessons = getTotalNumberOfLessonsInCourse(course.id());
    return new UserProgressInCourse(course.id(), completedLessons, totalLessons);
  }

  public void enrollUserInCourse(Long userId, String courseName) {
    userReplicaService.getUserById(userId);
    CourseWithLessons course = courseService.getCourseByNameOrThrow(courseName);

    jdbcClient.sql("INSERT INTO users_courses (user_id, course_id, completed_lessons) VALUES (:userId, :courseId, 0)")
        .param("userId", userId)
        .param("courseId", course.id())
        .update();
  }

  public void cancelUserEnrollmentInCourse(Long userId, String courseName) {
    userReplicaService.getUserById(userId);
    CourseWithLessons course = courseService.getCourseByNameOrThrow(courseName);

    jdbcClient.sql("DELETE FROM users_courses WHERE user_id = :userId AND course_id = :courseId")
        .param("userId", userId)
        .param("courseId", course.id())
        .update();
  }

  public void incrementCompletedLessonsForUser(Long userId, String courseName) {
    userReplicaService.getUserById(userId);
    CourseWithLessons course = courseService.getCourseByNameOrThrow(courseName);

    int totalNumberOfLessons = getTotalNumberOfLessonsInCourse(course.id());
    int totalNumberOfCompletedLessons = getTotalNumberOfCompletedLessonsInCourse(userId, course.id());

    if (totalNumberOfCompletedLessons < totalNumberOfLessons) {
      jdbcClient.sql("UPDATE users_courses SET completed_lessons = completed_lessons + 1 WHERE user_id = :userId AND course_id = :courseId")
          .param("userId", userId)
          .param("courseId", course.id())
          .update();
    }
  }

  public void decrementCompletedLessonsForUser(Long userId, String courseName) {
    userReplicaService.getUserById(userId);
    CourseWithLessons course = courseService.getCourseByNameOrThrow(courseName);

    int totalNumberOfCompletedLessons = getTotalNumberOfCompletedLessonsInCourse(userId, course.id());
    if (totalNumberOfCompletedLessons > 0) {
      jdbcClient.sql("UPDATE users_courses SET completed_lessons = completed_lessons - 1 WHERE user_id = :userId AND course_id = :courseId")
          .param("userId", userId)
          .param("courseId", course.id())
          .update();
    }
  }

  public void markLessonCompleted(Long userId, String lessonName) {
    userReplicaService.getUserById(userId);
    Lesson lesson = lessonService.getLessonByName(lessonName);

    jdbcClient.sql("INSERT INTO users_lessons (user_id, lesson_id, is_completed) VALUES (:userId, :lessonId, :isCompleted) ON CONFLICT (user_id, lesson_id) DO UPDATE SET is_completed = EXCLUDED.is_completed;")
        .param("userId", userId)
        .param("lessonId", lesson.id())
        .param("isCompleted", true)
        .update();
  }

  public void markLessonNotCompleted(Long userId, String lessonName) {
    userReplicaService.getUserById(userId);
    Lesson lesson = lessonService.getLessonByName(lessonName);

    jdbcClient.sql("INSERT INTO users_lessons (user_id, lesson_id, is_completed) VALUES (:userId, :lessonId, :isCompleted) ON CONFLICT (user_id, lesson_id) DO UPDATE SET is_completed = EXCLUDED.is_completed;")
        .param("userId", userId)
        .param("lessonId", lesson.id())
        .param("isCompleted", false)
        .update();
  }
}
