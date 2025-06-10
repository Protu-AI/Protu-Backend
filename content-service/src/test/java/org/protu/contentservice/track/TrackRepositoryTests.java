package org.protu.contentservice.track;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.protu.contentservice.config.PostgresContainerConfig;
import org.protu.contentservice.course.CourseRepository;
import org.protu.contentservice.course.CourseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@JdbcTest
@Import({CourseRepository.class, TrackRepository.class, PostgresContainerConfig.class})
public class TrackRepositoryTests {

  @Autowired
  TrackRepository tracks;

  @Autowired
  CourseRepository courses;

  @Autowired
  JdbcClient jdbcClient;

  @BeforeEach
  void setUp() {
    jdbcClient.sql("DELETE FROM tracks_courses;").update();
    jdbcClient.sql("DELETE FROM tracks;").update();
    jdbcClient.sql("DELETE FROM courses;").update();
  }

  @Test
  void addTrack_shouldAddTrack() {
    var newTrack = new TrackRequest("track1", "desc1");

    tracks.add(newTrack);

    assertThat(tracks.findAll().orElse(null))
        .hasSize(1)
        .extracting(TrackWithCourses::name)
        .containsExactly("track1");
  }

  @Test
  void getNonExistingTrack_shouldReturnEmpty() {
    assertThat(tracks.findByName("non-existing-track")).isEmpty();
  }

  @Test
  void getTrackByName() {
    var track = new TrackRequest("track1", "desc1");

    tracks.add(track);

    assertThat(tracks.findByName("track1").orElse(null))
        .extracting(
            TrackWithCourses::name,
            TrackWithCourses::description)
        .containsExactly("track1", "desc1");
  }

  @Test
  void updateTrack_shouldAddTrack_whenTrackDoesNotExist() {
    var nonExistingTrack = new TrackRequest("track999", "desc999");

    assertThat(tracks.findByName(nonExistingTrack.name()).orElse(null)).isNull();

    tracks.update("track999", nonExistingTrack);

    assertThat(tracks.findByName(nonExistingTrack.name()).orElse(null))
        .extracting(
            TrackWithCourses::name,
            TrackWithCourses::description)
        .containsExactly("track999", "desc999");
  }


  @Test
  void updateTrack_shouldUpdateTrack_whenTrackExists() {
    var track = new TrackRequest("track1", "desc1");

    tracks.add(track);

    var updatedTrack = new TrackRequest("updated_track1", "updated_desc1");

    tracks.update("track1", updatedTrack);

    assertThat(tracks.findByName("updated_track1").orElse(null))
        .extracting(
            TrackWithCourses::name,
            TrackWithCourses::description)
        .containsExactly("updated_track1", "updated_desc1");

    assertThat(tracks.findByName("track1").orElse(null))
        .isNull();
  }

  @Test
  void deleteTrack() {
    var track = new TrackRequest("track1", "desc1");

    tracks.add(track);
    assertThat(tracks.findAll().orElse(null))
        .hasSize(1);

    tracks.delete("track1");
    assertThat(tracks.findAll().orElse(null))
        .isEmpty();
  }

  @Test
  void addCourseToTrack() {
    var track = new TrackRequest("track1", "desc1");
    var course = new CourseRequest("course1", "desc1");

    tracks.add(track);
    TrackWithCourses trackWithCourses = tracks.findByName("track1")
        .orElseThrow(RuntimeException::new);

    assertThat(trackWithCourses.courses()).isEmpty();

    courses.add(course);
    tracks.addCourseToTrack("track1", "course1");

    trackWithCourses = tracks.findByName("track1")
        .orElseThrow(RuntimeException::new);

    assertThat(trackWithCourses.courses()).hasSize(1);
    assertThat(trackWithCourses.courses().getFirst().name()).isEqualTo("course1");
  }

  @Test
  void deleteCourseFromTrack() {
    var track = new TrackRequest("track1", "desc1");
    var course = new CourseRequest("course1", "desc1");

    tracks.add(track);
    courses.add(course);
    tracks.addCourseToTrack("track1", "course1");

    var trackWithCourses = tracks.findByName("track1")
        .orElseThrow(RuntimeException::new);

    assertThat(trackWithCourses.courses()).hasSize(1);
    assertThat(trackWithCourses.courses().getFirst().name()).isEqualTo("course1");

    tracks.deleteCourseFromTrack("track1", "course1");
    trackWithCourses = tracks.findByName("track1")
        .orElseThrow(RuntimeException::new);

    assertThat(trackWithCourses.courses()).isEmpty();
  }

  @Test
  void findCoursesByTrackName() {
    var track = new TrackRequest("track1", "desc1");
    var course1 = new CourseRequest("course1", "desc1");
    var course2 = new CourseRequest("course2", "desc2");


    tracks.add(track);
    courses.add(course1);
    courses.add(course2);

    var trackWithCourses = tracks.findByName("track1")
        .orElseThrow(RuntimeException::new);

    assertThat(trackWithCourses.courses()).isEmpty();

    tracks.addCourseToTrack("track1", "course1");
    tracks.addCourseToTrack("track1", "course2");

    trackWithCourses = tracks.findByName("track1")
        .orElseThrow(RuntimeException::new);

    assertThat(trackWithCourses.courses())
        .hasSize(2)
        .extracting("name")
        .containsExactlyInAnyOrder("course1", "course2");
  }
}
