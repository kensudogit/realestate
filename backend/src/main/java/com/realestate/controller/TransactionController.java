package com.realestate.controller;

import com.realestate.dto.TransactionDto;
import com.realestate.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 取引管理コントローラークラス
 * 取引に関するREST APIエンドポイントを提供します
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransactionController {

    // 取引管理サービス（業務ロジック層）
    private final TransactionService transactionService;

    /**
     * 全ての取引を取得するAPI
     * @return 取引DTOのリスト
     */
    @GetMapping
    public ResponseEntity<List<TransactionDto>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    /**
     * IDによる取引の取得API
     * @param id 取引ID
     * @return 取引DTO（存在しない場合は404エラー）
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 新規取引の作成API
     * @param transactionDto 作成する取引情報DTO
     * @return 作成された取引DTO
     */
    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(@RequestBody TransactionDto transactionDto) {
        TransactionDto createdTransaction = transactionService.createTransaction(transactionDto);
        return ResponseEntity.ok(createdTransaction);
    }

    /**
     * 取引情報の更新API
     * @param id 更新対象の取引ID
     * @param transactionDto 更新する取引情報DTO
     * @return 更新された取引DTO（存在しない場合は404エラー）
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionDto> updateTransaction(
            @PathVariable Long id,
            @RequestBody TransactionDto transactionDto) {
        return transactionService.updateTransaction(id, transactionDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 取引の削除API
     * @param id 削除対象の取引ID
     * @return 削除成功時200、存在しない場合は404エラー
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        if (transactionService.deleteTransaction(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
