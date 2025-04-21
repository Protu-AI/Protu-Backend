package org.protu.contentservice.lesson.dto;

import java.sql.Timestamp;

public record LessonSummary(Integer id, String name, Integer lessonOrder, Timestamp createdAt, Timestamp updatedAt) {
}
