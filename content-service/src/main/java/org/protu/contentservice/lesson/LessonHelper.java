package org.protu.contentservice.lesson;

import lombok.RequiredArgsConstructor;
import org.protu.contentservice.common.exception.custom.EntityNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LessonHelper {

  private final LessonRepository lessonRepo;

  public Lesson fetchLessonByIdOrThrow(Integer lessonId) {
    return lessonRepo.findById(lessonId).orElseThrow(() -> new EntityNotFoundException("Lesson", lessonId));
  }

  public Lesson fetchLessonByNameOrThrow(String lessonName) {
    return lessonRepo.findLessonByName(lessonName).orElseThrow(() -> new EntityNotFoundException("Lesson", lessonName));
  }
}