package se.liaprojekt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.liaprojekt.dto.SectionResponse;
import se.liaprojekt.exception.ResourceNotFoundException;
import se.liaprojekt.model.Course;
import se.liaprojekt.model.Section;
import se.liaprojekt.model.TestResult;
import se.liaprojekt.repository.CourseRepository;
import se.liaprojekt.repository.SectionRepository;
import se.liaprojekt.repository.TestResultRepository;
import se.liaprojekt.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final TestResultRepository testResultRepository;
    private final UserRepository userRepository;

    public SectionResponse addSection(Long courseId, String title) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found: " + courseId));

        int nextIndex =

                //  Kolla om kursen inte har några sections alls
                course.getSections().isEmpty()

                        //  Om inga sections finns → första section får index 0
                        ? 0

                        //  Annars: det finns redan sections i kursen
                        : course.getSections()

                        //  Ta sista section i listan (högst orderIndex pga @OrderBy("orderIndex ASC"))
                          .get(course.getSections().size() - 1)

                        //  Hämta dess orderIndex
                          .getOrderIndex()

                          //  +1 = nästa lediga index
                          + 1;

        Section section = new Section();
        section.setTitle(title);
        section.setOrderIndex(nextIndex);
        section.setCourse(course);

        Section saved = sectionRepository.save(section);

        return mapToResponse(saved);
    }

    // =========================
    // GET SECTIONS (USER VIEW)
    // =========================
    public List<SectionResponse> getSections(Long courseId, String entraId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found"));

        return course.getSections()
                .stream()
                .map(section -> mapToResponse(section, entraId))
                .toList();
    }

    // =========================
    // LOCK LOGIC (ENTRA ID)
    // =========================
    public boolean isSectionLocked(Section section, String entraId) {

        // =========================
        // Första section är alltid upplåst
        // =========================
        if (section.getOrderIndex() == 0) {
            return false;
        }

        // =========================
        // Hitta föregående section
        // =========================
        Section previousSection = sectionRepository
                .findByCourseIdAndOrderIndex(
                        section.getCourse().getId(),
                        section.getOrderIndex() - 1
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException("Previous section not found"));

        // =========================
        // Kolla om user klarat testet (ENTRA ID)
        // =========================
        boolean previousCompleted = testResultRepository
                .findByUser_EntraIdAndSectionId(entraId, previousSection.getId())
                .map(result -> result.getStatus() == TestResult.Status.COMPLETED)
                .orElse(false);

        // =========================
        // lås om inte klarat
        // =========================
        return !previousCompleted;
    }

    // =========================
    // ADMIN MAPPING
    // =========================
    private SectionResponse mapToResponse(Section section) {
        return new SectionResponse(
                section.getId(),
                section.getTitle(),
                section.getOrderIndex(),
                section.getCourse().getId(),
                false
        );
    }

    // =========================
    // USER MAPPING
    // =========================
    private SectionResponse mapToResponse(Section section, String entraId) {
        return new SectionResponse(
                section.getId(),
                section.getTitle(),
                section.getOrderIndex(),
                section.getCourse().getId(),
                isSectionLocked(section, entraId)
        );
    }
}