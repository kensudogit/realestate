package com.realestate.service;

import com.realestate.dto.ClientDto;
import com.realestate.entity.Client;
import com.realestate.entity.Client.ClientType;
import com.realestate.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * クライアント管理サービスクラス
 * クライアントの作成、更新、削除、検索などの業務ロジックを提供します
 */
@Service
@RequiredArgsConstructor
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    // クライアントリポジトリ（データアクセス層）
    private final ClientRepository clientRepository;

    /**
     * 全てのクライアントを取得
     * @return クライアントDTOのリスト
     */
    public List<ClientDto> getAllClients() {
        try {
            List<Client> clients = clientRepository.findAll();
            return clients.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("クライアントデータの取得に失敗しました", e);
            // エラー時は空のリストを返す
            return new ArrayList<>();
        }
    }

    /**
     * IDによるクライアントの取得
     * @param id クライアントID
     * @return クライアントDTO（存在しない場合は空）
     */
    public Optional<ClientDto> getClientById(Long id) {
        return clientRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * クライアントタイプによるクライアントの検索
     * @param type クライアントタイプ（個人、法人など）
     * @return 該当するクライアントDTOのリスト
     */
    public List<ClientDto> getClientsByType(ClientType type) {
        return clientRepository.findByType(type).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * クエリによるクライアントの検索
     * @param query 検索クエリ（名前、メール、住所など）
     * @return 該当するクライアントDTOのリスト
     */
    public List<ClientDto> searchClients(String query) {
        try {
            String lowerQuery = query.toLowerCase();
            List<Client> allClients = clientRepository.findAll();
            return allClients.stream()
                    .filter(client -> 
                        client.getFirstName().toLowerCase().contains(lowerQuery) ||
                        client.getLastName().toLowerCase().contains(lowerQuery) ||
                        client.getEmail().toLowerCase().contains(lowerQuery) ||
                        (client.getAddress() != null && client.getAddress().toLowerCase().contains(lowerQuery))
                    )
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("クライアント検索に失敗しました: {}", query, e);
            // エラー時は空のリストを返す
            return new ArrayList<>();
        }
    }

    /**
     * 高度な検索・フィルタリング
     * @param name 顧客名（部分一致）
     * @param email メールアドレス（部分一致）
     * @param type 顧客タイプ
     * @param status ステータス
     * @return 該当するクライアントDTOのリスト
     */
    public List<ClientDto> advancedSearch(String name, String email, ClientType type, String status) {
        try {
            List<Client> allClients = clientRepository.findAll();
            return allClients.stream()
                    .filter(client -> {
                        // 名前フィルタリング
                        if (name != null && !name.trim().isEmpty()) {
                            String fullName = (client.getFirstName() + " " + client.getLastName()).toLowerCase();
                            if (!fullName.contains(name.toLowerCase())) {
                                return false;
                            }
                        }
                        
                        // メールフィルタリング
                        if (email != null && !email.trim().isEmpty()) {
                            if (!client.getEmail().toLowerCase().contains(email.toLowerCase())) {
                                return false;
                            }
                        }
                        
                        // タイプフィルタリング
                        if (type != null && client.getType() != type) {
                            return false;
                        }
                        
                        // ステータスフィルタリング（現在は実装されていないため、常にtrue）
                        // TODO: ステータスフィールドが実装されたら有効化
                        
                        return true;
                    })
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("高度なクライアント検索に失敗しました", e);
            // エラー時は空のリストを返す
            return new ArrayList<>();
        }
    }

    /**
     * 新規クライアントの作成
     * @param clientDto クライアント情報DTO
     * @return 作成されたクライアントDTO
     */
    public ClientDto createClient(ClientDto clientDto) {
        Client client = convertToEntity(clientDto);
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());
        Client savedClient = clientRepository.save(client);
        return convertToDto(savedClient);
    }

    /**
     * クライアント情報の更新
     * @param id 更新対象のクライアントID
     * @param clientDto 更新するクライアント情報DTO
     * @return 更新されたクライアントDTO（存在しない場合は空）
     */
    public Optional<ClientDto> updateClient(Long id, ClientDto clientDto) {
        return clientRepository.findById(id)
                .map(existingClient -> {
                    // クライアント基本情報の更新
                    existingClient.setFirstName(clientDto.getFirstName());
                    existingClient.setLastName(clientDto.getLastName());
                    existingClient.setEmail(clientDto.getEmail());
                    existingClient.setPhone(clientDto.getPhone());
                    existingClient.setAddress(clientDto.getAddress());
                    existingClient.setType(clientDto.getType());
                    existingClient.setUpdatedAt(LocalDateTime.now());
                    Client savedClient = clientRepository.save(existingClient);
                    return convertToDto(savedClient);
                });
    }

    /**
     * クライアントの削除
     * @param id 削除対象のクライアントID
     * @return 削除成功時true、存在しない場合はfalse
     */
    public boolean deleteClient(Long id) {
        if (clientRepository.existsById(id)) {
            clientRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * クライアントエンティティをDTOに変換
     * @param client クライアントエンティティ
     * @return クライアントDTO
     */
    private ClientDto convertToDto(Client client) {
        ClientDto dto = new ClientDto();
        dto.setId(client.getId());
        dto.setFirstName(client.getFirstName());
        dto.setLastName(client.getLastName());
        dto.setEmail(client.getEmail());
        dto.setPhone(client.getPhone());
        dto.setAddress(client.getAddress());
        dto.setType(client.getType());
        dto.setCreatedAt(client.getCreatedAt());
        dto.setUpdatedAt(client.getUpdatedAt());
        return dto;
    }

    /**
     * クライアントDTOをエンティティに変換
     * @param dto クライアントDTO
     * @return クライアントエンティティ
     */
    private Client convertToEntity(ClientDto dto) {
        Client client = new Client();
        client.setId(dto.getId());
        client.setFirstName(dto.getFirstName());
        client.setLastName(dto.getLastName());
        client.setEmail(dto.getEmail());
        client.setPhone(dto.getPhone());
        client.setAddress(dto.getAddress());
        client.setType(dto.getType());
        client.setCreatedAt(dto.getCreatedAt());
        client.setUpdatedAt(dto.getUpdatedAt());
        return client;
    }
}
