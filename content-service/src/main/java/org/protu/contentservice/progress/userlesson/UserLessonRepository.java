package org.protu.contentservice.progress.userlesson;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLessonRepository extends JpaRepository<UsersLessons, UsersLessonsPK> {
}
