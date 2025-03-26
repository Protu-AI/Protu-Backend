package org.protu.contentservice.track;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackRepository extends JpaRepository<Track, Integer> {

  @NonNull
  @Query("SELECT t FROM Track t LEFT JOIN FETCH t.courses")
  List<Track> findAll();

  @Query("SELECT t FROM Track t LEFT JOIN FETCH t.courses WHERE t.name = :name")
  Optional<Track> findTracksByName(@Param("name") String name);
}
