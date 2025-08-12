package com.realestate.service;

import com.realestate.dto.PropertyDto;
import com.realestate.entity.Property;
import com.realestate.entity.Property.PropertyType;
import com.realestate.entity.Property.PropertyStatus;
import com.realestate.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 物件管理サービスクラス
 * 物件の作成、更新、削除、検索などの業務ロジックを提供します
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PropertyService {
    
    private static final Logger log = LoggerFactory.getLogger(PropertyService.class);
    
    // 物件リポジトリ（データアクセス層）
    private final PropertyRepository propertyRepository;
    
    /**
     * 全ての物件を取得
     * @return 物件DTOのリスト
     */
    public List<PropertyDto> getAllProperties() {
        try {
            List<Property> properties = propertyRepository.findAll();
            return properties.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("物件データの取得に失敗しました", e);
            // エラー時は空のリストを返す
            return new ArrayList<>();
        }
    }
    
    /**
     * IDによる物件の取得
     * @param id 物件ID
     * @return 物件DTO（存在しない場合は空）
     */
    public Optional<PropertyDto> getPropertyById(Long id) {
        return propertyRepository.findById(id)
            .map(this::convertToDto);
    }
    
    /**
     * 物件タイプによる物件の検索
     * @param type 物件タイプ（マンション、一戸建て、土地など）
     * @return 該当する物件DTOのリスト
     */
    public List<PropertyDto> getPropertiesByType(PropertyType type) {
        return propertyRepository.findByType(type).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 物件ステータスによる物件の検索
     * @param status 物件ステータス（販売中、売約済み、賃貸中など）
     * @return 該当する物件DTOのリスト
     */
    public List<PropertyDto> getPropertiesByStatus(PropertyStatus status) {
        return propertyRepository.findByStatus(status).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * キーワードによる物件の検索
     * @param query 検索キーワード
     * @return 該当する物件DTOのリスト
     */
    public List<PropertyDto> searchProperties(String query) {
        try {
            List<Property> properties = propertyRepository.findByNameContainingIgnoreCase(query);
            properties.addAll(propertyRepository.findByAddressContainingIgnoreCase(query));
            return properties.stream()
                .distinct()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("物件検索に失敗しました: {}", query, e);
            // エラー時は空のリストを返す
            return new ArrayList<>();
        }
    }
    
    /**
     * 複数条件による物件の検索
     * @param type 物件タイプ
     * @param status 物件ステータス
     * @param minPrice 最低価格
     * @param maxPrice 最高価格
     * @param minArea 最小面積
     * @param maxArea 最大面積
     * @return 該当する物件DTOのリスト
     */
    public List<PropertyDto> getPropertiesByCriteria(
            PropertyType type, PropertyStatus status, 
            BigDecimal minPrice, BigDecimal maxPrice,
            BigDecimal minArea, BigDecimal maxArea) {
        return propertyRepository.findPropertiesByCriteria(type, status, minPrice, maxPrice, minArea, maxArea)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 新規物件の作成
     * @param propertyDto 物件情報DTO
     * @return 作成された物件DTO
     */
    public PropertyDto createProperty(PropertyDto propertyDto) {
        Property property = convertToEntity(propertyDto);
        Property savedProperty = propertyRepository.save(property);
        return convertToDto(savedProperty);
    }
    
    /**
     * 物件情報の更新
     * @param id 更新対象の物件ID
     * @param propertyDto 更新する物件情報DTO
     * @return 更新された物件DTO（存在しない場合は空）
     */
    public Optional<PropertyDto> updateProperty(Long id, PropertyDto propertyDto) {
        return propertyRepository.findById(id)
            .map(existingProperty -> {
                updatePropertyFields(existingProperty, propertyDto);
                Property savedProperty = propertyRepository.save(existingProperty);
                return convertToDto(savedProperty);
            });
    }
    
    /**
     * 物件の削除
     * @param id 削除対象の物件ID
     * @return 削除成功時true、存在しない場合はfalse
     */
    public boolean deleteProperty(Long id) {
        if (propertyRepository.existsById(id)) {
            propertyRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    /**
     * 物件DTOをエンティティに変換
     * @param dto 物件DTO
     * @return 物件エンティティ
     */
    private Property convertToEntity(PropertyDto dto) {
        Property property = new Property();
        property.setName(dto.getName());
        property.setAddress(dto.getAddress());
        property.setDescription(dto.getDescription());
        property.setType(dto.getType());
        property.setStatus(dto.getStatus());
        property.setPrice(dto.getPrice());
        property.setArea(dto.getArea());
        property.setRooms(dto.getRooms());
        property.setBathrooms(dto.getBathrooms());
        property.setParkingSpaces(dto.getParkingSpaces());
        property.setYearBuilt(dto.getYearBuilt());
        return property;
    }
    
    /**
     * 物件エンティティをDTOに変換
     * @param property 物件エンティティ
     * @return 物件DTO
     */
    private PropertyDto convertToDto(Property property) {
        return new PropertyDto(
            property.getId(),
            property.getName(),
            property.getAddress(),
            property.getDescription(),
            property.getType(),
            property.getStatus(),
            property.getPrice(),
            property.getArea(),
            property.getRooms(),
            property.getBathrooms(),
            property.getParkingSpaces(),
            property.getYearBuilt(),
            property.getCreatedAt(),
            property.getUpdatedAt()
        );
    }
    
    /**
     * 物件フィールドの更新
     * @param existingProperty 既存の物件エンティティ
     * @param dto 更新する物件情報DTO
     */
    private void updatePropertyFields(Property existingProperty, PropertyDto dto) {
        if (dto.getName() != null) existingProperty.setName(dto.getName());
        if (dto.getAddress() != null) existingProperty.setAddress(dto.getAddress());
        if (dto.getDescription() != null) existingProperty.setDescription(dto.getDescription());
        if (dto.getType() != null) existingProperty.setType(dto.getType());
        if (dto.getStatus() != null) existingProperty.setStatus(dto.getStatus());
        if (dto.getPrice() != null) existingProperty.setPrice(dto.getPrice());
        if (dto.getArea() != null) existingProperty.setArea(dto.getArea());
        if (dto.getRooms() != null) existingProperty.setRooms(dto.getRooms());
        if (dto.getBathrooms() != null) existingProperty.setBathrooms(dto.getBathrooms());
        if (dto.getParkingSpaces() != null) existingProperty.setParkingSpaces(dto.getParkingSpaces());
        if (dto.getYearBuilt() != null) existingProperty.setYearBuilt(dto.getYearBuilt());
    }
}
