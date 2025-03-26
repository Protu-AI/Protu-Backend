package org.protu.contentservice.lesson;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.protu.contentservice.course.Course;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(
    name = "lessons",
    indexes = {
        @Index(name = "idx_lessons_name", columnList = "name"),
        @Index(name = "idx_lessons_lessonOrder", columnList = "lesson_order")
    })
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Lesson {
  @Id
  @Column(name = "id", nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Integer id;

  @Column(name = "name", nullable = false, unique = true, length = 30)
  String name;

  @Column(name = "content", columnDefinition = "TEXT")
  String content;

  @Column(name = "lesson_order", nullable = false, unique = true)
  Integer lessonOrder = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id")
  private Course course;

  @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  Timestamp createdAt;

  @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  Timestamp updatedAt;

  @PrePersist
  public void onCreate() {
    this.createdAt = Timestamp.from(Instant.now());
    this.updatedAt = Timestamp.from(Instant.now());
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = Timestamp.from(Instant.now());
  }
}
