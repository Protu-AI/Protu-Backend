package org.protu.contentservice.track;

import org.protu.contentservice.common.exception.custom.EntityNotFoundException;
import org.protu.contentservice.course.CourseDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrackService {

  private static final String CACHE_ALL_TRACK_LIST = "all-tracks-list";
  private static final String CACHE_TRACK_DETAILS = "track-details";
  private static final String CACHE_TRACK_COURSES = "track-courses";
  private final TrackRepository tracks;

  public TrackService(TrackRepository tracks) {
    this.tracks = tracks;
  }

  @Transactional
  @CacheEvict(value = CACHE_ALL_TRACK_LIST, allEntries = true)
  public void createTrackIfNotExists(TrackRequest trackRequest) {
    tracks.add(trackRequest);
  }

  @Transactional(readOnly = true)
  @Cacheable(cacheNames = CACHE_ALL_TRACK_LIST, key = "'all-tracks'")
  public List<TrackWithCourses> getAllTracks() {
    return tracks.findAll().orElse(null);
  }

  @Transactional(readOnly = true)
  @Cacheable(value = CACHE_TRACK_DETAILS, key = "#trackName", unless = "#result == null")
  public TrackWithCourses getTrackByName(String trackName) {
    return tracks.findByName(trackName)
        .orElseThrow(() -> new EntityNotFoundException("Track", trackName));
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = CACHE_ALL_TRACK_LIST, allEntries = true),
      @CacheEvict(value = CACHE_TRACK_DETAILS, key = "#trackName"),
      @CacheEvict(value = CACHE_TRACK_COURSES, key = "#trackName")
  })
  public void updateTrack(String trackName, TrackRequest trackRequest) {
    tracks.update(trackName, trackRequest);
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = CACHE_ALL_TRACK_LIST, allEntries = true),
      @CacheEvict(value = CACHE_TRACK_DETAILS, key = "#trackName"),
      @CacheEvict(value = CACHE_TRACK_COURSES, key = "#trackName")
  })
  public void deleteTrack(String trackName) {
    tracks.delete(trackName);
  }

  @Transactional(readOnly = true)
  @Cacheable(value = CACHE_TRACK_COURSES, key = "#trackName", unless = "#result == null || #result.isEmpty()")
  public List<CourseDto> getAllCoursesForTrack(String trackName) {
    return tracks.findCoursesByTrackName(trackName).orElse(null);
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = CACHE_ALL_TRACK_LIST, allEntries = true),
      @CacheEvict(value = CACHE_TRACK_DETAILS, key = "#trackName"),
      @CacheEvict(value = CACHE_TRACK_COURSES, key = "#trackName")
  })
  public void addExistingCourseToTrack(String trackName, String courseName) {
    tracks.addCourseToTrack(trackName, courseName);
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = CACHE_ALL_TRACK_LIST, allEntries = true),
      @CacheEvict(value = CACHE_TRACK_DETAILS, key = "#trackName"),
      @CacheEvict(value = CACHE_TRACK_COURSES, key = "#trackName")
  })
  public void deleteCourseFromTrack(String trackName, String courseName) {
    tracks.deleteCourseFromTrack(trackName, courseName);
  }
}
