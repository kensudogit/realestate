package com.realestate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 取引エンティティクラス
 * 
 * 不動産取引に関連する金銭的な取引情報を管理するエンティティです。
 * 支払い、返金、手数料、メンテナンス費、保険料、税金などの取引を記録します。
 * 
 * @author システム開発チーム
 * @version 1.0.0
 * @since 2025-08-11
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    /**
     * 取引ID（主キー）
     * データベースで自動生成される一意の識別子
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 関連契約
     * この取引に関連する契約（多対一の関連）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    /**
     * 取引タイプ
     * 取引の種類（支払い、返金、手数料、メンテナンス費、保険料、税金など）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    /**
     * 取引金額
     * 取引の金額（円単位）
     */
    @Column(nullable = false)
    private BigDecimal amount;

    /**
     * 取引日時
     * 取引が発生した日時
     */
    @Column(nullable = false)
    private LocalDateTime transactionDate;

    /**
     * 取引説明
     * 取引の詳細な説明（最大500文字）
     */
    @Column(length = 500)
    private String description;

    /**
     * 取引ステータス
     * 取引の現在の状態（保留、完了、失敗、キャンセルなど）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

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
     * 取引タイプの列挙型
     * 取引の種類を定義
     */
    public enum TransactionType {
        /** 支払い */
        PAYMENT, 
        /** 返金 */
        REFUND, 
        /** 手数料 */
        COMMISSION, 
        /** メンテナンス費 */
        MAINTENANCE, 
        /** 保険料 */
        INSURANCE, 
        /** 税金 */
        TAX
    }

    /**
     * 取引ステータスの列挙型
     * 取引の現在の状態を定義
     */
    public enum TransactionStatus {
        /** 保留 */
        PENDING, 
        /** 完了 */
        COMPLETED, 
        /** 失敗 */
        FAILED, 
        /** キャンセル */
        CANCELLED
    }
}
