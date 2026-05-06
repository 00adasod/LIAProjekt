package se.liaprojekt.dto;

import java.util.List;

public record TestQuestionResponse(
        Long id,
        String questionText,
        List<TestAnswerResponse> answers
) {}
