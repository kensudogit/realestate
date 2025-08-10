package com.realestate.dto;

import com.realestate.entity.Contract.ContractType;
import com.realestate.entity.Contract.ContractStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractDto {
    private Long id;
    private String contractNumber;
    private PropertyDto property;
    private ClientDto client;
    private String propertyName;
    private String clientName;
    private ContractType type;
    private ContractStatus status;
    private BigDecimal amount;
    private BigDecimal monthlyRent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String terms;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
