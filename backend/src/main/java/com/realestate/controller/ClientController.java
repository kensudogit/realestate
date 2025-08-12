package com.realestate.controller;

import com.realestate.dto.ClientDto;
import com.realestate.entity.Client.ClientType;
import com.realestate.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 顧客管理コントローラークラス
 * 
 * 顧客に関するREST APIエンドポイントを提供します。
 * 顧客の作成、取得、更新、削除、検索などの操作を管理します。
 * 
 * @author システム開発チーム
 * @version 1.0.0
 * @since 2025-08-11
 */
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClientController {

    /**
     * 顧客管理サービス（業務ロジック層）
     */
    private final ClientService clientService;

    /**
     * 全ての顧客を取得するAPI
     * 
     * @return 顧客DTOのリスト
     */
    @GetMapping
    public ResponseEntity<List<ClientDto>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    /**
     * IDによる顧客の取得API
     * 
     * @param id 顧客ID
     * @return 顧客DTO（存在しない場合は404エラー）
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getClientById(@PathVariable Long id) {
        return clientService.getClientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 顧客タイプによる顧客の検索API
     * 
     * @param type 顧客タイプ（買主、売主、賃借人、貸主など）
     * @return 該当する顧客DTOのリスト
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<ClientDto>> getClientsByType(@PathVariable ClientType type) {
        return ResponseEntity.ok(clientService.getClientsByType(type));
    }

    /**
     * キーワードによる顧客の検索API
     * 
     * @param query 検索キーワード（名前、メール、住所など）
     * @return 該当する顧客DTOのリスト
     */
    @GetMapping("/search")
    public ResponseEntity<List<ClientDto>> searchClients(@RequestParam String query) {
        return ResponseEntity.ok(clientService.searchClients(query));
    }

    /**
     * 高度な検索・フィルタリングAPI
     * 
     * @param name 顧客名（部分一致）
     * @param email メールアドレス（部分一致）
     * @param type 顧客タイプ
     * @param status ステータス
     * @return 該当する顧客DTOのリスト
     */
    @GetMapping("/search/advanced")
    public ResponseEntity<List<ClientDto>> advancedSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        ClientType clientType = null;
        if (type != null && !type.trim().isEmpty()) {
            try {
                clientType = ClientType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 無効なタイプの場合はnullを返す
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(clientService.advancedSearch(name, email, clientType, status));
    }

    /**
     * 新規顧客の作成API
     * 
     * @param clientDto 作成する顧客の情報
     * @return 作成された顧客DTO
     */
    @PostMapping
    public ResponseEntity<ClientDto> createClient(@RequestBody ClientDto clientDto) {
        ClientDto createdClient = clientService.createClient(clientDto);
        return ResponseEntity.ok(createdClient);
    }

    /**
     * 顧客情報の更新API
     * 
     * @param id 更新対象の顧客ID
     * @param clientDto 更新する顧客の情報
     * @return 更新された顧客DTO（存在しない場合は404エラー）
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClientDto> updateClient(
            @PathVariable Long id,
            @RequestBody ClientDto clientDto) {
        return clientService.updateClient(id, clientDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 顧客の削除API
     * 
     * @param id 削除対象の顧客ID
     * @return 削除成功時は200、存在しない場合は404エラー
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        if (clientService.deleteClient(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
