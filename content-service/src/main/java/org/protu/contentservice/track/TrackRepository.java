package org.protu.contentservice.track;

import org.protu.contentservice.common.exception.custom.EntityNotFoundException;
import org.protu.contentservice.course.CourseDto;
import org.protu.contentservice.course.CourseRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.*;

@Repository
public class TrackRepository {

  private static final RowMapper<TrackWithCourses> TRACK_MAPPER = (rs, rowNum) -> {
    int trackId = rs.getInt("track_id");
    String trackName = rs.getString("track_name");
    String trackDescription = rs.getString("track_desc");
    return new TrackWithCourses(trackId, trackName, trackDescription, new ArrayList<>());
  };

  private static final RowMapper<CourseDto> COURSE_MAPPER = (rs, rowNum) -> {
    int courseId = rs.getInt("course_id");
    String courseName = rs.getString("course_name");
    String courseDescription = rs.getString("course_desc");
    String coursePicUrl = rs.getString("course_pic");
    return new CourseDto(courseId, courseName, courseDescription, coursePicUrl);
  };

  private final JdbcClient jdbcClient;
  private final CourseRepository courses;

  public TrackRepository(JdbcClient jdbcClient, CourseRepository courses) {
    this.jdbcClient = jdbcClient;
    this.courses = courses;
  }

  private Track findByNameOrThrow(String trackName) {
    return jdbcClient
        .sql("""
            SELECT
              t.id,
              t.name,
              t.description
              FROM tracks t
              WHERE t.name = :name
            """)
        .param("name", trackName)
        .query(Track.class)
        .optional()
        .orElseThrow(() -> new EntityNotFoundException("Track", trackName));
  }

  public Optional<List<TrackWithCourses>> findAll() {
    String sql = """
            SELECT
                t.id AS track_id,
                t.name AS track_name,
                t.description AS track_desc,
                c.id AS course_id,
                c.name AS course_name,
                c.description AS course_desc,
                c.pic_url AS course_pic
            FROM tracks t
            LEFT JOIN tracks_courses tc ON tc.track_id = t.id
            LEFT JOIN courses c ON c.id = tc.course_id
            ORDER BY t.id
        """;

    return Optional.of(jdbcClient
        .sql(sql)
        .query(rs -> {
          Map<Integer, TrackWithCourses> map = new LinkedHashMap<>();

          while (rs.next()) {
            int trackId = rs.getInt("track_id");
            if (!map.containsKey(trackId)) {
              map.put(trackId, TRACK_MAPPER.mapRow(rs, rs.getRow()));
            }

            Integer courseId = rs.getObject("course_id", Integer.class);
            if (courseId != null) {
              CourseDto course = COURSE_MAPPER.mapRow(rs, rs.getRow());
              map.get(trackId).courses().add(course);
            }
          }

          return new ArrayList<>(map.values());
        }));
  }

  public Optional<TrackWithCourses> findByName(String trackName) {
    String sql = """
            SELECT
                t.id AS track_id,
                t.name AS track_name,
                t.description AS track_desc,
                c.id AS course_id,
                c.name AS course_name,
                c.description AS course_desc,
                c.pic_url AS course_pic
            FROM tracks t
            LEFT JOIN tracks_courses tc ON tc.track_id = t.id
            LEFT JOIN courses c ON c.id = tc.course_id
            WHERE t.name = :name
        """;

    List<TrackWithCourses> results = jdbcClient
        .sql(sql)
        .param("name", trackName)
        .query((ResultSet rs) -> {
          Map<Integer, TrackWithCourses> map = new LinkedHashMap<>();

          while (rs.next()) {
            int trackId = rs.getInt("track_id");
            if (!map.containsKey(trackId)) {
              map.put(trackId, TRACK_MAPPER.mapRow(rs, rs.getRow()));
            }

            Integer courseId = rs.getObject("course_id", Integer.class);
            if (courseId != null) {
              CourseDto course = COURSE_MAPPER.mapRow(rs, rs.getRow());
              map.get(trackId).courses().add(course);
            }
          }

          return new ArrayList<>(map.values());
        });

    return Optional.ofNullable(results.get(0));
  }

  public void add(TrackRequest trackRequest) {
    jdbcClient.sql("""
            INSERT INTO tracks (name, description)
            VALUES (:name, :description)
            ON CONFLICT (name)
            DO NOTHING
            """)
        .param("name", trackRequest.name())
        .param("description", trackRequest.description())
        .update();
  }

  public void update(String trackName, TrackRequest trackRequest) {
    jdbcClient.sql("""
            INSERT INTO tracks (name, description)
            VALUES (:newName, :description) ON CONFLICT (name) DO
            UPDATE
            SET name  = EXCLUDED.name, description = EXCLUDED.description;
            """)
        .param("newName", trackRequest.name())
        .param("description", trackRequest.description())
        .param("name", trackName)
        .update();
  }

  public void delete(String trackName) {
    jdbcClient.sql("DELETE FROM tracks WHERE name = :name")
        .param("name", trackName)
        .update();
  }

  public Optional<List<CourseDto>> findCoursesByTrackName(String trackName) {
    TrackWithCourses track = findByName(trackName)
        .orElseThrow(() -> new EntityNotFoundException("Track", trackName));

    return Optional.of(jdbcClient
        .sql("""
            SELECT
                c.id,
                c.name,
                c.description,
                c.pic_url AS picUrl
              FROM courses AS c
              JOIN tracks_courses AS tc ON tc.course_id = c.id
              WHERE tc.track_id = :trackId
            """)
        .param("name", trackName)
        .param("trackId", track.id())
        .query(CourseDto.class)
        .list());
  }

  public void addCourseToTrack(String trackName, String courseName) {
    Track track = findByNameOrThrow(trackName);
    CourseDto course = courses.findByNameOrThrow(courseName);

    jdbcClient.sql("""
            INSERT INTO tracks_courses (track_id, course_id)
            VALUES (:trackId, :courseId)
            ON CONFLICT (track_id, course_id)
            DO NOTHING
            """)
        .param("trackId", track.id())
        .param("courseId", course.id())
        .update();
  }

  public void deleteCourseFromTrack(String trackName, String courseName) {
    Track track = findByNameOrThrow(trackName);
    CourseDto course = courses.findByNameOrThrow(courseName);

    jdbcClient.sql("DELETE FROM tracks_courses WHERE track_id = :trackId AND course_id = :courseId")
        .param("trackId", track.id())
        .param("courseId", course.id())
        .update();
  }
}
