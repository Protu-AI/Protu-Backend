package org.protu.contentservice.lesson;

import org.protu.contentservice.common.exception.custom.EntityNotFoundException;
import org.protu.contentservice.lesson.dto.LessonRequest;
import org.protu.contentservice.lesson.dto.LessonUpdateRequest;
import org.protu.contentservice.lesson.dto.LessonWithContent;
import org.protu.contentservice.lesson.dto.LessonWithoutContent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LessonService {

  private static final String CACHE_LESSON_WITH_CONTENT = "lesson-with-content";
  private static final String CACHE_LESSON_WITHOUT_CONTENT = "lesson-without-content";
  private final LessonRepository lessons;

  public LessonService(LessonRepository lessons) {
    this.lessons = lessons;
  }

  @Transactional
  public void createLesson(LessonRequest lessonRequest) {
    lessons.add(lessonRequest);
  }

  @Transactional(readOnly = true)
  @Cacheable(value = CACHE_LESSON_WITH_CONTENT, key = "#lessonName", unless = "#result == null")
  public LessonWithContent findByName(String lessonName) {
    return lessons.findByName(lessonName)
        .orElseThrow(() -> new EntityNotFoundException("Lesson", lessonName));
  }

  @Transactional(readOnly = true)
  @Cacheable(value = CACHE_LESSON_WITHOUT_CONTENT, key = "#lessonName", unless = "#result == null")
  public LessonWithoutContent findByNameWithoutContent(String lessonName) {
    return lessons.findByNameWithoutContent(lessonName)
        .orElseThrow(() -> new EntityNotFoundException("Lesson", lessonName));
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = CACHE_LESSON_WITH_CONTENT, key = "#lessonName"),
      @CacheEvict(value = CACHE_LESSON_WITHOUT_CONTENT, key = "#lessonName")
  })
  public void updateLesson(String lessonName, LessonUpdateRequest lessonRequest) {
    lessons.update(lessonName, lessonRequest);
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = CACHE_LESSON_WITH_CONTENT, key = "#lessonName"),
      @CacheEvict(value = CACHE_LESSON_WITHOUT_CONTENT, key = "#lessonName")
  })
  public void deleteLesson(String lessonName) {
    lessons.delete(lessonName);
  }
}
