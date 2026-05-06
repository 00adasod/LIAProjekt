package se.liaprojekt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.liaprojekt.model.TestResult;

import java.util.List;
import java.util.Optional;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {

    // =========================
    // ALL USER RESULTS (ALL SECTIONS)
    // =========================
    List<TestResult> findByUser_EntraId(String entraId);

    // =========================
    // ALL ATTEMPTS FOR ONE SECTION
    // =========================
    List<TestResult> findByUser_EntraIdAndSectionId(
            String entraId,
            Long sectionId
    );

    // =========================
    // GET ATTEMPTS SORTED BY ATTEMPT NUMBER (IMPORTANT)
    // =========================
    List<TestResult> findByUser_EntraIdAndSectionIdOrderByAttemptNumberDesc(
            String entraId,
            Long sectionId
    );

    // =========================
    // GET LATEST ATTEMPT
    // =========================
    Optional<TestResult> findTopByUser_EntraIdAndSectionIdOrderByAttemptNumberDesc(
            String entraId,
            Long sectionId
    );
}
