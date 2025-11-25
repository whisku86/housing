package com.smart_housing.smart_housing.model;

import jakarta.persistence.*;

import lombok.Data;
import java.sql.Timestamp;

@Entity
@Table(name = "landlords")
@Data
public class Landlord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "landlord_Id")
    private Long landlordId;

    @Column(name = "name")
    private String name;

    @Column(unique = true)
    private String email;

    @Column(length = 100)
    private String password;

    @Column(name = "phone")
    private String phone;

    @Column(name = "status", columnDefinition = "ENUM('ACTIVE','SUSPENDED') DEFAULT 'ACTIVE'")
    private String status = "ACTIVE";

    @Column(name = "approved_at")
    private Timestamp approvedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

}