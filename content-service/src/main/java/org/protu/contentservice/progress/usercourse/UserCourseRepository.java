package org.protu.contentservice.progress.usercourse;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCourseRepository extends JpaRepository<UsersCourses, UsersCoursesPK> {

  @NativeQuery("SELECT completed_lessons FROM users_courses WHERE user_id = :userId AND course_id = :courseId")
  Integer getCompletedLessonsByUserForCourse(Long userId, Integer courseId);
}
