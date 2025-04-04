package org.protu.contentservice.progress.dto;

public record UserProgressInCourse(Integer courseId, Integer completedLessons, Integer totalLessons) {
}
