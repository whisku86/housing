package com.smart_housing.smart_housing.model;

import jakarta.persistence.*;


@Entity
@Table(name = "landlords")
public class Landlord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "landlord_Id")
    private Long landlordId;

    @Column(name = "Name")
    private String name;

    @Column(unique = true)
    private String email;

    @Column(length = 100)
    private String password;

    private String phone;
    @Column(name = "status", columnDefinition = "ENUM('active', 'inactive') DEFAULT 'active'")
    private String status = "active";

    // Getters and Setters
    public Long getLandlordId() {
        return landlordId;
    }

    public void setLandlordId(Long landlordId) {
        this.landlordId = landlordId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
