package org.protu.contentservice.lesson.dto;

import jakarta.validation.constraints.Size;

public record LessonUpdateRequest(
    @Size(max = 30, message = "Lesson name must not exceed 30 characters")
    String name,
    Integer lessonOrder,
    String content) {
}
