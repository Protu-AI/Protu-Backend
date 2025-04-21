package org.protu.contentservice.progress.userlesson;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.protu.contentservice.lesson.Lesson;
import org.protu.contentservice.progress.user.User;

@Entity
@Table(name = "users_lessons")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersLessons {

  @EmbeddedId
  private UsersLessonsPK id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId(value = "userId")
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId(value = "lessonId")
  @JoinColumn(name = "lesson_id")
  private Lesson lesson;

  @Column(name = "is_completed", nullable = false)
  private Boolean isCompleted = false;
}
