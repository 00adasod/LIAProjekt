package se.liaprojekt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.liaprojekt.dto.*;
import se.liaprojekt.exception.BadRequestException;
import se.liaprojekt.exception.ResourceNotFoundException;
import se.liaprojekt.model.*;
import se.liaprojekt.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestResultRepository testResultRepository;
    private final SectionRepository sectionRepository;
    private final TestQuestionRepository questionRepository;
    private final AnsweredQuestionRepository answeredQuestionRepository;
    private final UserRepository userRepository;


    @Transactional
    public void createQuestion(Long sectionId, TestQuestionRequest request) {

        // =========================
        // Hämta section
        // =========================
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Section not found: " + sectionId));

        // =========================
        // VALIDERING
        // =========================
        long correctCount = request.answers()
                .stream()
                .filter(TestAnswerRequest::isCorrect)
                .count();

        if (correctCount != 1) {
            throw new BadRequestException("A question must have exactly one correct answer");
        }

        // =========================
        // Skapa question
        // =========================
        TestQuestion question = new TestQuestion();
        question.setQuestionText(request.questionText());
        question.setSection(section);

        // =========================
        // Skapa answers (kopplade till question)
        // =========================
        List<TestAnswer> answers = request.answers().stream()
                .map(dto -> {
                    TestAnswer answer = new TestAnswer();
                    answer.setAnswerText(dto.answerText());
                    answer.setIsCorrect(dto.isCorrect());
                    answer.setQuestion(question);
                    return answer;
                })
                .toList();

        question.setAnswers(answers);

        // =========================
        // Spara (cascade sparar answers automatiskt)
        // =========================
        questionRepository.save(question);
    }

    // =========================
    // START TEST
    // =========================
    @Transactional
    public TestResultResponse startTest(String entraId, Long sectionId) {

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        User user =  userRepository.findByEntraId(entraId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // =========================
        // CHECK SECTION ACCESS (LOCK VALIDATION)
        // =========================
        if (isSectionLocked(user, section)) {
            throw new BadRequestException("Section is locked. Complete previous section first.");
        }

        // =========================
        // CHECK IF EXISTING TEST
        // =========================
        Optional<TestResult> existing = testResultRepository
                .findByUser_EntraIdAndSectionId(entraId, sectionId);

        TestResult result;

        if (existing.isPresent()) {

            result = existing.get();

            // =========================
            // ALREADY PASSED → BLOCK RETRY
            // =========================
            if (result.getStatus() == TestResult.Status.COMPLETED) {
                throw new BadRequestException("Test already completed and cannot be retaken");
            }

            // =========================
            // FAILED → ALLOW RETRY (RESET)
            // =========================
            if (result.getStatus() == TestResult.Status.FAILED) {

                result.setStatus(TestResult.Status.IN_PROGRESS);
                result.setPassed(false);
                result.setScore(null);
                result.setCompletedAt(null);

                result = testResultRepository.save(result);
            }

            // IN_PROGRESS → use existing result
        } else {

            // =========================
            // FIRST TIME START
            // =========================
            result = new TestResult();
            result.setUser(user);
            result.setSection(section);
            result.setStatus(TestResult.Status.IN_PROGRESS);
            result.setPassed(false);

            result = testResultRepository.save(result);
        }

        // =========================
        // RETURN DTO (ALWAYS CLEAN)
        // =========================
        return new TestResultResponse(
                result.getId(),
                result.getStatus().name(),
                result.getScore(),
                result.isPassed(),
                result.getStartedAt(),
                result.getCompletedAt()
        );
    }

    // =========================
    // SUBMIT ANSWER
    // =========================
    @Transactional
    public void submitAnswer(Long testResultId, Long questionId, Long answerId) {

        // =========================
        // FETCH TEST
        // =========================
        TestResult result = testResultRepository.findById(testResultId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        // =========================
        // FETCH QUESTION
        // =========================
        TestQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        // =========================
        // CHECK IF ALREADY ANSWERED (FAST EXIT)
        // =========================
        if (answeredQuestionRepository
                .findByTestResult_IdAndQuestion_Id(testResultId, questionId)
                .isPresent()) {
            return;
        }

        // =========================
        // RESOLVE SELECTED ANSWER
        // =========================
        TestAnswer selectedAnswer = question.getAnswers()
                .stream()
                .filter(a -> a.getId().equals(answerId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found"));

        // =========================
        // CREATE ANSWER ENTITY
        // =========================
        AnsweredQuestion answered = new AnsweredQuestion();
        answered.setQuestion(question);
        answered.setTestResult(result);
        answered.setCorrect(selectedAnswer.getIsCorrect());

        // =========================
        // SAVE WITH DB SAFETY NET
        // =========================
        try {
            answeredQuestionRepository.save(answered);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // race condition → ignore duplicate
        }
    }

    // =========================
    // SUBMIT TEST
    // =========================
    @Transactional
    public TestResultResponse submitTest(Long testResultId) {

        // Hämta testresultatet (själva "test-sessionen")
        TestResult result = testResultRepository.findById(testResultId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        // Hämta alla svar som användaren har lämnat i detta test
        List<AnsweredQuestion> answers =
                answeredQuestionRepository.findByTestResult_Id(testResultId);

        // =========================
        // RÄKNA RÄTT SVAR
        // =========================
        // Här filtrerar vi fram alla svar som är markerade som korrekta
        long correct = answers.stream()
                .filter(AnsweredQuestion::isCorrect)
                .count();

        // Totalt antal frågor som användaren har svarat på
        int total = answers.size();

        // =========================
        // BERÄKNA POÄNG (SCORE)
        // =========================
        // Score räknas som procent:
        // (antal rätt / totalt antal frågor) * 100
        //
        // Exempel:
        // 7 rätt av 10 → 70%
        int score = total == 0 ? 0 : (int) ((correct * 100.0) / total);

        // Spara poängen i testresultatet
        result.setScore(score);

        // Sätt när testet blev färdigställt
        result.setCompletedAt(LocalDateTime.now());

        // =========================
        // BESTÄM OM TESTET ÄR GODKÄNT
        // =========================
        // Här är "kravet" för att klara testet
        // Just nu: minst 70% rätt
        boolean passed = score >= 70;

        result.setPassed(passed);

        // =========================
        // SÄTT STATUS BASERAT PÅ RESULTAT
        // =========================
        // Om godkänt → COMPLETED
        // Om under krav → FAILED
        result.setStatus(
                passed ? TestResult.Status.COMPLETED : TestResult.Status.FAILED
        );

        // =========================
        // SPARA RESULTATET
        // =========================
        TestResult saved = testResultRepository.save(result);

        return mapToResponse(saved);
    }

    private TestResultResponse mapToResponse(TestResult result) {
        return new TestResultResponse(
                result.getId(),
                result.getStatus().name(),
                result.getScore(),
                result.isPassed(),
                result.getStartedAt(),
                result.getCompletedAt()
        );
    }

    public boolean isSectionLocked(User user, Section section) {

        if (section.getOrderIndex() == 0) {
            return false;
        }

        Section previous = sectionRepository
                .findByCourseIdAndOrderIndex(
                        section.getCourse().getId(),
                        section.getOrderIndex() - 1
                )
                .orElseThrow(() -> new ResourceNotFoundException("Previous section not found"));

        return testResultRepository
                .findByUser_EntraIdAndSectionId(user.getEntraId(), previous.getId())
                .map(result -> result.getStatus() != TestResult.Status.COMPLETED)
                .orElse(true); // om inget test finns = locked
    }

    public List<TestQuestionResponse> getQuestions(Long sectionId) {

        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        return section.getTestQuestions()
                .stream()
                .map(q -> new TestQuestionResponse(
                        q.getId(),
                        q.getQuestionText(),
                        q.getAnswers().stream()
                                .map(a -> new TestAnswerResponse(
                                        a.getId(),
                                        a.getAnswerText()
                                ))
                                .toList()
                ))
                .toList();
    }
}