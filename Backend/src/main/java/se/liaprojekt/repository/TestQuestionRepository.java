package se.liaprojekt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.liaprojekt.model.TestQuestion;

public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {
}
