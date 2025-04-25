package org.protu.contentservice.lesson;

import org.protu.contentservice.lesson.dto.LessonSummary;
import org.protu.contentservice.lesson.dto.LessonsWithCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Integer> {

  Optional<Lesson> findLessonByName(String name);

  @NativeQuery("SELECT COUNT(id) FROM lessons WHERE course_id = :courseId")
  Integer findNumberOfLessonsForCourseWithId(Integer courseId);

  @Query("SELECT new org.protu.contentservice.lesson.dto.LessonsWithCompletion" +
      "(l.id, l.name, l.lessonOrder, ul.isCompleted) " +
      "FROM Lesson l " +
      "LEFT JOIN UsersLessons ul " +
      "ON l.id = ul.lesson.id AND ul.user.id = :userId " +
      "WHERE l.course.id = :courseId " +
      "ORDER BY l.lessonOrder")
  List<LessonsWithCompletion> findLessonsWithCompletionStatus(@Param("userId") Long userId, @Param("courseId") Integer courseId);


  @Query("SELECT new org.protu.contentservice.lesson.dto.LessonSummary(l.id, l.name, l.lessonOrder, l.createdAt, l.updatedAt) " +
      "FROM Lesson l " +
      "WHERE l.course.id = :courseId " +
      "ORDER BY l.lessonOrder")
  List<LessonSummary> findAllLessonsInCourse(@Param("courseId") Integer courseId);
}
