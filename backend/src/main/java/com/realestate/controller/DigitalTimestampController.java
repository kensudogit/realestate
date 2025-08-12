package com.realestate.controller;

import com.realestate.dto.DigitalTimestampDto;
import com.realestate.entity.DigitalTimestamp;
import com.realestate.service.DigitalTimestampService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/timestamps")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DigitalTimestampController {

    private final DigitalTimestampService timestampService;

    /**
     * 電子タイムスタンプを作成
     */
    @PostMapping
    public ResponseEntity<DigitalTimestampDto> createTimestamp(@RequestBody DigitalTimestampDto timestampDto) {
        try {
            DigitalTimestamp timestamp = timestampService.createTimestamp(timestampDto);
            return ResponseEntity.ok(convertToDto(timestamp));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 電子タイムスタンプを検証
     */
    @PostMapping("/{timestampId}/verify")
    public ResponseEntity<Boolean> verifyTimestamp(@PathVariable Long timestampId) {
        try {
            boolean isValid = timestampService.verifyTimestamp(timestampId);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 文書IDでタイムスタンプ一覧を取得
     */
    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<DigitalTimestampDto>> getTimestampsByDocument(@PathVariable String documentId) {
        try {
            List<DigitalTimestamp> timestamps = timestampService.getTimestampsByDocument(documentId);
            List<DigitalTimestampDto> timestampDtos = timestamps.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(timestampDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * タイムスタンプIDでタイムスタンプを取得
     */
    @GetMapping("/{timestampId}")
    public ResponseEntity<DigitalTimestampDto> getTimestampById(@PathVariable Long timestampId) {
        try {
            DigitalTimestamp timestamp = timestampService.getTimestampById(timestampId);
            if (timestamp != null) {
                return ResponseEntity.ok(convertToDto(timestamp));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * すべてのタイムスタンプを取得
     */
    @GetMapping
    public ResponseEntity<List<DigitalTimestampDto>> getAllTimestamps() {
        try {
            List<DigitalTimestamp> timestamps = timestampService.getAllTimestamps();
            List<DigitalTimestampDto> timestampDtos = timestamps.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(timestampDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * タイムスタンプを削除
     */
    @DeleteMapping("/{timestampId}")
    public ResponseEntity<Void> deleteTimestamp(@PathVariable Long timestampId) {
        try {
            timestampService.deleteTimestamp(timestampId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * EntityをDtoに変換
     */
    private DigitalTimestampDto convertToDto(DigitalTimestamp timestamp) {
        DigitalTimestampDto dto = new DigitalTimestampDto();
        dto.setId(timestamp.getId());
        dto.setDocumentId(timestamp.getDocumentId().toString());
        dto.setDocumentType(timestamp.getDocumentType());
        dto.setTimestampAt(timestamp.getTimestampAt());
        dto.setTimestampCertificate(timestamp.getTimestampCertificate());
        dto.setTimestampAuthority(timestamp.getTimestampAuthority());
        dto.setStatus(timestamp.getStatus());
        return dto;
    }
}
