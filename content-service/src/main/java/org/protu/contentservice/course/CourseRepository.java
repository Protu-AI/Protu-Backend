package org.protu.contentservice.course;

import org.protu.contentservice.common.exception.custom.EntityNotFoundException;
import org.protu.contentservice.lesson.dto.LessonWithoutContent;
import org.protu.contentservice.lesson.dto.LessonsWithCompletion;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.*;

@Repository
public class CourseRepository {

  private static final RowMapper<LessonWithoutContent> LESSON_MAPPER = (rs, rowNum) -> new LessonWithoutContent(
      rs.getInt("lesson_id"),
      rs.getString("lesson_name"),
      rs.getInt("lesson_order")
  );

  private final JdbcClient jdbcClient;

  public CourseRepository(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public void add(CourseRequest courseRequest) {
    jdbcClient.sql("""
            INSERT INTO courses (name, description)
            VALUES (:name, :description)
            ON CONFLICT (name)
            DO NOTHING
            """)
        .param("name", courseRequest.name())
        .param("description", courseRequest.description())
        .update();
  }

  public Optional<CourseWithLessons> findByName(String courseName) {
    String sql = """
            SELECT
              c.id AS course_id,
              c.name AS course_name,
              c.description AS course_desc,
              c.pic_url AS course_pic,
              l.id AS lesson_id,
              l.name AS lesson_name,
              l.lesson_order AS lesson_order
            FROM courses c
            LEFT JOIN courses_lessons cl ON cl.course_id = c.id
            LEFT JOIN lessons l ON l.id = cl.lesson_id
            WHERE c.name = :name
            ORDER BY l.lesson_order
        """;

    List<CourseWithLessons> results = jdbcClient.sql(sql)
        .param("name", courseName)
        .query((ResultSet rs) -> {
          Map<Integer, CourseWithLessons> map = new LinkedHashMap<>();

          while (rs.next()) {
            int courseId = rs.getInt("course_id");
            String name = rs.getString("course_name");
            String description = rs.getString("course_desc");
            String picUrl = rs.getString("course_pic");

            map.computeIfAbsent(courseId, id -> new CourseWithLessons(id, name, description, picUrl, new ArrayList<>()));

            Integer lessonId = rs.getObject("lesson_id", Integer.class);
            if (lessonId != null) {
              LessonWithoutContent lesson = LESSON_MAPPER.mapRow(rs, 0);
              map.get(courseId).lessons().add(lesson);
            }
          }

          return new ArrayList<>(map.values());
        });

    if (results.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(results.get(0));
  }

  public CourseDto findByNameOrThrow(String courseName) {
    return jdbcClient.sql("""
            SELECT
              c.id,
              c.name,
              c.description,
              c.pic_url
            FROM courses c
            WHERE c.name = :name
            """)
        .param("name", courseName)
        .query(CourseDto.class)
        .optional()
        .orElseThrow(() -> new EntityNotFoundException("Course", courseName));
  }

  public void update(String courseName, CourseRequest courseRequest) {
    jdbcClient.sql("""
            UPDATE courses
            SET name = :newName, description = :description
            WHERE name = :name
            """)
        .param("newName", courseRequest.name())
        .param("description", courseRequest.description())
        .param("name", courseName)
        .update();
  }

  public List<CourseWithLessons> findAll() {
    String sql = """
            SELECT
              c.id AS course_id,
              c.name AS course_name,
              c.description AS course_desc,
              c.pic_url AS course_pic,
              l.id AS lesson_id,
              l.name AS lesson_name,
              l.lesson_order AS lesson_order
            FROM courses c
            LEFT JOIN courses_lessons cl ON cl.course_id = c.id
            LEFT JOIN lessons l ON l.id = cl.lesson_id
            ORDER BY c.id, l.lesson_order
        """;

    return jdbcClient.sql(sql)
        .query((ResultSet rs) -> {
          Map<Integer, CourseWithLessons> map = new LinkedHashMap<>();

          while (rs.next()) {
            int courseId = rs.getInt("course_id");
            String name = rs.getString("course_name");
            String description = rs.getString("course_desc");
            String picUrl = rs.getString("course_pic");

            map.computeIfAbsent(courseId, id -> new CourseWithLessons(id, name, description, picUrl, new ArrayList<>()));

            Integer lessonId = rs.getObject("lesson_id", Integer.class);
            if (lessonId != null) {
              LessonWithoutContent lesson = LESSON_MAPPER.mapRow(rs, 0);
              map.get(courseId).lessons().add(lesson);
            }
          }

          return new ArrayList<>(map.values());
        });
  }


  public void updateCoursePicture(String courseName, String secureAssetUrl) {
    jdbcClient.sql("UPDATE courses SET pic_url = :url WHERE name = :name")
        .param("url", secureAssetUrl)
        .param("name", courseName)
        .update();
  }

  public List<LessonWithoutContent> findLessonsByCourseId(int courseId) {
    return jdbcClient.sql("""
            SELECT
                l.id,
                l.name,
                l.lesson_order AS lessonOrder
            FROM lessons AS l
            JOIN courses_lessons AS cl ON cl.lesson_id = l.id
            WHERE cl.course_id = :courseId
            ORDER BY l.lesson_order
            """)
        .param("courseId", courseId)
        .query(LessonWithoutContent.class)
        .list();
  }

  public void addLessonToCourse(int courseId, int lessonId) {
    jdbcClient.sql("""
            INSERT INTO courses_lessons (course_id, lesson_id)
            VALUES (:courseId, :lessonId)
            ON CONFLICT (course_id, lesson_id)
            DO NOTHING
            """)
        .param("courseId", courseId)
        .param("lessonId", lessonId)
        .update();
  }

  public void deleteLessonFromCourse(int courseId, int lessonId) {
    jdbcClient.sql("DELETE FROM courses_lessons WHERE course_id = :courseId AND lesson_id = :lessonId")
        .param("courseId", courseId)
        .param("lessonId", lessonId)
        .update();
  }

  public void delete(String courseName) {
    jdbcClient.sql("DELETE FROM courses WHERE name = :name")
        .param("name", courseName)
        .update();
  }

  public List<LessonsWithCompletion> findLessonsWithCompletionStatus(Long userId, int courseId) {
    return jdbcClient.sql("""
            SELECT
                l.id,
                l.name,
                l.lesson_order AS lessonOrder,
                ul.is_completed AS isCompleted
            FROM lessons AS l
            JOIN courses_lessons AS cl ON cl.lesson_id = l.id
            LEFT JOIN users_lessons AS ul ON ul.lesson_id = l.id AND ul.user_id = :userId
            WHERE cl.course_id = :courseId
            ORDER BY l.lesson_order
            """)
        .param("userId", userId)
        .param("courseId", courseId)
        .query(LessonsWithCompletion.class)
        .list();
  }
}