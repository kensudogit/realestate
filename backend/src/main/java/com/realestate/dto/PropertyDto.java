package com.realestate.dto;

import com.realestate.entity.Property.PropertyType;
import com.realestate.entity.Property.PropertyStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物件データ転送オブジェクト（DTO）
 * 
 * フロントエンドとバックエンド間で物件情報をやり取りするためのデータクラスです。
 * エンティティクラスから不要な情報を除外し、必要な情報のみを公開します。
 * 
 * @author システム開発チーム
 * @version 1.0.0
 * @since 2025-08-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDto {
    
    /**
     * 物件ID
     */
    private Long id;
    
    /**
     * 物件名
     */
    private String name;
    
    /**
     * 物件住所
     */
    private String address;
    
    /**
     * 物件説明
     */
    private String description;
    
    /**
     * 物件タイプ
     */
    private PropertyType type;
    
    /**
     * 物件ステータス
     */
    private PropertyStatus status;
    
    /**
     * 物件価格
     */
    private BigDecimal price;
    
    /**
     * 物件面積
     */
    private BigDecimal area;
    
    /**
     * 部屋数
     */
    private Integer rooms;
    
    /**
     * 浴室数
     */
    private Integer bathrooms;
    
    /**
     * 駐車場スペース数
     */
    private Integer parkingSpaces;
    
    /**
     * 築年数
     */
    private Integer yearBuilt;
    
    /**
     * 作成日時
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新日時
     */
    private LocalDateTime updatedAt;
}
