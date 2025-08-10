package com.realestate.repository;

import com.realestate.entity.Contract;
import com.realestate.entity.Contract.ContractType;
import com.realestate.entity.Contract.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findByType(ContractType type);

    List<Contract> findByStatus(ContractStatus status);

    List<Contract> findByPropertyId(Long propertyId);

    List<Contract> findByClientId(Long clientId);

    @Query("SELECT c FROM Contract c WHERE c.endDate <= :date AND c.status = 'ACTIVE'")
    List<Contract> findExpiringContracts(@Param("date") LocalDateTime date);

    @Query("SELECT c FROM Contract c WHERE c.startDate >= :startDate AND c.startDate <= :endDate")
    List<Contract> findContractsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
