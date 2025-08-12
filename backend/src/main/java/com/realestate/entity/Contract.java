package com.realestate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 契約エンティティクラス
 * 
 * 不動産取引の契約情報を管理するエンティティです。
 * 契約番号、物件、顧客、契約タイプ、ステータス、金額、期間、条件などの情報を保持します。
 * 
 * @author システム開発チーム
 * @version 1.0.0
 * @since 2025-08-11
 */
@Entity
@Table(name = "contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contract {

    /**
     * 契約ID（主キー）
     * データベースで自動生成される一意の識別子
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 契約番号
     * 契約を識別するための一意の番号
     */
    @Column(unique = true, nullable = false)
    private String contractNumber;

    /**
     * 物件情報
     * 契約対象の物件（多対一の関連）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    /**
     * 顧客情報
     * 契約の当事者となる顧客（多対一の関連）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    /**
     * 契約タイプ
     * 契約の種類（売買、賃貸、リース、管理など）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractType type;

    /**
     * 契約ステータス
     * 契約の現在の状態（下書き、有効、期限切れ、終了、保留など）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status;

    /**
     * 契約金額
     * 契約の総額（円単位）
     */
    @Column(nullable = false)
    private BigDecimal amount;

    /**
     * 契約開始日
     * 契約の開始日時
     */
    @Column(nullable = false)
    private LocalDateTime startDate;

    /**
     * 契約終了日
     * 契約の終了日時
     */
    @Column(nullable = false)
    private LocalDateTime endDate;

    /**
     * 契約条件
     * 契約の詳細な条件や条項（最大1000文字）
     */
    @Column(length = 1000)
    private String terms;

    /**
     * 月額賃料
     * 賃貸契約の場合の月額賃料（円単位）
     */
    @Column
    private BigDecimal monthlyRent;

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
     * 契約タイプの列挙型
     * 契約の種類を定義
     */
    public enum ContractType {
        /** 売買契約 */
        SALE, 
        /** 賃貸契約 */
        RENTAL, 
        /** リース契約 */
        LEASE, 
        /** 管理契約 */
        MANAGEMENT
    }

    /**
     * 契約ステータスの列挙型
     * 契約の現在の状態を定義
     */
    public enum ContractStatus {
        /** 下書き */
        DRAFT, 
        /** 有効 */
        ACTIVE, 
        /** 期限切れ */
        EXPIRED, 
        /** 終了 */
        TERMINATED, 
        /** 保留 */
        PENDING
    }
}
