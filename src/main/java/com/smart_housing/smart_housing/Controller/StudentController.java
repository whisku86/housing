package com.smart_housing.smart_housing.Controller;

import com.smart_housing.smart_housing.model.Student;
import com.smart_housing.smart_housing.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://127.0.0.1:8080", "http://localhost:8080"})
@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping("/register")
    public String register(@RequestBody Student student) {
        return studentService.register(student);
    }

    @PostMapping("/login")
    public String login(@RequestBody Student student) {
        return studentService.login(student.getEmail(), student.getPassword());
    }
}
