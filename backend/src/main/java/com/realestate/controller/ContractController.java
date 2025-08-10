package com.realestate.controller;

import com.realestate.dto.ContractDto;
import com.realestate.entity.Contract.ContractType;
import com.realestate.entity.Contract.ContractStatus;
import com.realestate.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 契約管理コントローラークラス
 * 契約に関するREST APIエンドポイントを提供します
 */
@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContractController {

    // 契約管理サービス（業務ロジック層）
    private final ContractService contractService;

    /**
     * 全ての契約を取得するAPI
     * @return 契約DTOのリスト
     */
    @GetMapping
    public ResponseEntity<List<ContractDto>> getAllContracts() {
        return ResponseEntity.ok(contractService.getAllContracts());
    }

    /**
     * IDによる契約の取得API
     * @param id 契約ID
     * @return 契約DTO（存在しない場合は404エラー）
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContractDto> getContractById(@PathVariable Long id) {
        return contractService.getContractById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 契約タイプによる契約の検索API
     * @param type 契約タイプ（売買、賃貸など）
     * @return 該当する契約DTOのリスト
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ContractDto>> getContractsByType(@PathVariable ContractType type) {
        return ResponseEntity.ok(contractService.getContractsByType(type));
    }

    /**
     * 契約ステータスによる契約の検索API
     * @param status 契約ステータス（進行中、完了、キャンセルなど）
     * @return 該当する契約DTOのリスト
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ContractDto>> getContractsByStatus(@PathVariable ContractStatus status) {
        return ResponseEntity.ok(contractService.getContractsByStatus(status));
    }

    /**
     * 新規契約の作成API
     * @param contractDto 作成する契約情報DTO
     * @return 作成された契約DTO
     */
    @PostMapping
    public ResponseEntity<ContractDto> createContract(@RequestBody ContractDto contractDto) {
        ContractDto createdContract = contractService.createContract(contractDto);
        return ResponseEntity.ok(createdContract);
    }

    /**
     * 契約情報の更新API
     * @param id 更新対象の契約ID
     * @param contractDto 更新する契約情報DTO
     * @return 更新された契約DTO（存在しない場合は404エラー）
     */
    @PutMapping("/{id}")
    public ResponseEntity<ContractDto> updateContract(
            @PathVariable Long id,
            @RequestBody ContractDto contractDto) {
        return contractService.updateContract(id, contractDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 契約の削除API
     * @param id 削除対象の契約ID
     * @return 削除成功時200、存在しない場合は404エラー
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContract(@PathVariable Long id) {
        if (contractService.deleteContract(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
