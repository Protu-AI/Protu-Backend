package org.protu.contentservice.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseRequest(
    @NotBlank(message = "Course name is required") @Size(max = 30, message = "Course name must not exceed 30 characters") String name,
    @NotBlank(message = "description is required") String description) {
}
