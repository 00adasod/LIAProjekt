package se.liaprojekt.dto;

public record TestAnswerRequest(
        String answerText,
        boolean isCorrect
) {}