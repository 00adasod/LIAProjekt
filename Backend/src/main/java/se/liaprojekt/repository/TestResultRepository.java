package se.liaprojekt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.liaprojekt.model.TestResult;

import java.util.List;
import java.util.Optional;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    Optional<TestResult> findByUser_EntraIdAndSectionId(String entraId, Long sectionId);

    List<TestResult> findByUser_EntraId(String entraId);
}
