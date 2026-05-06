package se.liaprojekt.dto;

import java.util.List;

public record TestQuestionRequest(
        String questionText,
        List<TestAnswerRequest> answers
) {}
