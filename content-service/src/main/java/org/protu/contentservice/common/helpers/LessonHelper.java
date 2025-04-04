package org.protu.contentservice.common.helpers;

import lombok.RequiredArgsConstructor;
import org.protu.contentservice.lesson.Lesson;
import org.protu.contentservice.lesson.LessonRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LessonHelper {

  private final LessonRepository lessonRepository;

  public Lesson fetchLessonByIdOrThrow(Integer lessonId) {
    return lessonRepository.findById(lessonId).orElseThrow(() -> new RuntimeException("Lesson not found"));
  }
}