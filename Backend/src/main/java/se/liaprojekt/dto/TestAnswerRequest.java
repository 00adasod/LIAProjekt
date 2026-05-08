package se.liaprojekt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TestAnswerRequest(
        String answerText,

        @JsonProperty("correct")
        boolean isCorrect
) {}