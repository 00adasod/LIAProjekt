package se.liaprojekt.dto;

public record CourseProgressResponse(
        Long courseId,
        String title,
        int totalSections,
        int completedSections,
        int progressPercentage
) {}
