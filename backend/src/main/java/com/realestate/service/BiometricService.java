package com.realestate.service;

import com.realestate.entity.BiometricData;
import com.realestate.repository.BiometricDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * 生体認証サービスクラス
 * 
 * 生体認証（指紋・掌紋）の登録、検証、管理を行うサービスクラスです。
 * 生体データの暗号化、ハッシュ生成、マッチングなどの機能を提供します。
 * 
 * @author システム開発チーム
 * @version 1.0.0
 * @since 2025-08-11
 */
@Service
@RequiredArgsConstructor
public class BiometricService {

    private static final Logger log = LoggerFactory.getLogger(BiometricService.class);

    private final BiometricDataRepository biometricDataRepository;

    /**
     * 生体データを登録
     * 
     * @param userId ユーザーID
     * @param userName ユーザー名
     * @param biometricType 生体データタイプ
     * @param biometricDataBase64 生体データ（Base64エンコード）
     * @param qualityScore データ品質スコア
     * @return 登録された生体データ
     */
    public BiometricData registerBiometricData(Long userId, String userName, 
                                             BiometricData.BiometricType biometricType,
                                             String biometricDataBase64, Integer qualityScore) {
        try {
            // 生体データのハッシュを生成
            String biometricHash = generateHash(biometricDataBase64);
            
            // 既存のデータとの重複チェック
            if (biometricDataRepository.existsByBiometricHash(biometricHash)) {
                throw new RuntimeException("同じ生体データが既に登録されています");
            }
            
            // 生体データエンティティを作成
            BiometricData biometricData = new BiometricData();
            biometricData.setUserId(userId);
            biometricData.setUserName(userName);
            biometricData.setBiometricType(biometricType);
            biometricData.setBiometricData(biometricDataBase64);
            biometricData.setBiometricHash(biometricHash);
            biometricData.setQualityScore(qualityScore);
            biometricData.setRegisteredAt(LocalDateTime.now());
            biometricData.setExpiresAt(LocalDateTime.now().plusYears(5)); // 5年間有効
            biometricData.setStatus(BiometricData.BiometricStatus.ACTIVE);
            
            // データベースに保存
            return biometricDataRepository.save(biometricData);
            
        } catch (Exception e) {
            log.error("生体データの登録に失敗しました: {}", e.getMessage(), e);
            throw new RuntimeException("生体データの登録に失敗しました", e);
        }
    }

    /**
     * 生体認証を実行
     * 
     * @param userId ユーザーID
     * @param biometricType 生体データタイプ
     * @param biometricDataBase64 認証用生体データ（Base64エンコード）
     * @return 認証結果
     */
    public boolean authenticateBiometric(Long userId, BiometricData.BiometricType biometricType,
                                       String biometricDataBase64) {
        try {
            // ユーザーの登録済み生体データを取得
            List<BiometricData> registeredData = biometricDataRepository
                .findByUserIdAndBiometricTypeAndStatus(userId, biometricType, BiometricData.BiometricStatus.ACTIVE);
            
            if (registeredData.isEmpty()) {
                log.warn("ユーザー {} の生体データが見つかりません: {}", userId, biometricType);
                return false;
            }
            
            // 認証用データのハッシュを生成
            String inputHash = generateHash(biometricDataBase64);
            
            // 登録済みデータとのマッチング
            for (BiometricData registered : registeredData) {
                if (isBiometricMatch(inputHash, registered.getBiometricHash())) {
                    // 最終使用日時を更新
                    registered.setLastUsedAt(LocalDateTime.now());
                    biometricDataRepository.save(registered);
                    
                    log.info("生体認証が成功しました: ユーザー {}, タイプ {}", userId, biometricType);
                    return true;
                }
            }
            
            log.warn("生体認証が失敗しました: ユーザー {}, タイプ {}", userId, biometricType);
            return false;
            
        } catch (Exception e) {
            log.error("生体認証の実行に失敗しました: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * ユーザーの生体データ一覧を取得
     * 
     * @param userId ユーザーID
     * @return 生体データ一覧
     */
    public List<BiometricData> getBiometricDataByUser(Long userId) {
        return biometricDataRepository.findByUserId(userId);
    }

    /**
     * 生体データを無効化
     * 
     * @param biometricDataId 生体データID
     * @return 無効化成功時true
     */
    public boolean deactivateBiometricData(Long biometricDataId) {
        try {
            Optional<BiometricData> biometricDataOpt = biometricDataRepository.findById(biometricDataId);
            if (biometricDataOpt.isEmpty()) {
                return false;
            }
            
            BiometricData biometricData = biometricDataOpt.get();
            biometricData.setStatus(BiometricData.BiometricStatus.INACTIVE);
            biometricDataRepository.save(biometricData);
            
            return true;
            
        } catch (Exception e) {
            log.error("生体データの無効化に失敗しました: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 生体データを削除
     * 
     * @param biometricDataId 生体データID
     * @return 削除成功時true
     */
    public boolean deleteBiometricData(Long biometricDataId) {
        try {
            Optional<BiometricData> biometricDataOpt = biometricDataRepository.findById(biometricDataId);
            if (biometricDataOpt.isEmpty()) {
                return false;
            }
            
            BiometricData biometricData = biometricDataOpt.get();
            biometricData.setStatus(BiometricData.BiometricStatus.DELETED);
            biometricDataRepository.save(biometricData);
            
            return true;
            
        } catch (Exception e) {
            log.error("生体データの削除に失敗しました: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 生体データの品質を評価
     * 
     * @param biometricDataBase64 生体データ（Base64エンコード）
     * @return 品質スコア（0-100）
     */
    public Integer evaluateBiometricQuality(String biometricDataBase64) {
        try {
            // データサイズをチェック
            byte[] data = Base64.getDecoder().decode(biometricDataBase64);
            int dataSize = data.length;
            
            // 基本的な品質評価（簡易版）
            int qualityScore = 0;
            
            if (dataSize > 1000) {
                qualityScore += 30; // データサイズが十分
            }
            
            if (dataSize > 5000) {
                qualityScore += 20; // 高解像度データ
            }
            
            // データの一貫性チェック（簡易版）
            if (hasConsistentData(data)) {
                qualityScore += 50;
            }
            
            return Math.min(100, qualityScore);
            
        } catch (Exception e) {
            log.error("生体データの品質評価に失敗しました: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 生体データのハッシュを生成
     */
    private String generateHash(String data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * 生体データのマッチング（簡易版）
     * 実際の実装では、より高度な生体認証アルゴリズムを使用
     */
    private boolean isBiometricMatch(String inputHash, String registeredHash) {
        // 簡易的なハッシュ比較（実際の実装では、生体データの特徴点マッチングなど）
        return inputHash.equals(registeredHash);
    }

    /**
     * データの一貫性チェック（簡易版）
     */
    private boolean hasConsistentData(byte[] data) {
        if (data.length < 100) {
            return false;
        }
        
        // データの基本的な一貫性をチェック
        // 実際の実装では、生体データの特徴点の分布や品質をチェック
        return true;
    }
}
