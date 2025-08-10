package com.realestate.config;

import com.realestate.entity.*;
import com.realestate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

        private final PropertyRepository propertyRepository;
        private final ClientRepository clientRepository;
        private final ContractRepository contractRepository;
        private final TransactionRepository transactionRepository;

        @Override
        public void run(String... args) throws Exception {
                // サンプルデータの投入
                initializeSampleData();
        }

        private void initializeSampleData() {
                // サンプル物件の作成
                List<Property> properties = Arrays.asList(
                                createSampleProperty(
                                                "青山マンション 101号室",
                                                "東京都港区青山1-1-1",
                                                "青山エリアの高級マンション。駅徒歩5分、南向きで日当たり良好。24時間セキュリティ、宅配ボックス完備。",
                                                Property.PropertyType.APARTMENT,
                                                Property.PropertyStatus.AVAILABLE,
                                                new BigDecimal("85000000"),
                                                new BigDecimal("65.5"),
                                                2, 1, 1, 2015),
                                createSampleProperty(
                                                "代官山一戸建て",
                                                "東京都渋谷区代官山町1-2-3",
                                                "代官山の閑静な住宅街にある一戸建て。庭付きで駐車場2台分。築年数は古いが、リノベーション済み。",
                                                Property.PropertyType.HOUSE,
                                                Property.PropertyStatus.AVAILABLE,
                                                new BigDecimal("120000000"),
                                                new BigDecimal("120.0"),
                                                4, 2, 2, 2010),
                                createSampleProperty(
                                                "新宿オフィスビル",
                                                "東京都新宿区新宿3-1-1",
                                                "新宿駅徒歩3分のオフィスビル。1階は店舗、2階以上はオフィス。エレベーター2基、24時間セキュリティ。",
                                                Property.PropertyType.COMMERCIAL,
                                                Property.PropertyStatus.AVAILABLE,
                                                new BigDecimal("250000000"),
                                                new BigDecimal("200.0"),
                                                0, 0, 5, 2008),
                                createSampleProperty(
                                                "六本木ヒルズレジデンス",
                                                "東京都港区六本木6-10-1",
                                                "六本木ヒルズ内の超高級マンション。最上階のペントハウス。東京タワーと東京スカイツリーの両方を眺望。",
                                                Property.PropertyType.APARTMENT,
                                                Property.PropertyStatus.AVAILABLE,
                                                new BigDecimal("350000000"),
                                                new BigDecimal("180.0"),
                                                4, 3, 2, 2003),
                                createSampleProperty(
                                                "銀座商業ビル",
                                                "東京都中央区銀座4-5-6",
                                                "銀座の中心部にある商業ビル。1階は高級ブランド店、2階以上はオフィス。地下鉄直結。",
                                                Property.PropertyType.COMMERCIAL,
                                                Property.PropertyStatus.AVAILABLE,
                                                new BigDecimal("180000000"),
                                                new BigDecimal("150.0"),
                                                0, 0, 3, 2012),
                                createSampleProperty(
                                                "世田谷区の土地",
                                                "東京都世田谷区世田谷1-1-1",
                                                "世田谷区の閑静な住宅街にある更地。約200坪の広大な土地。建築条件なし。",
                                                Property.PropertyType.LAND,
                                                Property.PropertyStatus.AVAILABLE,
                                                new BigDecimal("80000000"),
                                                new BigDecimal("660.0"),
                                                0, 0, 0, 0),
                                createSampleProperty(
                                                "渋谷スクランブルスクエア近くのオフィス",
                                                "東京都渋谷区渋谷2-24-12",
                                                "渋谷駅徒歩5分のオフィスビル。フロア全体を賃貸可能。IT企業に人気の立地。",
                                                Property.PropertyType.OFFICE,
                                                Property.PropertyStatus.AVAILABLE,
                                                new BigDecimal("120000000"),
                                                new BigDecimal("300.0"),
                                                0, 0, 8, 2018),
                                createSampleProperty(
                                                "横浜みなとみらいの倉庫",
                                                "神奈川県横浜市西区みなとみらい2-2-1",
                                                "みなとみらいエリアの大型倉庫。物流拠点として最適。24時間稼働可能。",
                                                Property.PropertyType.WAREHOUSE,
                                                Property.PropertyStatus.AVAILABLE,
                                                new BigDecimal("95000000"),
                                                new BigDecimal("500.0"),
                                                0, 0, 20, 2015),
                                createSampleProperty(
                                                "吉祥寺の古民家",
                                                "東京都武蔵野市吉祥寺本町1-1-1",
                                                "吉祥寺駅徒歩10分の古民家。築50年だが、庭付きで閑静。リノベーションの可能性あり。",
                                                Property.PropertyType.HOUSE,
                                                Property.PropertyStatus.AVAILABLE,
                                                new BigDecimal("68000000"),
                                                new BigDecimal("90.0"),
                                                3, 1, 1, 1973),
                                createSampleProperty(
                                                "池袋の投資用マンション",
                                                "東京都豊島区池袋1-1-1",
                                                "池袋駅徒歩8分の投資用マンション。現在賃貸中。安定した家賃収入。",
                                                Property.PropertyType.APARTMENT,
                                                Property.PropertyStatus.RENTED,
                                                new BigDecimal("45000000"),
                                                new BigDecimal("45.0"),
                                                1, 1, 0, 2005));

                properties.forEach(propertyRepository::save);

                // サンプル顧客の作成
                List<Client> clients = Arrays.asList(
                                createSampleClient(
                                                "田中", "太郎", "tanaka@example.com", "090-1234-5678",
                                                "東京都新宿区西新宿1-1-1", Client.ClientType.BUYER),
                                createSampleClient(
                                                "佐藤", "花子", "sato@example.com", "090-8765-4321",
                                                "東京都渋谷区渋谷1-2-3", Client.ClientType.SELLER),
                                createSampleClient(
                                                "山田", "次郎", "yamada@example.com", "090-5555-6666",
                                                "東京都港区六本木1-3-5", Client.ClientType.TENANT),
                                createSampleClient(
                                                "鈴木", "美咲", "suzuki@example.com", "090-1111-2222",
                                                "東京都中央区銀座1-1-1", Client.ClientType.BUYER),
                                createSampleClient(
                                                "高橋", "健一", "takahashi@example.com", "090-3333-4444",
                                                "東京都世田谷区世田谷1-1-1", Client.ClientType.LANDLORD),
                                createSampleClient(
                                                "伊藤", "恵子", "ito@example.com", "090-5555-7777",
                                                "東京都武蔵野市吉祥寺1-1-1", Client.ClientType.SELLER),
                                createSampleClient(
                                                "渡辺", "雄一", "watanabe@example.com", "090-7777-8888",
                                                "神奈川県横浜市西区みなとみらい1-1-1", Client.ClientType.BUYER),
                                createSampleClient(
                                                "中村", "由美", "nakamura@example.com", "090-9999-0000",
                                                "東京都豊島区池袋1-1-1", Client.ClientType.TENANT),
                                createSampleClient(
                                                "小林", "正男", "kobayashi@example.com", "090-1212-3434",
                                                "東京都新宿区新宿1-1-1", Client.ClientType.SELLER),
                                createSampleClient(
                                                "加藤", "愛子", "kato@example.com", "090-5656-7878",
                                                "東京都港区青山1-1-1", Client.ClientType.BUYER));

                clients.forEach(clientRepository::save);

                // サンプル契約の作成
                List<Contract> contracts = Arrays.asList(
                                createSampleContract(
                                                properties.get(0), clients.get(2), Contract.ContractType.RENTAL,
                                                Contract.ContractStatus.ACTIVE,
                                                new BigDecimal("150000"),
                                                LocalDateTime.now().minusMonths(6),
                                                LocalDateTime.now().plusMonths(6),
                                                "月額家賃15万円、敷金2ヶ月分、礼金1ヶ月分、共益費込み"),
                                createSampleContract(
                                                properties.get(3), clients.get(6), Contract.ContractType.SALE,
                                                Contract.ContractStatus.PENDING,
                                                new BigDecimal("320000000"),
                                                LocalDateTime.now().minusDays(30),
                                                LocalDateTime.now().plusDays(60),
                                                "頭金20%、残りは住宅ローン。手数料は売主負担。"),
                                createSampleContract(
                                                properties.get(4), clients.get(3), Contract.ContractType.LEASE,
                                                Contract.ContractStatus.ACTIVE,
                                                new BigDecimal("800000"),
                                                LocalDateTime.now().minusMonths(12),
                                                LocalDateTime.now().plusMonths(24),
                                                "月額賃料80万円、保証金2ヶ月分、更新料1ヶ月分"),
                                createSampleContract(
                                                properties.get(7), clients.get(4), Contract.ContractType.MANAGEMENT,
                                                Contract.ContractStatus.ACTIVE,
                                                new BigDecimal("50000"),
                                                LocalDateTime.now().minusMonths(3),
                                                LocalDateTime.now().plusMonths(12),
                                                "管理費月額5万円、入居者募集代行、家賃徴収代行"),
                                createSampleContract(
                                                properties.get(9), clients.get(7), Contract.ContractType.RENTAL,
                                                Contract.ContractStatus.ACTIVE,
                                                new BigDecimal("85000"),
                                                LocalDateTime.now().minusMonths(8),
                                                LocalDateTime.now().plusMonths(4),
                                                "月額家賃8.5万円、敷金1ヶ月分、礼金なし、共益費別途"));

                contracts.forEach(contractRepository::save);

                // サンプル取引の作成
                List<Transaction> transactions = Arrays.asList(
                                createSampleTransaction(
                                                contracts.get(0), Transaction.TransactionType.PAYMENT,
                                                new BigDecimal("150000"),
                                                LocalDateTime.now().minusDays(15),
                                                "1月分家賃支払い",
                                                Transaction.TransactionStatus.COMPLETED),
                                createSampleTransaction(
                                                contracts.get(0), Transaction.TransactionType.PAYMENT,
                                                new BigDecimal("150000"),
                                                LocalDateTime.now().minusDays(15),
                                                "2月分家賃支払い",
                                                Transaction.TransactionStatus.COMPLETED),
                                createSampleTransaction(
                                                contracts.get(1), Transaction.TransactionType.PAYMENT,
                                                new BigDecimal("64000000"),
                                                LocalDateTime.now().minusDays(10),
                                                "頭金支払い",
                                                Transaction.TransactionStatus.COMPLETED),
                                createSampleTransaction(
                                                contracts.get(2), Transaction.TransactionType.PAYMENT,
                                                new BigDecimal("800000"),
                                                LocalDateTime.now().minusDays(5),
                                                "1月分賃料支払い",
                                                Transaction.TransactionStatus.COMPLETED),
                                createSampleTransaction(
                                                contracts.get(3), Transaction.TransactionType.COMMISSION,
                                                new BigDecimal("25000"),
                                                LocalDateTime.now().minusDays(20),
                                                "管理手数料",
                                                Transaction.TransactionStatus.COMPLETED),
                                createSampleTransaction(
                                                contracts.get(4), Transaction.TransactionType.PAYMENT,
                                                new BigDecimal("85000"),
                                                LocalDateTime.now().minusDays(12),
                                                "1月分家賃支払い",
                                                Transaction.TransactionStatus.COMPLETED),
                                createSampleTransaction(
                                                contracts.get(0), Transaction.TransactionType.MAINTENANCE,
                                                new BigDecimal("50000"),
                                                LocalDateTime.now().minusDays(8),
                                                "エアコン修理費",
                                                Transaction.TransactionStatus.COMPLETED),
                                createSampleTransaction(
                                                contracts.get(2), Transaction.TransactionType.INSURANCE,
                                                new BigDecimal("120000"),
                                                LocalDateTime.now().minusDays(25),
                                                "火災保険料",
                                                Transaction.TransactionStatus.COMPLETED));

                transactions.forEach(transactionRepository::save);
        }

        private Property createSampleProperty(String name, String address, String description,
                        Property.PropertyType type, Property.PropertyStatus status,
                        BigDecimal price, BigDecimal area, Integer rooms,
                        Integer bathrooms, Integer parkingSpaces, Integer yearBuilt) {
                Property property = new Property();
                property.setName(name);
                property.setAddress(address);
                property.setDescription(description);
                property.setType(type);
                property.setStatus(status);
                property.setPrice(price);
                property.setArea(area);
                property.setRooms(rooms);
                property.setBathrooms(bathrooms);
                property.setParkingSpaces(parkingSpaces);
                property.setYearBuilt(yearBuilt);
                return property;
        }

        private Client createSampleClient(String firstName, String lastName, String email,
                        String phone, String address, Client.ClientType type) {
                Client client = new Client();
                client.setFirstName(firstName);
                client.setLastName(lastName);
                client.setEmail(email);
                client.setPhone(phone);
                client.setAddress(address);
                client.setType(type);
                return client;
        }

        private Contract createSampleContract(Property property, Client client,
                        Contract.ContractType type, Contract.ContractStatus status,
                        BigDecimal amount, LocalDateTime startDate,
                        LocalDateTime endDate, String terms) {
                Contract contract = new Contract();
                contract.setContractNumber("CTR-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000));
                contract.setProperty(property);
                contract.setClient(client);
                contract.setType(type);
                contract.setStatus(status);
                contract.setAmount(amount);
                contract.setStartDate(startDate);
                contract.setEndDate(endDate);
                contract.setTerms(terms);
                
                // 賃貸契約の場合は月額家賃を設定
                if (type == Contract.ContractType.RENTAL || type == Contract.ContractType.LEASE) {
                    contract.setMonthlyRent(amount);
                }
                
                return contract;
        }

        private Transaction createSampleTransaction(Contract contract,
                        Transaction.TransactionType type, BigDecimal amount,
                        LocalDateTime transactionDate, String description,
                        Transaction.TransactionStatus status) {
                Transaction transaction = new Transaction();
                transaction.setContract(contract);
                transaction.setType(type);
                transaction.setAmount(amount);
                transaction.setTransactionDate(transactionDate);
                transaction.setDescription(description);
                transaction.setStatus(status);
                return transaction;
        }
}
