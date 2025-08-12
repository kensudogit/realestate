# 倉庫管理システム 単体テスト設計書

## 1. 概要

### 1.1 目的
本ドキュメントは、倉庫管理システムの単体テストの設計・実装方針を定義し、品質保証と保守性向上を目的とします。

### 1.2 対象システム
- 物件管理機能
- 顧客管理機能  
- 契約管理機能
- 取引管理機能
- 電子認証システム（電子署名、電子タイムスタンプ、生体認証）
- 情報管理システム

### 1.3 テスト環境
- **開発環境**: Java 17, Spring Boot 3.1.5, H2 Database
- **テストフレームワーク**: JUnit 5, Spring Boot Test
- **ビルドツール**: Gradle
- **データベース**: H2 Database（テスト用）

## 2. テスト戦略

### 2.1 テストレベル
- **単体テスト**: 各クラス・メソッドの個別テスト
- **統合テスト**: コンポーネント間の連携テスト
- **APIテスト**: REST APIエンドポイントのテスト

### 2.2 テストカバレッジ目標
- **行カバレッジ**: 90%以上
- **分岐カバレッジ**: 85%以上
- **メソッドカバレッジ**: 95%以上

### 2.3 テストデータ管理
- テスト用データベースの自動初期化
- テストケースごとのデータ独立性確保
- モックデータの活用

## 3. テスト対象コンポーネント

### 3.1 エンティティ層
- Property（物件）
- Client（顧客）
- Contract（契約）
- Transaction（取引）
- DigitalSignature（電子署名）
- DigitalTimestamp（電子タイムスタンプ）
- BiometricData（生体認証データ）

### 3.2 サービス層
- PropertyService
- ClientService
- ContractService
- TransactionService
- DigitalSignatureService
- DigitalTimestampService
- BiometricService

### 3.3 コントローラー層
- PropertyController
- ClientController
- ContractController
- TransactionController
- DigitalSignatureController
- DigitalTimestampController
- BiometricController

### 3.4 リポジトリ層
- PropertyRepository
- ClientRepository
- ContractRepository
- TransactionRepository
- DigitalSignatureRepository
- DigitalTimestampRepository
- BiometricRepository

## 4. 詳細テストケース設計

### 4.1 物件管理機能テスト

#### 4.1.1 PropertyService テストケース

**テストクラス**: `PropertyServiceTest`

| テストメソッド | テスト内容 | 入力データ | 期待結果 | テストデータ |
|---------------|-----------|-----------|----------|-------------|
| `testCreateProperty_Success()` | 正常な物件作成 | 有効な物件データ | 物件が正常に作成される | 新規物件DTO |
| `testCreateProperty_InvalidData()` | 無効データでの物件作成 | 必須項目欠損データ | ValidationException発生 | 不完全な物件DTO |
| `testUpdateProperty_Success()` | 正常な物件更新 | 有効な更新データ | 物件が正常に更新される | 既存物件ID + 更新データ |
| `testUpdateProperty_NotFound()` | 存在しない物件の更新 | 存在しないID | EntityNotFoundException発生 | 無効な物件ID |
| `testDeleteProperty_Success()` | 正常な物件削除 | 有効な物件ID | 物件が正常に削除される | 既存物件ID |
| `testDeleteProperty_NotFound()` | 存在しない物件の削除 | 存在しないID | EntityNotFoundException発生 | 無効な物件ID |
| `testFindPropertyById_Success()` | 物件ID検索成功 | 有効な物件ID | 物件データが返される | 既存物件ID |
| `testFindPropertyById_NotFound()` | 存在しない物件ID検索 | 存在しないID | EntityNotFoundException発生 | 無効な物件ID |
| `testFindAllProperties_Success()` | 全物件取得成功 | なし | 全物件リストが返される | データベース内の全物件 |
| `testSearchProperties_ByType()` | 物件タイプ別検索 | 物件タイプ | 該当タイプの物件リスト | 各物件タイプ |
| `testSearchProperties_ByStatus()` | 物件ステータス別検索 | 物件ステータス | 該当ステータスの物件リスト | 各物件ステータス |
| `testSearchProperties_ByPriceRange()` | 価格範囲検索 | 最小・最大価格 | 該当価格範囲の物件リスト | 様々な価格の物件 |
| `testSearchProperties_ByArea()` | 面積範囲検索 | 最小・最大面積 | 該当面積範囲の物件リスト | 様々な面積の物件 |

#### 4.1.2 PropertyController テストケース

**テストクラス**: `PropertyControllerTest`

| テストメソッド | テスト内容 | 入力データ | 期待結果 | HTTPステータス |
|---------------|-----------|-----------|----------|---------------|
| `testCreateProperty_ValidRequest()` | 正常なPOST要求 | 有効なJSON | 201 Created | 201 |
| `testCreateProperty_InvalidRequest()` | 無効なPOST要求 | 無効なJSON | 400 Bad Request | 400 |
| `testGetProperty_ValidId()` | 正常なGET要求 | 有効なID | 200 OK + 物件データ | 200 |
| `testGetProperty_InvalidId()` | 無効なIDでのGET要求 | 無効なID | 404 Not Found | 404 |
| `testUpdateProperty_ValidRequest()` | 正常なPUT要求 | 有効なJSON | 200 OK + 更新データ | 200 |
| `testUpdateProperty_InvalidRequest()` | 無効なPUT要求 | 無効なJSON | 400 Bad Request | 400 |
| `testDeleteProperty_ValidId()` | 正常なDELETE要求 | 有効なID | 204 No Content | 204 |
| `testDeleteProperty_InvalidId()` | 無効なIDでのDELETE要求 | 無効なID | 404 Not Found | 404 |
| `testSearchProperties_ValidCriteria()` | 正常な検索要求 | 有効な検索条件 | 200 OK + 検索結果 | 200 |
| `testSearchProperties_InvalidCriteria()` | 無効な検索条件 | 無効な検索条件 | 400 Bad Request | 400 |

#### 4.1.3 PropertyRepository テストケース

**テストクラス**: `PropertyRepositoryTest`

| テストメソッド | テスト内容 | 入力データ | 期待結果 | テストデータ |
|---------------|-----------|-----------|----------|-------------|
| `testSaveProperty_Success()` | 物件保存成功 | 新規物件エンティティ | 保存された物件が返される | 新規物件データ |
| `testFindById_ExistingProperty()` | 既存物件検索成功 | 既存物件ID | 物件エンティティが返される | 既存物件ID |
| `testFindById_NonExistingProperty()` | 存在しない物件検索 | 存在しないID | nullが返される | 無効なID |
| `testFindAll_WithData()` | 全物件取得（データあり） | なし | 全物件リストが返される | 複数の物件データ |
| `testFindAll_EmptyDatabase()` | 全物件取得（データなし） | なし | 空のリストが返される | 空のデータベース |
| `testFindByType_ValidType()` | タイプ別検索成功 | 有効な物件タイプ | 該当タイプの物件リスト | 各物件タイプ |
| `testFindByStatus_ValidStatus()` | ステータス別検索成功 | 有効な物件ステータス | 該当ステータスの物件リスト | 各物件ステータス |
| `testFindByPriceBetween_ValidRange()` | 価格範囲検索成功 | 有効な価格範囲 | 該当範囲の物件リスト | 様々な価格の物件 |
| `testFindByAreaBetween_ValidRange()` | 面積範囲検索成功 | 有効な面積範囲 | 該当範囲の物件リスト | 様々な面積の物件 |
| `testDeleteById_ExistingProperty()` | 既存物件削除成功 | 既存物件ID | 削除が実行される | 既存物件ID |
| `testDeleteById_NonExistingProperty()` | 存在しない物件削除 | 存在しないID | 削除が実行される（影響なし） | 無効なID |
| `testCount_WithData()` | 件数カウント（データあり） | なし | 正しい件数が返される | 複数の物件データ |
| `testCount_EmptyDatabase()` | 件数カウント（データなし） | なし | 0が返される | 空のデータベース |

### 4.2 顧客管理機能テスト

#### 4.2.1 ClientService テストケース

**テストクラス**: `ClientServiceTest`

| テストメソッド | テスト内容 | 入力データ | 期待結果 | テストデータ |
|---------------|-----------|-----------|----------|-------------|
| `testCreateClient_Success()` | 正常な顧客作成 | 有効な顧客データ | 顧客が正常に作成される | 新規顧客DTO |
| `testCreateClient_InvalidData()` | 無効データでの顧客作成 | 必須項目欠損データ | ValidationException発生 | 不完全な顧客DTO |
| `testUpdateClient_Success()` | 正常な顧客更新 | 有効な更新データ | 顧客が正常に更新される | 既存顧客ID + 更新データ |
| `testUpdateClient_NotFound()` | 存在しない顧客の更新 | 存在しないID | EntityNotFoundException発生 | 無効な顧客ID |
| `testDeleteClient_Success()` | 正常な顧客削除 | 有効な顧客ID | 顧客が正常に削除される | 既存顧客ID |
| `testDeleteClient_NotFound()` | 存在しない顧客の削除 | 存在しないID | EntityNotFoundException発生 | 無効な顧客ID |
| `testFindClientById_Success()` | 顧客ID検索成功 | 有効な顧客ID | 顧客データが返される | 既存顧客ID |
| `testFindClientById_NotFound()` | 存在しない顧客ID検索 | 存在しないID | EntityNotFoundException発生 | 無効な顧客ID |
| `testFindAllClients_Success()` | 全顧客取得成功 | なし | 全顧客リストが返される | データベース内の全顧客 |
| `testAdvancedSearch_ByName()` | 顧客名での詳細検索 | 顧客名 | 該当名の顧客リスト | 様々な顧客名 |
| `testAdvancedSearch_ByType()` | 顧客タイプでの詳細検索 | 顧客タイプ | 該当タイプの顧客リスト | 各顧客タイプ |
| `testAdvancedSearch_ByStatus()` | 顧客ステータスでの詳細検索 | 顧客ステータス | 該当ステータスの顧客リスト | 各顧客ステータス |
| `testAdvancedSearch_ByEmail()` | メールアドレスでの詳細検索 | メールアドレス | 該当メールの顧客リスト | 様々なメールアドレス |
| `testAdvancedSearch_ByPhone()` | 電話番号での詳細検索 | 電話番号 | 該当電話番号の顧客リスト | 様々な電話番号 |
| `testAdvancedSearch_CombinedCriteria()` | 複合条件での詳細検索 | 複数の検索条件 | 該当条件の顧客リスト | 複合条件の組み合わせ |

### 4.3 契約管理機能テスト

#### 4.3.1 ContractService テストケース

**テストクラス**: `ContractServiceTest`

| テストメソッド | テスト内容 | 入力データ | 期待結果 | テストデータ |
|---------------|-----------|-----------|----------|-------------|
| `testCreateContract_Success()` | 正常な契約作成 | 有効な契約データ | 契約が正常に作成される | 新規契約DTO |
| `testCreateContract_InvalidData()` | 無効データでの契約作成 | 必須項目欠損データ | ValidationException発生 | 不完全な契約DTO |
| `testCreateContract_InvalidProperty()` | 無効な物件での契約作成 | 存在しない物件ID | EntityNotFoundException発生 | 無効な物件ID |
| `testCreateContract_InvalidClient()` | 無効な顧客での契約作成 | 存在しない顧客ID | EntityNotFoundException発生 | 無効な顧客ID |
| `testUpdateContract_Success()` | 正常な契約更新 | 有効な更新データ | 契約が正常に更新される | 既存契約ID + 更新データ |
| `testUpdateContract_NotFound()` | 存在しない契約の更新 | 存在しないID | EntityNotFoundException発生 | 無効な契約ID |
| `testDeleteContract_Success()` | 正常な契約削除 | 有効な契約ID | 契約が正常に削除される | 既存契約ID |
| `testDeleteContract_NotFound()` | 存在しない契約の削除 | 存在しないID | EntityNotFoundException発生 | 無効な契約ID |
| `testFindContractById_Success()` | 契約ID検索成功 | 有効な契約ID | 契約データが返される | 既存契約ID |
| `testFindContractById_NotFound()` | 存在しない契約ID検索 | 存在しないID | EntityNotFoundException発生 | 無効な契約ID |
| `testFindAllContracts_Success()` | 全契約取得成功 | なし | 全契約リストが返される | データベース内の全契約 |
| `testFindContractsByProperty()` | 物件別契約検索 | 有効な物件ID | 該当物件の契約リスト | 既存物件ID |
| `testFindContractsByClient()` | 顧客別契約検索 | 有効な顧客ID | 該当顧客の契約リスト | 既存顧客ID |
| `testFindContractsByType()` | 契約タイプ別検索 | 契約タイプ | 該当タイプの契約リスト | 各契約タイプ |
| `testFindContractsByStatus()` | 契約ステータス別検索 | 契約ステータス | 該当ステータスの契約リスト | 各契約ステータス |
| `testFindContractsByDateRange()` | 契約期間検索 | 開始・終了日 | 該当期間の契約リスト | 様々な日付範囲 |

### 4.4 取引管理機能テスト

#### 4.4.1 TransactionService テストケース

**テストクラス**: `TransactionServiceTest`

| テストメソッド | テスト内容 | 入力データ | 期待結果 | テストデータ |
|---------------|-----------|-----------|----------|-------------|
| `testCreateTransaction_Success()` | 正常な取引作成 | 有効な取引データ | 取引が正常に作成される | 新規取引DTO |
| `testCreateTransaction_InvalidData()` | 無効データでの取引作成 | 必須項目欠損データ | ValidationException発生 | 不完全な取引DTO |
| `testCreateTransaction_InvalidContract()` | 無効な契約での取引作成 | 存在しない契約ID | EntityNotFoundException発生 | 無効な契約ID |
| `testUpdateTransaction_Success()` | 正常な取引更新 | 有効な更新データ | 取引が正常に更新される | 既存取引ID + 更新データ |
| `testUpdateTransaction_NotFound()` | 存在しない取引の更新 | 存在しないID | EntityNotFoundException発生 | 無効な取引ID |
| `testDeleteTransaction_Success()` | 正常な取引削除 | 有効な取引ID | 取引が正常に削除される | 既存取引ID |
| `testDeleteTransaction_NotFound()` | 存在しない取引の削除 | 存在しないID | EntityNotFoundException発生 | 無効な取引ID |
| `testFindTransactionById_Success()` | 取引ID検索成功 | 有効な取引ID | 取引データが返される | 既存取引ID |
| `testFindTransactionById_NotFound()` | 存在しない取引ID検索 | 存在しないID | EntityNotFoundException発生 | 無効な取引ID |
| `testFindAllTransactions_Success()` | 全取引取得成功 | なし | 全取引リストが返される | データベース内の全取引 |
| `testFindTransactionsByContract()` | 契約別取引検索 | 有効な契約ID | 該当契約の取引リスト | 既存契約ID |
| `testFindTransactionsByType()` | 取引タイプ別検索 | 取引タイプ | 該当タイプの取引リスト | 各取引タイプ |
| `testFindTransactionsByStatus()` | 取引ステータス別検索 | 取引ステータス | 該当ステータスの取引リスト | 各取引ステータス |
| `testFindTransactionsByDateRange()` | 取引日付範囲検索 | 開始・終了日 | 該当期間の取引リスト | 様々な日付範囲 |
| `testCalculateTotalAmount_Success()` | 取引金額合計計算 | 契約ID | 正しい合計金額が返される | 複数の取引データ |

### 4.5 電子認証システムテスト

#### 4.5.1 電子署名機能テスト

**テストクラス**: `DigitalSignatureServiceTest`

| テストメソッド | テスト内容 | 入力データ | 期待結果 | テストデータ |
|---------------|-----------|-----------|----------|-------------|
| `testCreateSignature_Success()` | 正常な署名作成 | 有効な署名データ | 署名が正常に作成される | 新規署名DTO |
| `testCreateSignature_InvalidData()` | 無効データでの署名作成 | 必須項目欠損データ | ValidationException発生 | 不完全な署名DTO |
| `testCreateSignature_InvalidDocument()` | 無効な文書での署名作成 | 存在しない文書ID | EntityNotFoundException発生 | 無効な文書ID |
| `testVerifySignature_ValidSignature()` | 有効な署名の検証 | 有効な署名ID | 検証成功（true） | 有効な署名データ |
| `testVerifySignature_InvalidSignature()` | 無効な署名の検証 | 無効な署名ID | 検証失敗（false） | 無効な署名データ |
| `testVerifySignature_ExpiredSignature()` | 期限切れ署名の検証 | 期限切れ署名ID | 検証失敗（false） | 期限切れ署名データ |
| `testDeleteSignature_Success()` | 正常な署名削除 | 有効な署名ID | 署名が正常に削除される | 既存署名ID |
| `testDeleteSignature_NotFound()` | 存在しない署名の削除 | 存在しないID | EntityNotFoundException発生 | 無効な署名ID |
| `testFindSignaturesByDocument()` | 文書別署名検索 | 有効な文書ID | 該当文書の署名リスト | 既存文書ID |
| `testFindSignaturesByStatus()` | 署名ステータス別検索 | 署名ステータス | 該当ステータスの署名リスト | 各署名ステータス |
| `testFindSignaturesByDateRange()` | 署名日付範囲検索 | 開始・終了日 | 該当期間の署名リスト | 様々な日付範囲 |

#### 4.5.2 電子タイムスタンプ機能テスト

**テストクラス**: `DigitalTimestampServiceTest`

| テストメソッド | テスト内容 | 入力データ | 期待結果 | テストデータ |
|---------------|-----------|-----------|----------|-------------|
| `testCreateTimestamp_Success()` | 正常なタイムスタンプ作成 | 有効なタイムスタンプデータ | タイムスタンプが正常に作成される | 新規タイムスタンプDTO |
| `testCreateTimestamp_InvalidData()` | 無効データでのタイムスタンプ作成 | 必須項目欠損データ | ValidationException発生 | 不完全なタイムスタンプDTO |
| `testCreateTimestamp_InvalidDocument()` | 無効な文書でのタイムスタンプ作成 | 存在しない文書ID | EntityNotFoundException発生 | 無効な文書ID |
| `testVerifyTimestamp_ValidTimestamp()` | 有効なタイムスタンプの検証 | 有効なタイムスタンプID | 検証成功（true） | 有効なタイムスタンプデータ |
| `testVerifyTimestamp_InvalidTimestamp()` | 無効なタイムスタンプの検証 | 無効なタイムスタンプID | 検証失敗（false） | 無効なタイムスタンプデータ |
| `testVerifyTimestamp_ExpiredTimestamp()` | 期限切れタイムスタンプの検証 | 期限切れタイムスタンプID | 検証失敗（false） | 期限切れタイムスタンプデータ |
| `testUpdateTimestampStatus_Success()` | タイムスタンプステータス更新 | 有効なID + 新ステータス | ステータスが正常に更新される | 既存ID + 新ステータス |
| `testUpdateTimestampStatus_NotFound()` | 存在しないタイムスタンプのステータス更新 | 存在しないID | EntityNotFoundException発生 | 無効なID |
| `testDeleteTimestamp_Success()` | 正常なタイムスタンプ削除 | 有効なタイムスタンプID | タイムスタンプが正常に削除される | 既存タイムスタンプID |
| `testDeleteTimestamp_NotFound()` | 存在しないタイムスタンプの削除 | 存在しないID | EntityNotFoundException発生 | 無効なID |
| `testFindTimestampsByDocument()` | 文書別タイムスタンプ検索 | 有効な文書ID | 該当文書のタイムスタンプリスト | 既存文書ID |
| `testFindTimestampsByStatus()` | タイムスタンプステータス別検索 | タイムスタンプステータス | 該当ステータスのタイムスタンプリスト | 各タイムスタンプステータス |
| `testFindTimestampsByDateRange()` | タイムスタンプ日付範囲検索 | 開始・終了日 | 該当期間のタイムスタンプリスト | 様々な日付範囲 |

#### 4.5.3 生体認証機能テスト

**テストクラス**: `BiometricServiceTest`

| テストメソッド | テスト内容 | 入力データ | 期待結果 | テストデータ |
|---------------|-----------|-----------|----------|-------------|
| `testRegisterBiometric_Success()` | 正常な生体データ登録 | 有効な生体データ | 生体データが正常に登録される | 新規生体データDTO |
| `testRegisterBiometric_InvalidData()` | 無効データでの生体データ登録 | 必須項目欠損データ | ValidationException発生 | 不完全な生体データDTO |
| `testRegisterBiometric_InvalidClient()` | 無効な顧客での生体データ登録 | 存在しない顧客ID | EntityNotFoundException発生 | 無効な顧客ID |
| `testVerifyBiometric_ValidData()` | 有効な生体データの検証 | 有効な生体データID | 検証成功（true） | 有効な生体データ |
| `testVerifyBiometric_InvalidData()` | 無効な生体データの検証 | 無効な生体データID | 検証失敗（false） | 無効な生体データ |
| `testVerifyBiometric_LowQualityData()` | 低品質生体データの検証 | 低品質生体データID | 検証失敗（false） | 低品質生体データ |
| `testEvaluateBiometricQuality_HighQuality()` | 高品質生体データの品質評価 | 高品質生体データID | 高品質スコアが返される | 高品質生体データ |
| `testEvaluateBiometricQuality_LowQuality()` | 低品質生体データの品質評価 | 低品質生体データID | 低品質スコアが返される | 低品質生体データ |
| `testUpdateBiometric_Success()` | 正常な生体データ更新 | 有効な更新データ | 生体データが正常に更新される | 既存生体データID + 更新データ |
| `testUpdateBiometric_NotFound()` | 存在しない生体データの更新 | 存在しないID | EntityNotFoundException発生 | 無効なID |
| `testDeleteBiometric_Success()` | 正常な生体データ削除 | 有効な生体データID | 生体データが正常に削除される | 既存生体データID |
| `testDeleteBiometric_NotFound()` | 存在しない生体データの削除 | 存在しないID | EntityNotFoundException発生 | 無効なID |
| `testFindBiometricsByClient()` | 顧客別生体データ検索 | 有効な顧客ID | 該当顧客の生体データリスト | 既存顧客ID |
| `testFindBiometricsByType()` | 生体データタイプ別検索 | 生体データタイプ | 該当タイプの生体データリスト | 各生体データタイプ |
| `testFindBiometricsByQualityRange()` | 品質範囲別生体データ検索 | 最小・最大品質スコア | 該当範囲の生体データリスト | 様々な品質スコアのデータ |
