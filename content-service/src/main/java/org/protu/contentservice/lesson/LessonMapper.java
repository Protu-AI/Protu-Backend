package org.protu.contentservice.lesson;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.protu.contentservice.lesson.dto.LessonRequest;
import org.protu.contentservice.lesson.dto.LessonResponse;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LessonMapper {

  LessonResponse toLessonDto(Lesson lesson);

  Lesson toLessonEntity(LessonRequest lessonRequest);

  List<LessonResponse> toLessonDtoList(List<Lesson> lessons);
}
