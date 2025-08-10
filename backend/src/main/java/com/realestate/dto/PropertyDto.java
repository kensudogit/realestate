package com.realestate.dto;

import com.realestate.entity.Property.PropertyType;
import com.realestate.entity.Property.PropertyStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDto {
    
    private Long id;
    private String name;
    private String address;
    private String description;
    private PropertyType type;
    private PropertyStatus status;
    private BigDecimal price;
    private BigDecimal area;
    private Integer rooms;
    private Integer bathrooms;
    private Integer parkingSpaces;
    private Integer yearBuilt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
