package org.protu.contentservice.track;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.protu.contentservice.course.Course;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "tracks",
    indexes = {
        @Index(name = "idx_tracks_name", columnList = "name")
    })
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Track {
  @Id
  @Column(name = "id", nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Integer id;

  @Column(name = "name", nullable = false, unique = true, length = 30)
  String name;

  @Column(name = "description", columnDefinition = "TEXT")
  String description;

  @OneToMany(mappedBy = "track", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  List<Course> courses = new ArrayList<>();

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
