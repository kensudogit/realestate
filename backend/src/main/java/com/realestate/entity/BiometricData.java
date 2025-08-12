package com.realestate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 生体認証データエンティティクラス
 * 
 * 不動産取引における生体認証（指紋・掌紋）の情報を管理するエンティティです。
 * 認証者、生体データ、登録時刻、検証結果などを保持します。
 * 
 * @author システム開発チーム
 * @version 1.0.0
 * @since 2025-08-11
 */
@Entity
@Table(name = "biometric_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiometricData {

    /**
     * 生体認証データID（主キー）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 認証者ID
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 認証者名
     */
    @Column(nullable = false)
    private String userName;

    /**
     * 生体データタイプ
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BiometricType biometricType;

    /**
     * 生体データ（暗号化されたデータ）
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String biometricData;

    /**
     * 生体データのハッシュ
     */
    @Column(nullable = false, unique = true)
    private String biometricHash;

    /**
     * データ品質スコア（0-100）
     */
    @Column(nullable = false)
    private Integer qualityScore;

    /**
     * 登録日時
     */
    @Column(nullable = false)
    private LocalDateTime registeredAt;

    /**
     * 最終使用日時
     */
    @Column
    private LocalDateTime lastUsedAt;

    /**
     * データの有効期限
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * データのステータス
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BiometricStatus status;

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
     * 生体データタイプの列挙型
     */
    public enum BiometricType {
        FINGERPRINT_LEFT_THUMB,      // 左手親指
        FINGERPRINT_LEFT_INDEX,      // 左手人差し指
        FINGERPRINT_LEFT_MIDDLE,     // 左手中指
        FINGERPRINT_LEFT_RING,       // 左手薬指
        FINGERPRINT_LEFT_PINKY,      // 左手小指
        FINGERPRINT_RIGHT_THUMB,     // 右手親指
        FINGERPRINT_RIGHT_INDEX,     // 右手人差し指
        FINGERPRINT_RIGHT_MIDDLE,    // 右手中指
        FINGERPRINT_RIGHT_RING,      // 右手薬指
        FINGERPRINT_RIGHT_PINKY,     // 右手小指
        PALM_PRINT_LEFT,             // 左手掌紋
        PALM_PRINT_RIGHT,            // 右手掌紋
        FACE_RECOGNITION,            // 顔認識
        IRIS_SCAN,                   // 虹彩スキャン
        VOICE_PRINT                  // 声紋
    }

    /**
     * 生体データステータスの列挙型
     */
    public enum BiometricStatus {
        ACTIVE,             // 有効
        INACTIVE,           // 無効
        EXPIRED,            // 期限切れ
        SUSPENDED,          // 一時停止
        DELETED             // 削除済み
    }
}
