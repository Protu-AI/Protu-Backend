package org.protu.contentservice.progress;

import org.protu.contentservice.common.exception.custom.UserNotEnrolledInCourseException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ProgressRepository {

  private final JdbcClient jdbcClient;

  public ProgressRepository(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public int getNumberOfLessonsInCourse(Integer courseId) {
    Optional<Integer> countOpt = jdbcClient.sql("""
            SELECT COUNT(*)
            FROM courses_lessons AS cl
            WHERE cl.course_id = :courseId
            """)
        .param("courseId", courseId)
        .query(Integer.class)
        .optional();

    return countOpt.orElseThrow(() -> new RuntimeException("No lessons found for this course"));
  }

  public int getTotalNumberOfCompletedLessonsInCourse(Long userId, Integer courseId) {
    Optional<Integer> countOpt = jdbcClient.sql("""
            SELECT completed_lessons
            FROM users_courses
            WHERE user_id = :userId AND course_id = :courseId
            """)
        .param("userId", userId)
        .param("courseId", courseId)
        .query(Integer.class)
        .optional();

    return countOpt.orElseThrow(UserNotEnrolledInCourseException::new);
  }

  public void addCourseForUser(Long userId, Integer courseId) {
    jdbcClient.sql("""
            INSERT INTO users_courses (user_id, course_id, completed_lessons)
            VALUES (:userId, :courseId, 0)
            ON CONFLICT (user_id, course_id) DO NOTHING""")
        .param("userId", userId)
        .param("courseId", courseId)
        .update();
  }

  public void removeCourseForUser(Long userId, Integer courseId) {
    jdbcClient.sql("""
            DELETE FROM users_courses
            WHERE user_id = :userId AND course_id = :courseId
            """)
        .param("userId", userId)
        .param("courseId", courseId)
        .update();
  }

  public void incrementCompletedLessonByUser(Long userId, Integer courseId) {
    jdbcClient.sql("""
            UPDATE users_courses
            SET completed_lessons = completed_lessons + 1
            WHERE user_id = :userId AND course_id = :courseId
            """)
        .param("userId", userId)
        .param("courseId", courseId)
        .update();
  }

  public void decrementCompletedLessonsByUser(Long userId, Integer courseId) {
    jdbcClient.sql("""
            UPDATE users_courses
            SET completed_lessons = completed_lessons - 1
            WHERE user_id = :userId AND course_id = :courseId
            """)
        .param("userId", userId)
        .param("courseId", courseId)
        .update();
  }

  public boolean markLessonCompleted(Long userId, Integer lessonId) {
    int count = jdbcClient.sql("""
            INSERT INTO users_lessons (user_id, lesson_id, is_completed)
            VALUES (:userId, :lessonId, :isCompleted)
            ON CONFLICT (user_id, lesson_id) DO
            UPDATE SET is_completed = TRUE
            WHERE users_lessons.is_completed = FALSE;
            """)
        .param("userId", userId)
        .param("lessonId", lessonId)
        .param("isCompleted", true)
        .update();

    return count >= 1;
  }

  public boolean markLessonUncompleted(Long userId, Integer lessonId) {
    int count = jdbcClient.sql("""
            INSERT INTO users_lessons (user_id, lesson_id, is_completed)
            VALUES (:userId, :lessonId, :isCompleted)
            ON CONFLICT (user_id, lesson_id) DO
            UPDATE SET is_completed = FALSE
            WHERE users_lessons.is_completed = TRUE;
            """)
        .param("userId", userId)
        .param("lessonId", lessonId)
        .param("isCompleted", false)
        .update();

    return count >= 1;
  }
}
