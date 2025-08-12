package com.realestate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 顧客エンティティクラス
 * 
 * 不動産取引に関わる顧客の基本情報を管理するエンティティです。
 * 顧客の氏名、メールアドレス、電話番号、住所、顧客タイプなどの情報を保持します。
 * 
 * @author システム開発チーム
 * @version 1.0.0
 * @since 2025-08-11
 */
@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    /**
     * 顧客ID（主キー）
     * データベースで自動生成される一意の識別子
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 顧客の姓
     * 顧客の姓（例：田中）
     */
    @Column(nullable = false)
    private String firstName;

    /**
     * 顧客の名
     * 顧客の名（例：太郎）
     */
    @Column(nullable = false)
    private String lastName;

    /**
     * メールアドレス
     * 顧客の連絡用メールアドレス（一意制約あり）
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * 電話番号
     * 顧客の連絡用電話番号
     */
    @Column(nullable = false)
    private String phone;

    /**
     * 住所
     * 顧客の住所（最大500文字）
     */
    @Column(length = 500)
    private String address;

    /**
     * 顧客タイプ
     * 顧客の種類（買主、売主、賃借人、貸主など）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientType type;

    /**
     * 顧客ステータス
     * 顧客の現在の状態（アクティブ、非アクティブ、保留中など）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientStatus status;

    /**
     * 作成日時
     * レコードが作成された日時
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     * レコードが最後に更新された日時
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * エンティティ保存前の処理
     * 作成日時と更新日時を現在時刻に設定
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * エンティティ更新前の処理
     * 更新日時を現在時刻に設定
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 顧客タイプの列挙型
     * 顧客の種類を定義
     */
    public enum ClientType {
        /** 買主 */
        BUYER, 
        /** 売主 */
        SELLER, 
        /** 賃借人 */
        TENANT, 
        /** 貸主 */
        LANDLORD
    }

    /**
     * 顧客ステータスの列挙型
     * 顧客の現在の状態を定義
     */
    public enum ClientStatus {
        /** アクティブ */
        ACTIVE, 
        /** 非アクティブ */
        INACTIVE, 
        /** 保留中 */
        PENDING
    }
}
