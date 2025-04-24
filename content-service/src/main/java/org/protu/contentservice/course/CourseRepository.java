package org.protu.contentservice.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {


  @Query("SELECT c FROM Course c LEFT JOIN FETCH c.lessons l WHERE c.name = :name")
  Optional<Course> findCourseByName(@Param("name") String name);

  @Query("SELECT c.id, c.name, c.description, c.coursePicURL, c.createdAt, c.updatedAt, " +
      "l.id, l.name, l.lessonOrder, l.createdAt, l.updatedAt " +
      "FROM Course c LEFT JOIN c.lessons l")
  List<Object[]> findAllProjectedBy();
}
