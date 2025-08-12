package com.realestate.service;

import com.realestate.dto.DigitalTimestampDto;
import com.realestate.entity.DigitalTimestamp;
import com.realestate.repository.DigitalTimestampRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DigitalTimestampService {

    private final DigitalTimestampRepository timestampRepository;

    /**
     * 電子タイムスタンプを作成
     */
    public DigitalTimestamp createTimestamp(DigitalTimestampDto timestampDto) {
        DigitalTimestamp timestamp = new DigitalTimestamp();
        timestamp.setDocumentId(Long.parseLong(timestampDto.getDocumentId()));
        timestamp.setDocumentType(timestampDto.getDocumentType());
        timestamp.setTimestampAt(LocalDateTime.now());
        timestamp.setTimestampCertificate(timestampDto.getTimestampCertificate());
        timestamp.setTimestampAuthority(timestampDto.getTimestampAuthority());
        timestamp.setStatus(DigitalTimestamp.TimestampStatus.ACTIVE);
        
        return timestampRepository.save(timestamp);
    }

    /**
     * 電子タイムスタンプを検証
     */
    public boolean verifyTimestamp(Long timestampId) {
        Optional<DigitalTimestamp> optionalTimestamp = timestampRepository.findById(timestampId);
        if (optionalTimestamp.isPresent()) {
            DigitalTimestamp timestamp = optionalTimestamp.get();
            // 基本的な検証ロジック（実際の実装ではより複雑な検証を行う）
            return timestamp.getStatus() == DigitalTimestamp.TimestampStatus.ACTIVE;
        }
        return false;
    }

    /**
     * 文書IDでタイムスタンプ一覧を取得
     */
    public List<DigitalTimestamp> getTimestampsByDocument(String documentId) {
        return timestampRepository.findByDocumentId(Long.parseLong(documentId));
    }

    /**
     * タイムスタンプIDでタイムスタンプを取得
     */
    public DigitalTimestamp getTimestampById(Long timestampId) {
        return timestampRepository.findById(timestampId).orElse(null);
    }

    /**
     * すべてのタイムスタンプを取得
     */
    public List<DigitalTimestamp> getAllTimestamps() {
        return timestampRepository.findAll();
    }

    /**
     * タイムスタンプを削除
     */
    public void deleteTimestamp(Long timestampId) {
        timestampRepository.deleteById(timestampId);
    }

    /**
     * タイムスタンプの有効性を更新
     */
    public void updateTimestampStatus(Long timestampId, DigitalTimestamp.TimestampStatus status) {
        Optional<DigitalTimestamp> optionalTimestamp = timestampRepository.findById(timestampId);
        if (optionalTimestamp.isPresent()) {
            DigitalTimestamp timestamp = optionalTimestamp.get();
            timestamp.setStatus(status);
            timestampRepository.save(timestamp);
        }
    }
}
