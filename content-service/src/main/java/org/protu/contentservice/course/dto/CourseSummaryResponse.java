package org.protu.contentservice.course.dto;

import java.sql.Timestamp;

public record CourseSummaryResponse(Integer id, String name, String description,
                                    Timestamp createdAt, Timestamp updatedAt) {
}
