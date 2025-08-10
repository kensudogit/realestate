package com.realestate.service;

import com.realestate.dto.TransactionDto;
import com.realestate.entity.Transaction;
import com.realestate.entity.Transaction.TransactionType;
import com.realestate.entity.Transaction.TransactionStatus;
import com.realestate.entity.Contract;
import com.realestate.repository.TransactionRepository;
import com.realestate.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 取引管理サービスクラス
 * 取引の作成、更新、削除、検索などの業務ロジックを提供します
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    // 取引リポジトリ（データアクセス層）
    private final TransactionRepository transactionRepository;
    // 契約リポジトリ（契約情報の取得用）
    private final ContractRepository contractRepository;

    /**
     * 全ての取引を取得
     * @return 取引DTOのリスト
     */
    public List<TransactionDto> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * IDによる取引の取得
     * @param id 取引ID
     * @return 取引DTO（存在しない場合は空）
     */
    public Optional<TransactionDto> getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * 契約IDによる取引の検索
     * @param contractId 契約ID
     * @return 該当する取引DTOのリスト
     */
    public List<TransactionDto> getTransactionsByContractId(Long contractId) {
        return transactionRepository.findByContract_Id(contractId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 取引タイプによる取引の検索
     * @param type 取引タイプ（入金、出金、手数料など）
     * @return 該当する取引DTOのリスト
     */
    public List<TransactionDto> getTransactionsByType(TransactionType type) {
        return transactionRepository.findByType(type).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 取引ステータスによる取引の検索
     * @param status 取引ステータス（完了、処理中、エラーなど）
     * @return 該当する取引DTOのリスト
     */
    public List<TransactionDto> getTransactionsByStatus(TransactionStatus status) {
        return transactionRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 新規取引の作成
     * @param transactionDto 取引情報DTO
     * @return 作成された取引DTO
     */
    public TransactionDto createTransaction(TransactionDto transactionDto) {
        Transaction transaction = convertToEntity(transactionDto);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        Transaction savedTransaction = transactionRepository.save(transaction);
        return convertToDto(savedTransaction);
    }

    /**
     * 取引情報の更新
     * @param id 更新対象の取引ID
     * @param transactionDto 更新する取引情報DTO
     * @return 更新された取引DTO（存在しない場合は空）
     */
    public Optional<TransactionDto> updateTransaction(Long id, TransactionDto transactionDto) {
        return transactionRepository.findById(id)
                .map(existingTransaction -> {
                    // 契約オブジェクトの取得（IDから契約エンティティを取得）
                    if (transactionDto.getContractId() != null) {
                        Optional<Contract> contract = contractRepository.findById(transactionDto.getContractId());
                        contract.ifPresent(existingTransaction::setContract);
                    }
                    // 取引基本情報の更新
                    existingTransaction.setType(transactionDto.getType());
                    existingTransaction.setAmount(transactionDto.getAmount());
                    existingTransaction.setTransactionDate(transactionDto.getTransactionDate());
                    existingTransaction.setDescription(transactionDto.getDescription());
                    existingTransaction.setStatus(transactionDto.getStatus());
                    existingTransaction.setUpdatedAt(LocalDateTime.now());
                    Transaction savedTransaction = transactionRepository.save(existingTransaction);
                    return convertToDto(savedTransaction);
                });
    }

    /**
     * 取引の削除
     * @param id 削除対象の取引ID
     * @return 削除成功時true、存在しない場合はfalse
     */
    public boolean deleteTransaction(Long id) {
        if (transactionRepository.existsById(id)) {
            transactionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 取引エンティティをDTOに変換
     * @param transaction 取引エンティティ
     * @return 取引DTO
     */
    private TransactionDto convertToDto(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        // 契約IDの設定（契約エンティティからIDを取得）
        dto.setContractId(transaction.getContract() != null ? transaction.getContract().getId() : null);
        dto.setType(transaction.getType());
        dto.setAmount(transaction.getAmount());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setDescription(transaction.getDescription());
        dto.setStatus(transaction.getStatus());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setUpdatedAt(transaction.getUpdatedAt());
        return dto;
    }

    /**
     * 取引DTOをエンティティに変換
     * @param dto 取引DTO
     * @return 取引エンティティ
     */
    private Transaction convertToEntity(TransactionDto dto) {
        Transaction transaction = new Transaction();
        transaction.setId(dto.getId());
        
        // 契約オブジェクトの設定（IDから契約エンティティを取得）
        if (dto.getContractId() != null) {
            Optional<Contract> contract = contractRepository.findById(dto.getContractId());
            contract.ifPresent(transaction::setContract);
        }
        
        // 取引基本情報の設定
        transaction.setType(dto.getType());
        transaction.setAmount(dto.getAmount());
        transaction.setTransactionDate(dto.getTransactionDate());
        transaction.setDescription(dto.getDescription());
        transaction.setStatus(dto.getStatus());
        transaction.setCreatedAt(dto.getCreatedAt());
        transaction.setUpdatedAt(dto.getUpdatedAt());
        return transaction;
    }
}
