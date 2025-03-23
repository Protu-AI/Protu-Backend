package org.protu.contentservice.track.dto;

import org.protu.contentservice.course.Course;

import java.sql.Timestamp;
import java.util.List;

public record TrackResponse(Integer id, String name, String description,
                            List<Course> courses, Timestamp createdAt, Timestamp updatedAt) {
}
