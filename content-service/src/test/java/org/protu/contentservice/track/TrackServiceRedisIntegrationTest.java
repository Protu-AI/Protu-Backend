package org.protu.contentservice.track;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.protu.contentservice.RedisCacheHelper;
import org.protu.contentservice.config.RedisContainerConfig;
import org.protu.contentservice.course.CourseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
@Import(RedisContainerConfig.class)
public class TrackServiceRedisIntegrationTest {

  private static final String CACHE_ALL_TRACK_LIST = "all-tracks-list";
  private static final String CACHE_TRACK_DETAILS = "track-details";
  private static final String CACHE_TRACK_COURSES = "track-courses";
  private static final String ALL_TRACKS_KEY = "all-tracks";
  private static final String TRACK1_KEY = "track1";
  private static final String COURSE1_KEY = "course1";

  @MockitoBean
  private TrackRepository tracks;

  @MockitoBean
  private JwtDecoder jwtDecoder;

  @Autowired
  private TrackService trackService;

  @Autowired
  private RedisCacheHelper redisCacheHelper;

  @BeforeEach
  void setUp() {
    redisCacheHelper.clearAllCaches();
    reset(tracks);
  }

  private TrackWithCourses createTestTrack(int id, String name) {
    return new TrackWithCourses(id, name, "any", null);
  }

  private List<TrackWithCourses> createTestTrackList() {
    return List.of(
        createTestTrack(1, TRACK1_KEY),
        createTestTrack(2, "track2")
    );
  }

  private List<CourseDto> createTestCourseList() {
    return List.of(new CourseDto(1, "any", "any", "any"));
  }

  private TrackRequest createTestTrackRequest() {
    return new TrackRequest("any", "any");
  }

  private void cacheATrackInTrackDetailsCache() {
    var track = createTestTrack(1, TRACK1_KEY);
    when(tracks.findByName(TRACK1_KEY)).thenReturn(Optional.of(track));

    var cachedValue = redisCacheHelper.getCachedValue(CACHE_TRACK_DETAILS, TRACK1_KEY);
    assertThat(cachedValue).isNull(); // not yet cached 👌🏻

    var result1 = trackService.getTrackByName(TRACK1_KEY);

    cachedValue = redisCacheHelper.getCachedValue(CACHE_TRACK_DETAILS, TRACK1_KEY);
    assertThat(cachedValue).isEqualTo(track); // cached now 🙌🏻

    var result2 = trackService.getTrackByName(TRACK1_KEY);

    verify(tracks, times(1)).findByName(TRACK1_KEY);
    assertThat(result1).isEqualTo(result2);
  }

  private void cacheSomeTracksInAllTracksCache() {
    var trackList = createTestTrackList();
    when(tracks.findAll()).thenReturn(Optional.of(trackList));

    var cachedValue = redisCacheHelper.getCachedValue(CACHE_ALL_TRACK_LIST, ALL_TRACKS_KEY);
    assertThat(cachedValue).isNull();

    var result1 = trackService.getAllTracks();

    cachedValue = redisCacheHelper.getCachedValue(CACHE_ALL_TRACK_LIST, ALL_TRACKS_KEY);
    assertThat(cachedValue).isEqualTo(trackList);

    var result2 = trackService.getAllTracks();

    verify(tracks, times(1)).findAll();
    assertThat(result1).isEqualTo(result2);
  }

  private void cacheSomeCoursesInTrackCoursesCache() {
    var courseList = createTestCourseList();
    when(tracks.findCoursesByTrackName(TRACK1_KEY))
        .thenReturn(Optional.of(courseList));

    var cachedValue = redisCacheHelper.getCachedValue(CACHE_TRACK_COURSES, TRACK1_KEY);
    assertThat(cachedValue).isNull(); // not yet cached 👌🏻

    var result1 = trackService.getAllCoursesForTrack(TRACK1_KEY);

    cachedValue = redisCacheHelper.getCachedValue(CACHE_TRACK_COURSES, TRACK1_KEY);
    assertThat(cachedValue).isEqualTo(courseList); // cached now 🙌🏻

    var result2 = trackService.getAllCoursesForTrack(TRACK1_KEY);

    verify(tracks, times(1)).findCoursesByTrackName(TRACK1_KEY);
    assertThat(result1).isEqualTo(result2);
  }

  private void verifyCacheState(String cacheType, String key, boolean shouldBeNull) {
    var cachedValue = redisCacheHelper.getCachedValue(cacheType, key);
    if (shouldBeNull) {
      assertThat(cachedValue).isNull();
    } else {
      assertThat(cachedValue).isNotNull();
    }
  }

  private void verifyAllCachesState(boolean shouldBeNull) {
    verifyCacheState(CACHE_TRACK_DETAILS, TRACK1_KEY, shouldBeNull);
    verifyCacheState(CACHE_ALL_TRACK_LIST, ALL_TRACKS_KEY, shouldBeNull);
    verifyCacheState(CACHE_TRACK_COURSES, TRACK1_KEY, shouldBeNull);
  }

  private void verifyRepositoryCallCounts(
      int expectedFindByNameCalls,
      int expectedFindAllCalls,
      int expectedFindCoursesCalls) {

    verify(tracks, times(expectedFindByNameCalls)).findByName(TRACK1_KEY);
    verify(tracks, times(expectedFindAllCalls)).findAll();
    verify(tracks, times(expectedFindCoursesCalls)).findCoursesByTrackName(TRACK1_KEY);
  }

  private void triggerServiceCallsAndVerifyRepositoryCalls(
      int expectedFindByNameCalls,
      int expectedFindAllCalls,
      int expectedFindCoursesCalls) {

    trackService.getTrackByName(TRACK1_KEY);
    trackService.getAllTracks();
    trackService.getAllCoursesForTrack(TRACK1_KEY);

    verifyRepositoryCallCounts(expectedFindByNameCalls, expectedFindAllCalls, expectedFindCoursesCalls);
  }

  private void verifyThatCachesEvictionSucceeded() {
    verifyAllCachesState(true);
    triggerServiceCallsAndVerifyRepositoryCalls(2, 2, 2);
  }

  private void verifyThatCachesEvictionFailed() {
    verifyAllCachesState(false);
    triggerServiceCallsAndVerifyRepositoryCalls(1, 1, 1);
  }

  private void setupAllCaches() {
    cacheATrackInTrackDetailsCache();
    cacheSomeTracksInAllTracksCache();
    cacheSomeCoursesInTrackCoursesCache();
  }

  @Test
  void getTrackByName_shouldBeCached_afterFirstCall() {
    cacheATrackInTrackDetailsCache();
  }

  @Test
  void getAllTracks_shouldBeCached_afterFirstCall() {
    cacheSomeTracksInAllTracksCache();
  }

  @Test
  void getAllCoursesForTrack_shouldBeCached_afterFirstCall() {
    cacheSomeCoursesInTrackCoursesCache();
  }

  @Test
  void createTrack_shouldEvictOnlyAllTracksCache() {
    setupAllCaches();

    var track = createTestTrackRequest();
    trackService.createTrackIfNotExists(track);

    verifyCacheState(CACHE_TRACK_DETAILS, TRACK1_KEY, false);
    verifyCacheState(CACHE_ALL_TRACK_LIST, ALL_TRACKS_KEY, true);
    verifyCacheState(CACHE_TRACK_COURSES, TRACK1_KEY, false);

    triggerServiceCallsAndVerifyRepositoryCalls(1, 2, 1);
  }

  @Test
  void createTrack_shouldOnlyEvictAllTracksCache_whenExceptionIsThrown() {
    cacheSomeTracksInAllTracksCache();

    doThrow(new RuntimeException())
        .when(tracks).findAll();

    try {
      trackService.getAllTracks();
    } catch (Exception ignored) {
    }

    verifyCacheState(CACHE_ALL_TRACK_LIST, ALL_TRACKS_KEY, false);
    verify(tracks, times(1)).findAll();
  }

  @Test
  void updateTrack_shouldEvictAllTrackCaches_whenTrackExists() {
    setupAllCaches();

    trackService.updateTrack(TRACK1_KEY, createTestTrackRequest());

    verifyThatCachesEvictionSucceeded();
  }

  @Test
  void updateTrack_shouldNOTEvictAllTrackCaches_whenExceptionIsThrown() {
    setupAllCaches();

    doThrow(new RuntimeException())
        .when(tracks).update(TRACK1_KEY, createTestTrackRequest());

    try {
      trackService.updateTrack(TRACK1_KEY, createTestTrackRequest());
    } catch (Exception ignored) {
    }

    verifyThatCachesEvictionFailed();
  }

  @Test
  void deleteTrack_shouldEvictAllTrackCaches_whenTrackExists() {
    setupAllCaches();

    trackService.deleteTrack(TRACK1_KEY);

    verifyThatCachesEvictionSucceeded();
  }

  @Test
  void deleteTrack_shouldNOTEvictAllTrackCaches_whenExceptionIsThrown() {
    setupAllCaches();

    doThrow(new RuntimeException())
        .when(tracks).delete(TRACK1_KEY);

    try {
      trackService.deleteTrack(TRACK1_KEY);
    } catch (Exception ignored) {
    }

    verifyThatCachesEvictionFailed();
  }

  @Test
  void addCourseToTrack_shouldEvictAllTrackCaches_whenTrackExists() {
    setupAllCaches();

    trackService.addExistingCourseToTrack(TRACK1_KEY, COURSE1_KEY);

    verifyThatCachesEvictionSucceeded();
  }

  @Test
  void addCourseToTrack_shouldNOTEvictAllTrackCaches_whenExceptionIsThrown() {
    setupAllCaches();

    doThrow(new RuntimeException())
        .when(tracks).addCourseToTrack(TRACK1_KEY, COURSE1_KEY);

    try {
      trackService.addExistingCourseToTrack(TRACK1_KEY, COURSE1_KEY);
    } catch (Exception ignored) {
    }

    verifyThatCachesEvictionFailed();
  }

  @Test
  void deleteCourseFromTrack_shouldEvictAllTrackCaches_whenTrackExists() {
    setupAllCaches();

    trackService.deleteCourseFromTrack(TRACK1_KEY, COURSE1_KEY);

    verifyThatCachesEvictionSucceeded();
  }

  @Test
  void deleteCourseFromTrack_shouldNOTEvictAllTrackCaches_whenExceptionIsThrown() {
    setupAllCaches();

    doThrow(new RuntimeException())
        .when(tracks).deleteCourseFromTrack(TRACK1_KEY, COURSE1_KEY);

    try {
      trackService.deleteCourseFromTrack(TRACK1_KEY, COURSE1_KEY);
    } catch (Exception ignored) {
    }

    verifyThatCachesEvictionFailed();
  }
}