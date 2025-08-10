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

/**
 * クライアント管理サービスクラス
 * クライアントの作成、更新、削除、検索などの業務ロジックを提供します
 */
@Service
@RequiredArgsConstructor
public class ClientService {

    // クライアントリポジトリ（データアクセス層）
    private final ClientRepository clientRepository;

    /**
     * 全てのクライアントを取得
     * @return クライアントDTOのリスト
     */
    public List<ClientDto> getAllClients() {
        return clientRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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
