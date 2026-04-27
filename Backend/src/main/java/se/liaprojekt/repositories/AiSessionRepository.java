package se.liaprojekt.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import se.liaprojekt.model.AiSession;

public interface AiSessionRepository extends JpaRepository<AiSession, Long> {
}
