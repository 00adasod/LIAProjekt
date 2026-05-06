package se.liaprojekt.dto;

public record SubmitAnswerRequest(
        Long testResultId,
        Long questionId,
        Long answerId
) {}
