package com.realestate.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物件エンティティクラス
 * 
 * 不動産物件の基本情報を管理するエンティティです。
 * 物件の名前、住所、説明、タイプ、ステータス、価格、面積、部屋数などの情報を保持します。
 * 
 * @author システム開発チーム
 * @version 1.0.0
 * @since 2025-08-11
 */
@Entity
@Table(name = "properties")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Property {

    /**
     * 物件ID（主キー）
     * データベースで自動生成される一意の識別子
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 物件名
     * 物件を識別するための名称（例：青山マンション101号室）
     */
    @Column(nullable = false)
    private String name;

    /**
     * 物件住所
     * 物件の所在地（例：東京都港区青山1-1-1）
     */
    @Column(nullable = false)
    private String address;

    /**
     * 物件説明
     * 物件の詳細な説明や特徴（最大1000文字）
     */
    @Column(length = 1000)
    private String description;

    /**
     * 物件タイプ
     * 物件の種類（マンション、一戸建て、商業ビル、土地、オフィス、倉庫など）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyType type;

    /**
     * 物件ステータス
     * 物件の現在の状態（空室、売却済み、賃貸中、契約中、メンテナンス中など）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyStatus status;

    /**
     * 物件価格
     * 物件の価格（円単位）
     */
    @Column(nullable = false)
    private BigDecimal price;

    /**
     * 物件面積
     * 物件の面積（平方メートル単位）
     */
    @Column(nullable = false)
    private BigDecimal area;

    /**
     * 部屋数
     * 物件の部屋の数
     */
    @Column(nullable = false)
    private Integer rooms;

    /**
     * 浴室数
     * 物件の浴室の数
     */
    @Column(nullable = false)
    private Integer bathrooms;

    /**
     * 駐車場スペース数
     * 物件に付属する駐車場のスペース数
     */
    @Column(nullable = false)
    private Integer parkingSpaces;

    /**
     * 築年数
     * 物件が建築された年
     */
    @Column(nullable = false)
    private Integer yearBuilt;

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
     * 物件タイプの列挙型
     * 物件の種類を定義
     */
    public enum PropertyType {
        /** マンション */
        APARTMENT, 
        /** 一戸建て */
        HOUSE, 
        /** 商業ビル */
        COMMERCIAL, 
        /** 土地 */
        LAND, 
        /** オフィス */
        OFFICE, 
        /** 倉庫 */
        WAREHOUSE
    }

    /**
     * 物件ステータスの列挙型
     * 物件の現在の状態を定義
     */
    public enum PropertyStatus {
        /** 空室・売却可能 */
        AVAILABLE, 
        /** 売却済み */
        SOLD, 
        /** 賃貸中 */
        RENTED, 
        /** 契約中 */
        UNDER_CONTRACT, 
        /** メンテナンス中 */
        MAINTENANCE
    }
}
