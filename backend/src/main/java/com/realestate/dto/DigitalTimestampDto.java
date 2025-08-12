package com.realestate.dto;

import com.realestate.entity.DigitalTimestamp;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DigitalTimestampDto {
    private Long id;
    private String documentId;
    private DigitalTimestamp.DocumentType documentType;
    private LocalDateTime timestampAt;
    private String timestampCertificate;
    private String timestampAuthority;
    private DigitalTimestamp.TimestampStatus status;
}
