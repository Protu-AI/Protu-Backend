package org.protu.contentservice.course;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.protu.contentservice.common.exception.custom.EntityNotFoundException;
import org.protu.contentservice.common.properties.AppProperties;
import org.protu.contentservice.lesson.Lesson;
import org.protu.contentservice.lesson.LessonService;
import org.protu.contentservice.lesson.dto.LessonWithoutContent;
import org.protu.contentservice.lesson.dto.LessonsWithCompletion;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CourseService {

  private final AppProperties props;
  private final JdbcClient jdbcClient;
  private final LessonService lessonService;

  public CourseService(AppProperties props, JdbcClient jdbcClient, LessonService lessonService) {
    this.props = props;
    this.jdbcClient = jdbcClient;
    this.lessonService = lessonService;
  }

  public void createCourse(CourseRequest courseRequest) {
    jdbcClient.sql("INSERT INTO courses (name, description) VALUES (:name, :description) ON CONFLICT (name) DO NOTHING")
        .param("name", courseRequest.name())
        .param("description", courseRequest.description())
        .update();
  }

  public boolean isCourseExists(String courseName) {
    return jdbcClient.sql("SELECT COUNT(*) FROM courses WHERE name = :name")
        .param("name", courseName)
        .query(Integer.class)
        .single() > 0;
  }

  public CourseWithLessons getCourseByNameOrThrow(String courseName) {
    String sql = """
            SELECT c.id AS course_id, c.name AS course_name, c.description AS course_description, c.pic_url AS course_pic_url,
                l.id AS lesson_id, l.name AS lesson_name, l.lesson_order AS lesson_order
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
            String description = rs.getString("course_description");
            String picUrl = rs.getString("course_pic_url");

            map.computeIfAbsent(courseId, id ->
                new CourseWithLessons(id, name, description, picUrl, new ArrayList<>())
            );

            Integer lessonId = rs.getObject("lesson_id", Integer.class);
            if (lessonId != null) {
              LessonWithoutContent lesson = new LessonWithoutContent(
                  lessonId,
                  rs.getString("lesson_name"),
                  rs.getInt("lesson_order")
              );
              map.get(courseId).lessons().add(lesson);
            }
          }

          return new ArrayList<>(map.values());
        });

    if (results.isEmpty()) {
      throw new EntityNotFoundException("Course", courseName);
    }

    return results.get(0);
  }


  public void updateCourse(String courseName, CourseRequest courseRequest) {
    jdbcClient.sql("UPDATE courses SET name = :newName, description = :description WHERE name = :name ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description;")
        .param("newName", courseRequest.name())
        .param("description", courseRequest.description())
        .param("name", courseName)
        .update();
  }

  public List<CourseWithLessons> getAllCourses() {
    String sql = """
            SELECT c.id AS course_id, c.name AS course_name, c.description AS course_description, c.pic_url AS course_pic_url,
                l.id AS lesson_id, l.name AS lesson_name, l.lesson_order AS lesson_order
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
            String description = rs.getString("course_description");
            String picUrl = rs.getString("course_pic_url");

            map.computeIfAbsent(courseId, id ->
                new CourseWithLessons(id, name, description, picUrl, new ArrayList<>())
            );

            Integer lessonId = rs.getObject("lesson_id", Integer.class);
            if (lessonId != null) {
              LessonWithoutContent lesson = new LessonWithoutContent(
                  lessonId,
                  rs.getString("lesson_name"),
                  rs.getInt("lesson_order")
              );
              map.get(courseId).lessons().add(lesson);
            }
          }

          return new ArrayList<>(map.values());
        });
  }


  private String uploadToCloudinary(MultipartFile file) {
    try {
      File file1 = File.createTempFile("coursePic-", file.getOriginalFilename());
      file.transferTo(file1);
      Map uploadMap = ObjectUtils.asMap("asset_folder", "courses-pic");
      Map uploadResults = new Cloudinary(ObjectUtils.asMap(
          "cloud_name", props.cloudinary().cloudName(),
          "api_key", props.cloudinary().apiKey(),
          "api_secret", props.cloudinary().apiSecret()
      )).uploader().upload(file1, uploadMap);

      return uploadResults.get("secure_url").toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void uploadCoursePic(String courseName, MultipartFile file) {
    getCourseByNameOrThrow(courseName);

    String secureAssetUrl = uploadToCloudinary(file);
    jdbcClient.sql("UPDATE courses SET pic_url = :url WHERE name = :name")
        .param("url", secureAssetUrl)
        .param("name", courseName)
        .update();
  }

  public List<LessonWithoutContent> getAllLessonsForCourse(String courseName) {
    CourseWithLessons course = getCourseByNameOrThrow(courseName);

    return jdbcClient.sql("SELECT l.id, l.name, l.lesson_order AS lessonOrder FROM lessons AS l JOIN courses_lessons AS cl ON cl.lesson_id = l.id WHERE cl.course_id = :courseId")
        .param("courseId", course.id())
        .query(LessonWithoutContent.class)
        .list();
  }

  public void addExistingLessonToCourse(String courseName, String lessonName) {
    CourseWithLessons course = getCourseByNameOrThrow(courseName);
    Lesson lesson = lessonService.getLessonByName(lessonName);

    jdbcClient.sql("INSERT INTO courses_lessons (course_id, lesson_id) VALUES (:courseId, :lessonId) ON CONFLICT (course_id, lesson_id) DO NOTHING")
        .param("courseId", course.id())
        .param("lessonId", lesson.id())
        .update();
  }

  public void deleteLessonFromCourse(String courseName, String lessonName) {
    CourseWithLessons course = getCourseByNameOrThrow(courseName);
    Lesson lesson = lessonService.getLessonByName(lessonName);

    jdbcClient.sql("DELETE FROM courses_lessons WHERE course_id = :courseId AND lesson_id = :lessonId")
        .param("courseId", course.id())
        .param("lessonId", lesson.id())
        .update();
  }

  public void deleteCourse(String courseName) {
    jdbcClient.sql("DELETE FROM courses WHERE name = :name")
        .param("name", courseName)
        .update();
  }

  public List<LessonsWithCompletion> getAllLessonsWithCompletionStatusForCourse(Long userId, String courseName) {
    CourseWithLessons course = getCourseByNameOrThrow(courseName);

    return jdbcClient.sql("SELECT l.id, l.name, l.lesson_order AS lessonOrder, ul.is_completed AS isCompleted FROM lessons AS l JOIN courses_lessons AS cl ON cl.lesson_id = l.id LEFT JOIN users_lessons AS ul ON ul.lesson_id = l.id AND ul.user_id = :userId WHERE cl.course_id = :courseId")
        .param("userId", userId)
        .param("courseId", course.id())
        .query(LessonsWithCompletion.class)
        .list();
  }
}
