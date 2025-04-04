package org.protu.contentservice.lesson;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Integer> {

  Optional<Lesson> findLessonByName(String name);

  @NativeQuery("SELECT COUNT(id) FROM lessons WHERE course_id = :courseId")
  Integer findNumberOfLessonsForCourseWithId(Integer courseId);
}
