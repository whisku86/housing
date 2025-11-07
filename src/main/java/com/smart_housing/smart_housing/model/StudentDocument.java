package com.smart_housing.smart_housing.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "upload_files")
public class StudentDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocType docType;

    @Column(nullable = false, length = 100)
    private String filePath;

    @Column(nullable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime uploadTime;

    // Constructors
    public StudentDocument() {}

    public StudentDocument(int studentId, DocType docType, String filePath) {
        this.studentId = studentId;
        this.docType = docType;
        this.filePath = filePath;
        this.uploadTime = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public DocType getDocType() {
        return docType;
    }

    public void setDocType(DocType docType) {
        this.docType = docType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    // Enum for document type
    public enum DocType {
        Adm_letter,
        National_Id,
        School_Id
    }
}
