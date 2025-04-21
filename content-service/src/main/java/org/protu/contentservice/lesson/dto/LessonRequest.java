package org.protu.contentservice.lesson.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LessonRequest(
    @NotBlank(message = "Lesson name is required") @Size(max = 30, message = "Lesson name must not exceed 30 characters") String name,
    Integer lessonOrder, @NotBlank(message = "Lesson content is required") String content) {
}
