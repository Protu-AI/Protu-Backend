package org.protu.contentservice.track;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.protu.contentservice.course.CourseDto;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public record TrackWithCourses(
    Integer id,
    String name,
    String description,
    List<CourseDto> courses) {
}
