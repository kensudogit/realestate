package com.realestate.service;

import com.realestate.dto.ContractDto;
import com.realestate.dto.PropertyDto;
import com.realestate.dto.ClientDto;
import com.realestate.entity.Contract;
import com.realestate.entity.Contract.ContractType;
import com.realestate.entity.Contract.ContractStatus;
import com.realestate.entity.Property;
import com.realestate.entity.Client;
import com.realestate.repository.ContractRepository;
import com.realestate.repository.PropertyRepository;
import com.realestate.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 契約管理サービスクラス
 * 契約の作成、更新、削除、検索などの業務ロジックを提供します
 */
@Service
@RequiredArgsConstructor
public class ContractService {

    private static final Logger log = LoggerFactory.getLogger(ContractService.class);

    // 契約リポジトリ（データアクセス層）
    private final ContractRepository contractRepository;
    // 物件リポジトリ（物件情報の取得用）
    private final PropertyRepository propertyRepository;
    // クライアントリポジトリ（クライアント情報の取得用）
    private final ClientRepository clientRepository;

    /**
     * 全ての契約を取得
     * @return 契約DTOのリスト
     */
    public List<ContractDto> getAllContracts() {
        try {
            List<Contract> contracts = contractRepository.findAll();
            return contracts.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("契約データの取得に失敗しました", e);
            // エラー時は空のリストを返す
            return new ArrayList<>();
        }
    }

    /**
     * IDによる契約の取得
     * @param id 契約ID
     * @return 契約DTO（存在しない場合は空）
     */
    public Optional<ContractDto> getContractById(Long id) {
        return contractRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * 契約タイプによる契約の検索
     * @param type 契約タイプ（売買、賃貸など）
     * @return 該当する契約DTOのリスト
     */
    public List<ContractDto> getContractsByType(ContractType type) {
        return contractRepository.findByType(type).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 契約ステータスによる契約の検索
     * @param status 契約ステータス（進行中、完了、キャンセルなど）
     * @return 該当する契約DTOのリスト
     */
    public List<ContractDto> getContractsByStatus(ContractStatus status) {
        return contractRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * クエリによる契約の検索
     * @param query 検索クエリ（契約番号、物件名、顧客名、契約タイプ、ステータスなど）
     * @return 該当する契約DTOのリスト
     */
    public List<ContractDto> searchContracts(String query) {
        try {
            String lowerQuery = query.toLowerCase();
            List<Contract> allContracts = contractRepository.findAll();
            return allContracts.stream()
                    .filter(contract -> {
                        // 契約番号での検索
                        if (contract.getContractNumber() != null && 
                            contract.getContractNumber().toLowerCase().contains(lowerQuery)) {
                            return true;
                        }
                        // 物件名での検索
                        if (contract.getProperty() != null && 
                            contract.getProperty().getName() != null &&
                            contract.getProperty().getName().toLowerCase().contains(lowerQuery)) {
                            return true;
                        }
                        // 顧客名での検索
                        if (contract.getClient() != null) {
                            String clientName = contract.getClient().getFirstName() + " " + contract.getClient().getLastName();
                            if (clientName.toLowerCase().contains(lowerQuery)) {
                                return true;
                            }
                        }
                        // 契約タイプでの検索
                        if (contract.getType() != null && 
                            contract.getType().name().toLowerCase().contains(lowerQuery)) {
                            return true;
                        }
                        // 契約ステータスでの検索
                        if (contract.getStatus() != null && 
                            contract.getStatus().name().toLowerCase().contains(lowerQuery)) {
                            return true;
                        }
                        return false;
                    })
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("契約検索に失敗しました: {}", query, e);
            // エラー時は空のリストを返す
            return new ArrayList<>();
        }
    }

    /**
     * 新規契約の作成
     * @param contractDto 契約情報DTO
     * @return 作成された契約DTO
     */
    public ContractDto createContract(ContractDto contractDto) {
        Contract contract = convertToEntity(contractDto);
        contract.setCreatedAt(LocalDateTime.now());
        contract.setUpdatedAt(LocalDateTime.now());
        Contract savedContract = contractRepository.save(contract);
        return convertToDto(savedContract);
    }

    /**
     * 契約情報の更新
     * @param id 更新対象の契約ID
     * @param contractDto 更新する契約情報DTO
     * @return 更新された契約DTO（存在しない場合は空）
     */
    public Optional<ContractDto> updateContract(Long id, ContractDto contractDto) {
        return contractRepository.findById(id)
                .map(existingContract -> {
                    // 物件情報の設定（IDから物件エンティティを取得）
                    if (contractDto.getProperty() != null && contractDto.getProperty().getId() != null) {
                        Optional<Property> property = propertyRepository.findById(contractDto.getProperty().getId());
                        property.ifPresent(existingContract::setProperty);
                    }
                    // クライアント情報の設定（IDからクライアントエンティティを取得）
                    if (contractDto.getClient() != null && contractDto.getClient().getId() != null) {
                        Optional<Client> client = clientRepository.findById(contractDto.getClient().getId());
                        client.ifPresent(existingContract::setClient);
                    }
                    // 契約基本情報の更新
                    existingContract.setType(contractDto.getType());
                    existingContract.setStatus(contractDto.getStatus());
                    existingContract.setAmount(contractDto.getAmount());
                    existingContract.setStartDate(contractDto.getStartDate());
                    existingContract.setEndDate(contractDto.getEndDate());
                    existingContract.setTerms(contractDto.getTerms());
                    existingContract.setUpdatedAt(LocalDateTime.now());
                    Contract savedContract = contractRepository.save(existingContract);
                    return convertToDto(savedContract);
                });
    }

    /**
     * 契約の削除
     * @param id 削除対象の契約ID
     * @return 削除成功時true、存在しない場合はfalse
     */
    public boolean deleteContract(Long id) {
        if (contractRepository.existsById(id)) {
            contractRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 契約エンティティをDTOに変換
     * @param contract 契約エンティティ
     * @return 契約DTO
     */
    private ContractDto convertToDto(Contract contract) {
        ContractDto dto = new ContractDto();
        dto.setId(contract.getId());
        dto.setContractNumber(contract.getContractNumber());
        
        // 物件情報のDTO変換
        if (contract.getProperty() != null) {
            PropertyDto propertyDto = new PropertyDto();
            propertyDto.setId(contract.getProperty().getId());
            propertyDto.setName(contract.getProperty().getName());
            propertyDto.setAddress(contract.getProperty().getAddress());
            propertyDto.setDescription(contract.getProperty().getDescription());
            propertyDto.setType(contract.getProperty().getType());
            propertyDto.setStatus(contract.getProperty().getStatus());
            propertyDto.setPrice(contract.getProperty().getPrice());
            propertyDto.setArea(contract.getProperty().getArea());
            propertyDto.setRooms(contract.getProperty().getRooms());
            propertyDto.setBathrooms(contract.getProperty().getBathrooms());
            propertyDto.setParkingSpaces(contract.getProperty().getParkingSpaces());
            propertyDto.setYearBuilt(contract.getProperty().getYearBuilt());
            propertyDto.setCreatedAt(contract.getProperty().getCreatedAt());
            propertyDto.setUpdatedAt(contract.getProperty().getUpdatedAt());
            dto.setProperty(propertyDto);
        }
        
        // クライアント情報のDTO変換
        if (contract.getClient() != null) {
            ClientDto clientDto = new ClientDto();
            clientDto.setId(contract.getClient().getId());
            clientDto.setFirstName(contract.getClient().getFirstName());
            clientDto.setLastName(contract.getClient().getLastName());
            clientDto.setEmail(contract.getClient().getEmail());
            clientDto.setPhone(contract.getClient().getPhone());
            clientDto.setAddress(contract.getClient().getAddress());
            clientDto.setType(contract.getClient().getType());
            clientDto.setCreatedAt(contract.getClient().getCreatedAt());
            clientDto.setUpdatedAt(contract.getClient().getUpdatedAt());
            dto.setClient(clientDto);
        }
        
        // 物件名と顧客名の設定
        if (contract.getProperty() != null) {
            dto.setPropertyName(contract.getProperty().getName());
        }
        if (contract.getClient() != null) {
            dto.setClientName(contract.getClient().getFirstName() + " " + contract.getClient().getLastName());
        }
        
        // 契約基本情報の設定
        dto.setType(contract.getType());
        dto.setStatus(contract.getStatus());
        dto.setAmount(contract.getAmount());
        dto.setMonthlyRent(contract.getMonthlyRent());
        dto.setStartDate(contract.getStartDate());
        dto.setEndDate(contract.getEndDate());
        dto.setTerms(contract.getTerms());
        dto.setCreatedAt(contract.getCreatedAt());
        dto.setUpdatedAt(contract.getUpdatedAt());
        return dto;
    }

    /**
     * 契約DTOをエンティティに変換
     * @param dto 契約DTO
     * @return 契約エンティティ
     */
    private Contract convertToEntity(ContractDto dto) {
        Contract contract = new Contract();
        contract.setId(dto.getId());
        contract.setContractNumber(dto.getContractNumber());
        
        // 物件情報の設定（IDから物件エンティティを取得）
        if (dto.getProperty() != null && dto.getProperty().getId() != null) {
            Optional<Property> property = propertyRepository.findById(dto.getProperty().getId());
            property.ifPresent(contract::setProperty);
        }
        
        // クライアント情報の設定（IDからクライアントエンティティを取得）
        if (dto.getClient() != null && dto.getClient().getId() != null) {
            Optional<Client> client = clientRepository.findById(dto.getClient().getId());
            client.ifPresent(contract::setClient);
        }
        
        // 契約基本情報の設定
        contract.setType(dto.getType());
        contract.setStatus(dto.getStatus());
        contract.setAmount(dto.getAmount());
        contract.setMonthlyRent(dto.getMonthlyRent());
        contract.setStartDate(dto.getStartDate());
        contract.setEndDate(dto.getEndDate());
        contract.setTerms(dto.getTerms());
        contract.setCreatedAt(dto.getCreatedAt());
        contract.setUpdatedAt(dto.getUpdatedAt());
        return contract;
    }
}
