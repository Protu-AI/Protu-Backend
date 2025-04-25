package org.protu.contentservice.lesson.dto;

public record LessonsWithCompletion(Integer id, String name, Integer lessonOrder, Boolean isCompleted) {
  public LessonsWithCompletion(Integer id, String name, Integer lessonOrder, Boolean isCompleted) {
    this.id = id;
    this.name = name;
    this.lessonOrder = lessonOrder;
    this.isCompleted = isCompleted != null ? isCompleted : false;
  }
}
