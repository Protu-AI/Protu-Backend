package org.protu.contentservice.course;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.protu.contentservice.lesson.Lesson;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Entity
@Table(
    name = "courses",
    indexes = {
        @Index(name = "idx_courses_name", columnList = "name"),
        @Index(name = "idx_courses_trackId", columnList = "track_id")
    })
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Course {
  @Id
  @Column(name = "id", nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Integer id;

  @Column(name = "name", nullable = false, unique = true, length = 30)
  String name;

  @Column(name = "description", columnDefinition = "TEXT")
  String description;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  List<Lesson> lessons;

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
