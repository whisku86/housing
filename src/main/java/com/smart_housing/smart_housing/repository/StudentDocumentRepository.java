package com.smart_housing.smart_housing.repository;

import com.smart_housing.smart_housing.model.StudentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentDocumentRepository extends JpaRepository<StudentDocument, Integer> {
    List<StudentDocument> findByStudentId(int studentId);
}
