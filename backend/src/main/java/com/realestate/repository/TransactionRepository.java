package com.realestate.repository;

import com.realestate.entity.Transaction;
import com.realestate.entity.Transaction.TransactionType;
import com.realestate.entity.Transaction.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

        List<Transaction> findByType(TransactionType type);

        List<Transaction> findByStatus(TransactionStatus status);

        List<Transaction> findByAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

        List<Transaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

        List<Transaction> findByContract_Id(Long contractId);

        @Query("SELECT t FROM Transaction t WHERE " +
                        "(:type IS NULL OR t.type = :type) AND " +
                        "(:status IS NULL OR t.status = :status) AND " +
                        "(:minAmount IS NULL OR t.amount >= :minAmount) AND " +
                        "(:maxAmount IS NULL OR t.amount <= :maxAmount)")
        List<Transaction> findTransactionsByCriteria(
                        @Param("type") TransactionType type,
                        @Param("status") TransactionStatus status,
                        @Param("minAmount") BigDecimal minAmount,
                        @Param("maxAmount") BigDecimal maxAmount);
}
