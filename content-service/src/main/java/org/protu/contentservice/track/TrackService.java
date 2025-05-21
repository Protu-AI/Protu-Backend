package org.protu.contentservice.track;

import org.protu.contentservice.common.exception.custom.EntityNotFoundException;
import org.protu.contentservice.course.CourseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrackService {

  private final TrackRepository tracks;

  public TrackService(TrackRepository tracks) {
    this.tracks = tracks;
  }

  @Transactional
  public void createTrackIfNotExists(TrackRequest trackRequest) {
    tracks.add(trackRequest);
  }

  @Transactional(readOnly = true)
  public List<TrackWithCourses> getAllTracks() {
    return tracks.findAll().orElse(null);
  }

  @Transactional(readOnly = true)
  public TrackWithCourses getTrackByName(String trackName) {
    return tracks.findByName(trackName)
        .orElseThrow(() -> new EntityNotFoundException("Track", trackName));
  }

  @Transactional
  public void updateTrack(String trackName, TrackRequest trackRequest) {
    tracks.update(trackName, trackRequest);
  }

  @Transactional
  public void deleteTrack(String trackName) {
    tracks.delete(trackName);
  }

  @Transactional(readOnly = true)
  public List<CourseDto> getAllCoursesForTrack(String trackName) {
    return tracks.findCoursesByTrackName(trackName).orElse(null);
  }

  @Transactional
  public void addExistingCourseToTrack(String trackName, String courseName) {
    tracks.addCourseToTrack(trackName, courseName);
  }

  @Transactional
  public void deleteCourseFromTrack(String trackName, String courseName) {
    tracks.deleteCourseFromTrack(trackName, courseName);
  }
}
