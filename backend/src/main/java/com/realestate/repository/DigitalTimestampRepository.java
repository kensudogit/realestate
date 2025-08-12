package com.realestate.repository;

import com.realestate.entity.DigitalTimestamp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DigitalTimestampRepository extends JpaRepository<DigitalTimestamp, Long> {
    
    /**
     * 文書IDでタイムスタンプを検索
     */
    List<DigitalTimestamp> findByDocumentId(Long documentId);
    
    /**
     * 文書タイプでタイムスタンプを検索
     */
    List<DigitalTimestamp> findByDocumentType(DigitalTimestamp.DocumentType documentType);
    
    /**
     * ステータスでタイムスタンプを検索
     */
    List<DigitalTimestamp> findByStatus(DigitalTimestamp.TimestampStatus status);
    
    /**
     * タイムスタンプ認証局でタイムスタンプを検索
     */
    List<DigitalTimestamp> findByTimestampAuthority(String timestampAuthority);
}
