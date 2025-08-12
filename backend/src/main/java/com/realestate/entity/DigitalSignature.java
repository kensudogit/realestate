package com.realestate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 電子署名エンティティクラス
 * 
 * 不動産取引における電子署名の情報を管理するエンティティです。
 * 署名者、署名内容、署名時刻、署名データなどを保持します。
 * 
 * @author システム開発チーム
 * @version 1.0.0
 * @since 2025-08-11
 */
@Entity
@Table(name = "digital_signatures")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DigitalSignature {

    /**
     * 電子署名ID（主キー）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 署名者ID
     */
    @Column(nullable = false)
    private Long signerId;

    /**
     * 署名者名
     */
    @Column(nullable = false)
    private String signerName;

    /**
     * 署名対象の契約ID
     */
    @Column(nullable = false)
    private Long contractId;

    /**
     * 署名対象の文書タイプ
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    /**
     * 署名データ（暗号化された署名）
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String signatureData;

    /**
     * 署名ハッシュ
     */
    @Column(nullable = false, unique = true)
    private String signatureHash;

    /**
     * 署名時刻
     */
    @Column(nullable = false)
    private LocalDateTime signedAt;

    /**
     * 署名の有効期限
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 署名のステータス
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SignatureStatus status;

    /**
     * 署名の検証結果
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
        OTHER               // その他
    }

    /**
     * 署名ステータスの列挙型
     */
    public enum SignatureStatus {
        PENDING,            // 署名待ち
        SIGNED,             // 署名済み
        VERIFIED,           // 検証済み
        EXPIRED,            // 期限切れ
        REVOKED,            // 取り消し
        INVALID             // 無効
    }
}
