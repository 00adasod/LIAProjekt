package se.liaprojekt.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import se.liaprojekt.model.Section;

public interface SectionRepository extends JpaRepository<Section, Long> {
}