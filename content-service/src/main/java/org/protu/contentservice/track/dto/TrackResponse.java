package org.protu.contentservice.track.dto;

import org.protu.contentservice.course.dto.CourseSummaryResponse;

import java.sql.Timestamp;
import java.util.List;

public record TrackResponse(Integer id, String name, String description,
                            List<CourseSummaryResponse> courses, Timestamp createdAt, Timestamp updatedAt) {
}
