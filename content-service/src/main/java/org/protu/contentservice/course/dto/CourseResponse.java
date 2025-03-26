package org.protu.contentservice.course.dto;

import org.protu.contentservice.lesson.dto.LessonSummaryResponse;

import java.sql.Timestamp;
import java.util.List;

public record CourseResponse(Integer id, String name, String description,
                             List<LessonSummaryResponse> lessons, Timestamp createdAt, Timestamp updatedAt) {
}
