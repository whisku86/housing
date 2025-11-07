package com.smart_housing.smart_housing.service;

import com.smart_housing.smart_housing.model.Student;
import com.smart_housing.smart_housing.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public String register(Student student) {
        if (studentRepository.findByEmail(student.getEmail()) != null) {
            return "Email already exists!";
        }
        studentRepository.save(student);
        return "Student registered successfully!";
    }

    public String login(String email, String password) {
        Student student = studentRepository.findByEmail(email);
        if (student == null) {
            return "Email not found!";
        }
        if (!student.getPassword().equals(password)) {
            return "Incorrect password!";
        }
        return "Login successful!";
    }
}
