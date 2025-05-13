package org.protu.contentservice.lesson;

import org.protu.contentservice.lesson.dto.LessonRequest;
import org.protu.contentservice.lesson.dto.LessonUpdateRequest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LessonService {

  private final JdbcClient jdbcClient;

  public LessonService(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public void createLesson(LessonRequest lessonRequest) {
    jdbcClient.sql("INSERT INTO lessons (name, content, lesson_order) VALUES (:name, :content, :lessonOrder) ON CONFLICT (name) DO NOTHING")
        .param("name", lessonRequest.name())
        .param("content", lessonRequest.content())
        .param("lessonOrder", lessonRequest.lessonOrder())
        .update();
  }

  public Lesson getLessonByName(String lessonName) {
    Optional<Lesson> lessonOpt = jdbcClient.sql("SELECT id, name, content, lesson_order AS lessonOrder FROM lessons WHERE name = :name")
        .param("name", lessonName)
        .query(Lesson.class)
        .optional();

    return lessonOpt.orElseThrow(() -> new RuntimeException("Lesson does not exist"));
  }

  public void updateLesson(String lessonName, LessonUpdateRequest lessonRequest) {
    jdbcClient.sql("UPDATE lessons SET name = :newName, content = :content, lesson_order = :lessonOrder WHERE name = :name ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name, content = EXCLUDED.content, lesson_order = EXCLUDED.lesson_order;")
        .param("newName", lessonRequest.name())
        .param("content", lessonRequest.content())
        .param("lessonOrder", lessonRequest.lessonOrder())
        .param("name", lessonName)
        .update();
  }

  public void deleteLesson(String lessonName) {
    jdbcClient.sql("DELETE FROM lessons WHERE name = :lessonName")
        .param("lessonName", lessonName)
        .update();
  }
}
