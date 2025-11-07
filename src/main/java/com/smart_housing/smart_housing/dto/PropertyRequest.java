package com.smart_housing.smart_housing.dto;



import com.smart_housing.smart_housing.model.PropertyType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PropertyRequest {

    @NotBlank private String name;
    @NotBlank private String location;
    @NotNull private PropertyType type;
    @Min(1) @Max(100) private Integer maxOccupancy;
    @NotNull @DecimalMin(value = "0.01") private BigDecimal price;
    private List<String> bills;
    private String securityDetails;
    private List<String> amenities;
    private List<String> images;

}