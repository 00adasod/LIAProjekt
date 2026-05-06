package se.liaprojekt.dto;

import java.time.LocalDateTime;

public record TestResultResponse(
        Long id,
        String status,
        Integer score,
        boolean passed,
        LocalDateTime startedAt,
        LocalDateTime completedAt
) {}
