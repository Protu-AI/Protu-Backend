package org.protu.contentservice.lesson.dto;

import java.sql.Timestamp;

public record LessonResponse(Integer id, String name, Integer lessonOrder, String content, Timestamp createdAt,
                             Timestamp updatedAt) {
}
