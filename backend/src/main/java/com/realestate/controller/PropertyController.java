package com.realestate.controller;

import com.realestate.dto.PropertyDto;
import com.realestate.entity.Property.PropertyType;
import com.realestate.entity.Property.PropertyStatus;
import com.realestate.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 物件管理コントローラークラス
 * 物件に関するREST APIエンドポイントを提供します
 */
@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PropertyController {

    // 物件管理サービス（業務ロジック層）
    private final PropertyService propertyService;

    /**
     * 全ての物件を取得するAPI
     * @return 物件DTOのリスト
     */
    @GetMapping
    public ResponseEntity<List<PropertyDto>> getAllProperties() {
        return ResponseEntity.ok(propertyService.getAllProperties());
    }

    /**
     * IDによる物件の取得API
     * @param id 物件ID
     * @return 物件DTO（存在しない場合は404エラー）
     */
    @GetMapping("/{id}")
    public ResponseEntity<PropertyDto> getPropertyById(@PathVariable Long id) {
        return propertyService.getPropertyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 物件タイプによる物件の検索API
     * @param type 物件タイプ（マンション、一戸建て、土地など）
     * @return 該当する物件DTOのリスト
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<PropertyDto>> getPropertiesByType(@PathVariable PropertyType type) {
        return ResponseEntity.ok(propertyService.getPropertiesByType(type));
    }

    /**
     * 物件ステータスによる物件の検索API
     * @param status 物件ステータス（販売中、売約済み、賃貸中など）
     * @return 該当する物件DTOのリスト
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PropertyDto>> getPropertiesByStatus(@PathVariable PropertyStatus status) {
        return ResponseEntity.ok(propertyService.getPropertiesByStatus(status));
    }

    /**
     * キーワードによる物件の検索API
     * @param query 検索キーワード
     * @return 該当する物件DTOのリスト
     */
    @GetMapping("/search")
    public ResponseEntity<List<PropertyDto>> searchProperties(@RequestParam String query) {
        return ResponseEntity.ok(propertyService.searchProperties(query));
    }

    /**
     * 複数条件による物件の検索API
     * @param type 物件タイプ（オプション）
     * @param status 物件ステータス（オプション）
     * @param minPrice 最低価格（オプション）
     * @param maxPrice 最高価格（オプション）
     * @param minArea 最小面積（オプション）
     * @param maxArea 最大面積（オプション）
     * @return 該当する物件DTOのリスト
     */
    @GetMapping("/search/criteria")
    public ResponseEntity<List<PropertyDto>> searchPropertiesByCriteria(
            @RequestParam(required = false) PropertyType type,
            @RequestParam(required = false) PropertyStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minArea,
            @RequestParam(required = false) BigDecimal maxArea) {
        return ResponseEntity.ok(propertyService.getPropertiesByCriteria(
                type, status, minPrice, maxPrice, minArea, maxArea));
    }

    /**
     * 新規物件の作成API
     * @param propertyDto 作成する物件情報DTO
     * @return 作成された物件DTO
     */
    @PostMapping
    public ResponseEntity<PropertyDto> createProperty(@RequestBody PropertyDto propertyDto) {
        PropertyDto createdProperty = propertyService.createProperty(propertyDto);
        return ResponseEntity.ok(createdProperty);
    }

    /**
     * 物件情報の更新API
     * @param id 更新対象の物件ID
     * @param propertyDto 更新する物件情報DTO
     * @return 更新された物件DTO（存在しない場合は404エラー）
     */
    @PutMapping("/{id}")
    public ResponseEntity<PropertyDto> updateProperty(
            @PathVariable Long id,
            @RequestBody PropertyDto propertyDto) {
        return propertyService.updateProperty(id, propertyDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 物件の削除API
     * @param id 削除対象の物件ID
     * @return 削除成功時200、存在しない場合は404エラー
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        if (propertyService.deleteProperty(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
