package org.protu.contentservice.lesson;

import org.protu.contentservice.common.exception.custom.EntityNotFoundException;
import org.protu.contentservice.lesson.dto.LessonRequest;
import org.protu.contentservice.lesson.dto.LessonUpdateRequest;
import org.protu.contentservice.lesson.dto.LessonWithContent;
import org.protu.contentservice.lesson.dto.LessonWithoutContent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LessonService {

  private final LessonRepository lessons;

  public LessonService(LessonRepository lessons) {
    this.lessons = lessons;
  }

  @Transactional
  public void createLesson(LessonRequest lessonRequest) {
    lessons.add(lessonRequest);
  }

  @Transactional(readOnly = true)
  public LessonWithContent findByName(String lessonName) {
    return lessons.findByName(lessonName)
        .orElseThrow(() -> new EntityNotFoundException("Lesson", lessonName));
  }

  @Transactional(readOnly = true)
  public LessonWithoutContent findByNameWithoutContent(String lessonName) {
    return lessons.findByNameWithoutContent(lessonName)
        .orElseThrow(() -> new EntityNotFoundException("Lesson", lessonName));
  }

  @Transactional
  public void updateLesson(String lessonName, LessonUpdateRequest lessonRequest) {
    lessons.update(lessonName, lessonRequest);
  }

  @Transactional
  public void deleteLesson(String lessonName) {
    lessons.delete(lessonName);
  }
}
