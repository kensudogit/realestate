package com.realestate.repository;

import com.realestate.entity.BiometricData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 生体認証データリポジトリクラス
 * 
 * 生体認証データのデータアクセスを行うリポジトリクラスです。
 * 
 * @author システム開発チーム
 * @version 1.0.0
 * @since 2025-08-11
 */
@Repository
public interface BiometricDataRepository extends JpaRepository<BiometricData, Long> {

    /**
     * ユーザーIDで生体データ一覧を取得
     * 
     * @param userId ユーザーID
     * @return 生体データ一覧
     */
    List<BiometricData> findByUserId(Long userId);

    /**
     * ユーザーIDと生体データタイプで生体データ一覧を取得
     * 
     * @param userId ユーザーID
     * @param biometricType 生体データタイプ
     * @return 生体データ一覧
     */
    List<BiometricData> findByUserIdAndBiometricType(Long userId, BiometricData.BiometricType biometricType);

    /**
     * ユーザーID、生体データタイプ、ステータスで生体データ一覧を取得
     * 
     * @param userId ユーザーID
     * @param biometricType 生体データタイプ
     * @param status ステータス
     * @return 生体データ一覧
     */
    List<BiometricData> findByUserIdAndBiometricTypeAndStatus(Long userId, BiometricData.BiometricType biometricType, BiometricData.BiometricStatus status);

    /**
     * 生体データハッシュで生体データを検索
     * 
     * @param biometricHash 生体データハッシュ
     * @return 生体データ（存在しない場合はnull）
     */
    BiometricData findByBiometricHash(String biometricHash);

    /**
     * 生体データハッシュの存在チェック
     * 
     * @param biometricHash 生体データハッシュ
     * @return 存在する場合true
     */
    boolean existsByBiometricHash(String biometricHash);

    /**
     * ステータスで生体データ一覧を取得
     * 
     * @param status ステータス
     * @return 生体データ一覧
     */
    List<BiometricData> findByStatus(BiometricData.BiometricStatus status);
}
