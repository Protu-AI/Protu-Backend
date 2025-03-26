package org.protu.contentservice.course;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.protu.contentservice.course.dto.CourseRequest;
import org.protu.contentservice.course.dto.CourseResponse;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CourseMapper {

  CourseResponse toCourseDto(Course course);

  Course toCourseEntity(CourseRequest course);

  List<CourseResponse> toCourseDtoList(List<Course> course);
}
