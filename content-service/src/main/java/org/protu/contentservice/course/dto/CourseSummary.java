package org.protu.contentservice.course.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.protu.contentservice.lesson.dto.LessonSummary;

import java.sql.Timestamp;
import java.util.List;

@JsonPropertyOrder({"id", "name", "description", "coursePicURL", "lessons", "createdAt", "updatedAt"})
public record CourseSummary(Integer id, String name, String description, String coursePicURL,
                            Timestamp createdAt, Timestamp updatedAt,
                            List<LessonSummary> lessons) {
}
