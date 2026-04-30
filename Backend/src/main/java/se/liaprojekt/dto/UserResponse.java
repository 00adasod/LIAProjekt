package se.liaprojekt.dto;

import java.util.ArrayList;
import java.util.List;

public class UserResponse {

    private Long id;        // intern DB-id
    private String entraId; // enda riktiga user-identiteten

    private List<Long> courseIds = new ArrayList<>();

    public UserResponse() {
    }

    public UserResponse(Long id, String entraId) {
        this.id = id;
        this.entraId = entraId;
    }

    public Long getId() {
        return id;
    }

    public String getEntraId() {
        return entraId;
    }

    public List<Long> getCourseIds() {
        return courseIds;
    }

    public void addCourse(Long courseId) {
        this.courseIds.add(courseId);
    }

    public void addCourses(List<Long> courseIds) {
        this.courseIds.addAll(courseIds);
    }
}
