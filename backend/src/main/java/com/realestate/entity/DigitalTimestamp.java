package com.realestate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 電子タイムスタンプエンティティクラス
 * 
 * 不動産取引における電子タイムスタンプの情報を管理するエンティティです。
 * タイムスタンプ時刻、証明書、検証結果などを保持します。
 * 
 * @author システム開発チーム
 * @version 1.0.0
 * @since 2025-08-11
 */
@Entity
@Table(name = "digital_timestamps")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalTimestamp {

    /**
     * 電子タイムスタンプID（主キー）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * タイムスタンプ対象の文書ID
     */
    @Column(nullable = false)
    private Long documentId;

    /**
     * 文書タイプ
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    /**
     * タイムスタンプ時刻
     */
    @Column(nullable = false)
    private LocalDateTime timestampAt;

    /**
     * タイムスタンプ証明書
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String timestampCertificate;

    /**
     * タイムスタンプハッシュ
     */
    @Column(nullable = false, unique = true)
    private String timestampHash;

    /**
     * タイムスタンプ機関
     */
    @Column(nullable = false)
    private String timestampAuthority;

    /**
     * タイムスタンプ機関の証明書
     */
    @Column(columnDefinition = "TEXT")
    private String authorityCertificate;

    /**
     * タイムスタンプの有効期限
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * タイムスタンプのステータス
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimestampStatus status;

    /**
     * 検証結果
     */
    @Column
    private String verificationResult;

    /**
     * 作成日時
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * エンティティ保存前の処理
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * エンティティ更新前の処理
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 文書タイプの列挙型
     */
    public enum DocumentType {
        CONTRACT,           // 契約書
        AGREEMENT,          // 合意書
        CONSENT,            // 同意書
        NOTICE,             // 通知書
        APPLICATION,        // 申請書
        SIGNATURE,          // 署名
        BIOMETRIC,          // 生体認証
        OTHER               // その他
    }

    /**
     * タイムスタンプステータスの列挙型
     */
    public enum TimestampStatus {
        ACTIVE,             // 有効
        EXPIRED,            // 期限切れ
        REVOKED,            // 取り消し
        INVALID             // 無効
    }
}
