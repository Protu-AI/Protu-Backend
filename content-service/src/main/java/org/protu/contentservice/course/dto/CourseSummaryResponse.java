package org.protu.contentservice.course.dto;

import java.sql.Timestamp;

public record CourseSummaryResponse(Integer id, String name, String description,
                                    String coursePicURL, Timestamp createdAt, Timestamp updatedAt) {
}
