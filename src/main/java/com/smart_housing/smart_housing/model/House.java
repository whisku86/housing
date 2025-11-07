package com.smart_housing.smart_housing.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "houses")
public class House {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "landlord_Id")
    private int landlordId;

    private String location;

    private BigDecimal rent;

    private String amenities;

    @Enumerated(EnumType.STRING)
    private HouseType image; // matches enum ('bedsitter','single_room','one_bedroom','other')

    @Column(name = "image_path")
    private String imagePath;

    @Enumerated(EnumType.STRING)
    private Status status; // matches enum ('vacant','booked','occupied')

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Enum definitions
    public enum HouseType {
        bedsitter,
        single_room,
        one_bedroom,
        other
    }

    public enum Status {
        vacant,
        booked,
        occupied
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLandlordId() { return landlordId; }
    public void setLandlordId(int landlordId) { this.landlordId = landlordId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public BigDecimal getRent() { return rent; }
    public void setRent(BigDecimal rent) { this.rent = rent; }

    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }

    public HouseType getImage() { return image; }
    public void setImage(HouseType image) { this.image = image; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
