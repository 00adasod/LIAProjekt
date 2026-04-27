package se.liaprojekt.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import se.liaprojekt.model.UserProgress;

import java.util.List;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    List<UserProgress> findByUserId(Long userId);
}
