package se.liaprojekt.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser() {
        return ResponseEntity.ok("OK - getCurrentUser");
    }

    @GetMapping("/me/courses")
    public ResponseEntity<List<CourseResponse>> getMyCourses() {
        //TODO
        return ResponseEntity.ok(List.of());
    }

}