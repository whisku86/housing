package com.smart_housing.smart_housing.dto;



import com.smart_housing.smart_housing.model.RoomStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomRequest {
    private String roomNumber;
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;
    private RoomStatus status = RoomStatus.VACANT;
}