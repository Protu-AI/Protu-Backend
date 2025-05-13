package org.protu.contentservice.course;

import org.protu.contentservice.lesson.dto.LessonWithoutContent;

import java.util.List;

public record CourseWithLessons(
    Integer id,
    String name,
    String description,
    String picUrl,
    List<LessonWithoutContent> lessons) {
}
