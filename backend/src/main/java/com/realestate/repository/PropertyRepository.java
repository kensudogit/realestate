package com.realestate.repository;

import com.realestate.entity.Property;
import com.realestate.entity.Property.PropertyType;
import com.realestate.entity.Property.PropertyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByType(PropertyType type);

    List<Property> findByStatus(PropertyStatus status);

    List<Property> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<Property> findByAreaBetween(BigDecimal minArea, BigDecimal maxArea);

    List<Property> findByRoomsGreaterThanEqual(Integer rooms);

    List<Property> findByAddressContainingIgnoreCase(String address);

    List<Property> findByNameContainingIgnoreCase(String name);

    @Query("SELECT p FROM Property p WHERE " +
            "(:type IS NULL OR p.type = :type) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:minArea IS NULL OR p.area >= :minArea) AND " +
            "(:maxArea IS NULL OR p.area <= :maxArea)")
    List<Property> findPropertiesByCriteria(
            @Param("type") PropertyType type,
            @Param("status") PropertyStatus status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minArea") BigDecimal minArea,
            @Param("maxArea") BigDecimal maxArea);
}
