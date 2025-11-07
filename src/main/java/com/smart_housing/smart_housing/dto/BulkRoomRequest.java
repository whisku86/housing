package com.smart_housing.smart_housing.dto;



import com.smart_housing.smart_housing.model.RoomStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data // ← This generates getters/setters automatically
public class BulkRoomRequest {

    @Min(value = 1, message = "Count must be at least 1")
    private int count;

    @NotNull(message = "Room number prefix is required")
    private String roomNumberPrefix;

    @NotNull(message = "Starting room number suffix is required")
    private String roomNumberStart; // e.g., "01", "1", "A"

    @NotNull(message = "Price is required")
    private BigDecimal price;

    private RoomStatus status = RoomStatus.VACANT;
}