package org.protu.contentservice.track;

import org.protu.contentservice.common.exception.custom.EntityNotFoundException;
import org.protu.contentservice.course.Course;
import org.protu.contentservice.course.CourseDto;
import org.protu.contentservice.course.CourseService;
import org.protu.contentservice.course.CourseWithLessons;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TrackService {

  private final JdbcClient jdbcClient;
  private final CourseService courseService;

  public TrackService(JdbcClient jdbcClient, CourseService courseService) {
    this.jdbcClient = jdbcClient;
    this.courseService = courseService;
  }

  private boolean isTrackExists(String trackName) {
    return jdbcClient.sql("SELECT COUNT(*) FROM tracks WHERE name = :name")
        .param("name", trackName)
        .query(Integer.class)
        .single() > 0;
  }

  public void createTrackIfNotExists(TrackRequest trackRequest) {
    jdbcClient.sql("INSERT INTO tracks (name, description) VALUES (:name, :description) ON CONFLICT (name) DO NOTHING")
        .param("name", trackRequest.name())
        .param("description", trackRequest.description())
        .update();
  }

  public List<TrackWithCourses> getAllTracks() {
    String sql = """
            SELECT t.id AS track_id, t.name AS track_name, t.description AS track_desc,
                c.id AS course_id, c.name AS course_name, c.description AS course_desc, c.pic_url AS course_pic
            FROM tracks t
            LEFT JOIN tracks_courses tc ON tc.track_id = t.id
            LEFT JOIN courses c ON c.id = tc.course_id
            ORDER BY t.id
        """;

    return jdbcClient.sql(sql).query(rs -> {
      Map<Integer, TrackWithCourses> tracksMap = new LinkedHashMap<>();

      while (rs.next()) {
        Integer trackId = rs.getInt("track_id");
        String trackName = rs.getString("track_name");
        String trackDesc = rs.getString("track_desc");

        tracksMap.computeIfAbsent(trackId, id ->
            new TrackWithCourses(id, trackName, trackDesc, new ArrayList<>())
        );

        Integer courseId = rs.getObject("course_id", Integer.class);
        if (courseId != null) {
          CourseDto course = new CourseDto(
              courseId,
              rs.getString("course_name"),
              rs.getString("course_desc"),
              rs.getString("course_pic")
          );

          tracksMap.get(trackId).courses().add(course);
        }
      }

      return new ArrayList<>(tracksMap.values());
    });
  }

  public TrackWithCourses getTrackByName(String trackName) {
    String sql = """
            SELECT t.id AS track_id, t.name AS track_name, t.description AS track_description,
                c.id AS course_id, c.name AS course_name, c.description AS course_description, c.pic_url AS course_pic_url
            FROM tracks t
            LEFT JOIN tracks_courses tc ON tc.track_id = t.id
            LEFT JOIN courses c ON c.id = tc.course_id
            WHERE t.name = :name
        """;

    List<TrackWithCourses> results = jdbcClient.sql(sql)
        .param("name", trackName)
        .query((ResultSet rs) -> {
          Map<Integer, TrackWithCourses> map = new LinkedHashMap<>();

          while (rs.next()) {
            int trackId = rs.getInt("track_id");
            String name = rs.getString("track_name");
            String description = rs.getString("track_description");

            map.computeIfAbsent(trackId, id ->
                new TrackWithCourses(id, name, description, new ArrayList<>())
            );

            Integer courseId = rs.getObject("course_id", Integer.class);
            if (courseId != null) {
              CourseDto course = new CourseDto(
                  courseId,
                  rs.getString("course_name"),
                  rs.getString("course_description"),
                  rs.getString("course_pic_url")
              );
              map.get(trackId).courses().add(course);
            }
          }

          return new ArrayList<>(map.values());
        });

    if (results.isEmpty()) {
      throw new EntityNotFoundException("Track", trackName);
    }

    return results.get(0);
  }

  public void updateTrack(String trackName, TrackRequest trackRequest) {
    jdbcClient.sql("INSERT INTO tracks (name, description) VALUES (:newName, :description) WHERE name = :name ON CONFLICT (name) DO UPDATE SET name  = EXCLUDED.name, description = EXCLUDED.description;")
        .param("newName", trackRequest.name())
        .param("description", trackRequest.description())
        .param("name", trackName)
        .update();
  }

  public void deleteTrack(String trackName) {
    jdbcClient.sql("DELETE FROM tracks WHERE name = :name")
        .param("name", trackName)
        .update();
  }

  public List<Course> getAllCoursesForTrack(String trackName) {
    TrackWithCourses track = getTrackByName(trackName);

    return jdbcClient.sql("SELECT c.id, c.name, c.description, c.pic_url AS picUrl FROM courses AS c JOIN tracks_courses AS tc ON tc.course_id = c.id WHERE tc.track_id = :trackId")
        .param("name", trackName)
        .param("trackId", track.id())
        .query(Course.class)
        .list();
  }

  public void addExistingCourseToTrack(String trackName, String courseName) {
    TrackWithCourses track = getTrackByName(trackName);
    CourseWithLessons course = courseService.getCourseByNameOrThrow(courseName);

    jdbcClient.sql("INSERT INTO tracks_courses (track_id, course_id) VALUES (:trackId, :courseId) ON CONFLICT (track_id, course_id) DO NOTHING")
        .param("trackId", track.id())
        .param("courseId", course.id())
        .update();
  }

  public void deleteCourseFromTrack(String trackName, String courseName) {
    TrackWithCourses track = getTrackByName(trackName);
    CourseWithLessons course = courseService.getCourseByNameOrThrow(courseName);

    jdbcClient.sql("DELETE FROM tracks_courses WHERE track_id = :trackId AND course_id = :courseId")
        .param("trackId", track.id())
        .param("courseId", course.id())
        .update();
  }
}
