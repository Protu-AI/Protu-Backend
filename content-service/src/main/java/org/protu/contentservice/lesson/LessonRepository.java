package org.protu.contentservice.lesson;

import org.protu.contentservice.lesson.dto.LessonRequest;
import org.protu.contentservice.lesson.dto.LessonUpdateRequest;
import org.protu.contentservice.lesson.dto.LessonWithContent;
import org.protu.contentservice.lesson.dto.LessonWithoutContent;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class LessonRepository {

  private final JdbcClient jdbcClient;

  public LessonRepository(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public void add(LessonRequest lessonRequest) {
    jdbcClient.sql("""
            INSERT INTO lessons (name, content, lesson_order)
            VALUES (:name, :content, :lessonOrder)
            ON CONFLICT (name)
            DO NOTHING
            """)
        .param("name", lessonRequest.name())
        .param("content", lessonRequest.content())
        .param("lessonOrder", lessonRequest.lessonOrder())
        .update();
  }

  public Optional<LessonWithContent> findByName(String lessonName) {
    return jdbcClient.sql("""
            SELECT id, name, content, lesson_order AS lessonOrder
            FROM lessons
            WHERE name = :name
            """)
        .param("name", lessonName)
        .query(LessonWithContent.class)
        .optional();
  }

  public Optional<LessonWithoutContent> findByNameWithoutContent(String lessonName) {
    return jdbcClient.sql("""
            SELECT id, name, lesson_order AS lessonOrder
            FROM lessons
            WHERE name = :name
            """)
        .param("name", lessonName)
        .query(LessonWithoutContent.class)
        .optional();
  }

  public void update(String lessonName, LessonUpdateRequest lessonRequest) {
    jdbcClient.sql("""
            UPDATE lessons
            SET name = :newName, content = :content, lesson_order = :lessonOrder
            WHERE name = :name
            """)
        .param("newName", lessonRequest.name())
        .param("content", lessonRequest.content())
        .param("lessonOrder", lessonRequest.lessonOrder())
        .param("name", lessonName)
        .update();
  }

  public void delete(String lessonName) {
    jdbcClient.sql("DELETE FROM lessons WHERE name = :lessonName")
        .param("lessonName", lessonName)
        .update();
  }
}
