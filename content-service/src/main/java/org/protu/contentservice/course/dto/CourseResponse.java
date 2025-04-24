package org.protu.contentservice.course.dto;

import org.protu.contentservice.lesson.dto.LessonSummary;

import java.sql.Timestamp;
import java.util.List;

public record CourseResponse(Integer id, String name, String description,
                             String coursePicURL, List<LessonSummary> lessons,
                             Timestamp createdAt, Timestamp updatedAt) {
}
