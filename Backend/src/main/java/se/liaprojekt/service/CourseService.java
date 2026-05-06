package se.liaprojekt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.liaprojekt.dto.CourseProgressResponse;
import se.liaprojekt.dto.CourseRequest;
import se.liaprojekt.dto.CourseResponse;
import se.liaprojekt.exception.ResourceNotFoundException;
import se.liaprojekt.model.Course;
import se.liaprojekt.model.Section;
import se.liaprojekt.model.TestResult;
import se.liaprojekt.repository.CourseRepository;
import se.liaprojekt.repository.TestResultRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found with id: " + id)
                );

        return mapToResponse(course);
    }

    public CourseResponse createCourse(CourseRequest request) {
        Course course = new Course();
        course.setTitle(request.title());
        course.setDescription(request.description());
        // TODO: ändra CreatedBy till authenticatied user
        course.setCreatedBy("system");

        Course saved = courseRepository.save(course);

        return mapToResponse(saved);
    }

    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found with id: " + id)
                );

        course.setTitle(request.title());
        course.setDescription(request.description());

        return mapToResponse(courseRepository.save(course));
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    private CourseResponse mapToResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCreatedBy()
        );
    }

    // =========================
// GET COURSE PROGRESS
// =========================
    public CourseProgressResponse getCourseProgress(Long courseId, String entraId) {

        // =========================
        // FETCH COURSE
        // =========================
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        List<Section> sections = course.getSections();

        int totalSections = sections.size();

        // =========================
        // COUNT COMPLETED SECTIONS
        // =========================
        int completedSections = (int) sections.stream()
                .filter(section -> {

                    TestResult lastAttempt = testResultRepository
                            .findByUser_EntraIdAndSectionIdOrderByAttemptNumberDesc(
                                    entraId,
                                    section.getId()
                            )
                            .stream()
                            .findFirst()
                            .orElse(null);

                    return lastAttempt != null &&
                            lastAttempt.getStatus() == TestResult.Status.COMPLETED;
                })
                .count();

        // =========================
        // CALCULATE %
        // =========================
        int progress = totalSections == 0
                ? 0
                : (int) ((completedSections * 100.0) / totalSections);

        return new CourseProgressResponse(
                course.getId(),
                course.getTitle(),
                totalSections,
                completedSections,
                progress
        );
    }
}
