package org.protu.contentservice.course;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {

  @NonNull
  @Query("SELECT c FROM Course c LEFT JOIN FETCH c.lessons")
  List<Course> findAll();

  @Query("SELECT c FROM Course c LEFT JOIN FETCH c.lessons WHERE c.name = :name")
  Optional<Course> findCourseByName(@Param("name") String name);
}
