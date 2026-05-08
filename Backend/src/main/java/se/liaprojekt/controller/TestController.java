package se.liaprojekt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.liaprojekt.dto.*;
import se.liaprojekt.service.CurrentUserService;
import se.liaprojekt.service.TestService;

import java.util.List;

@RestController
@RequestMapping("/api/courses/sections/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;
    private final CurrentUserService currentUserService;

    // CREATE QUESTION (ADMIN)
    @PostMapping("/{sectionId}/questions")
    public ResponseEntity<Void> createQuestion(
            @PathVariable Long sectionId,
            @RequestBody TestQuestionRequest request
    ) {
        testService.createQuestion(sectionId, request);
        return ResponseEntity.ok().build();
    }

    // START TEST
    @PostMapping("/{sectionId}/start")
    public ResponseEntity<TestResultResponse> startTest(@PathVariable Long sectionId) {

        String entraId = currentUserService.getEntraId();

        return ResponseEntity.ok(
                testService.startTest(entraId, sectionId)
        );
    }

    // SUBMIT ANSWER
    @PostMapping("/answer")
    public ResponseEntity<Void> submitAnswer(@RequestBody SubmitAnswerRequest request) {

        testService.submitAnswer(
                request.testResultId(),
                request.questionId(),
                request.answerId()
        );

        return ResponseEntity.ok().build();
    }

    // SUBMIT TEST
    @PostMapping("/{testResultId}/submit")
    public ResponseEntity<TestResultResponse> submitTest(
            @PathVariable Long testResultId) {

        return ResponseEntity.ok(
                testService.submitTest(testResultId)
        );
    }

    // GET TEST QUESTIONS
    @GetMapping("/{sectionId}/questions")
    public ResponseEntity<List<TestQuestionResponse>> getQuestions(
            @PathVariable Long sectionId) {

        return ResponseEntity.ok(
                testService.getQuestions(sectionId)
        );
    }

    @GetMapping("/{sectionId}/attempts")
    public ResponseEntity<List<TestResultResponse>> getAttempts(
            @PathVariable Long sectionId
    ) {
        String entraId = currentUserService.getEntraId();

        return ResponseEntity.ok(
                testService.getAttempts(entraId, sectionId)
        );
    }
}