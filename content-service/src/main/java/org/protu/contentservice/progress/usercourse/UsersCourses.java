package org.protu.contentservice.progress.usercourse;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.protu.contentservice.course.Course;
import org.protu.contentservice.progress.user.User;

@Entity
@Table(name = "users_courses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsersCourses {

  @EmbeddedId
  private UsersCoursesPK id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId(value = "userId")
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId(value = "courseId")
  @JoinColumn(name = "course_id")
  private Course course;

  @Column(name = "completed_lessons", nullable = false)
  private Integer completedLessons = 0;
}
