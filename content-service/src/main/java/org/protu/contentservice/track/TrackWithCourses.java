package org.protu.contentservice.track;

import org.protu.contentservice.course.CourseDto;

import java.util.List;

public record TrackWithCourses(
    Integer id,
    String name,
    String description,
    List<CourseDto> courses) {
}
