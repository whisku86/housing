package com.smart_housing.smart_housing.Controller;

import com.smart_housing.smart_housing.model.Student;
import com.smart_housing.smart_housing.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*") // Allows your frontend to connect
@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    // Registration (Kept simple as you had it)
    @PostMapping("/register")
    public String register(@RequestBody Student student) {
        return studentService.register(student);
    }

    // Login (Updated to return JSON so frontend works)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Student loginRequest) {
        // 1. Call your Simple Service (No password encoder)
        Student student = studentService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());

        if (student != null) {
            // 2. Create the JSON response
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful!");
            response.put("token", "dummy-token-123");

            // 3. Add User Details (Required for the Booking button to appear)
            Map<String, Object> userSafe = new HashMap<>();
            userSafe.put("id", student.getStudentId());
            userSafe.put("name", student.getName());
            userSafe.put("email", student.getEmail());
            userSafe.put("role", "STUDENT");

            response.put("user", userSafe);

            return ResponseEntity.ok(response);
        } else {
            // Return Error as JSON
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid email or password");
            return ResponseEntity.status(401).body(error);
        }
    }
}
