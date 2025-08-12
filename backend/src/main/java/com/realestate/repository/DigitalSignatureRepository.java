package com.realestate.repository;

import com.realestate.entity.DigitalSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 電子署名リポジトリクラス
 * 
 * 電子署名のデータアクセスを行うリポジトリクラスです。
 * 
 * @author システム開発チーム
 * @version 1.0.0
 * @since 2025-08-11
 */
@Repository
public interface DigitalSignatureRepository extends JpaRepository<DigitalSignature, Long> {

    /**
     * 署名者IDで署名一覧を取得
     * 
     * @param signerId 署名者ID
     * @return 署名一覧
     */
    List<DigitalSignature> findBySignerId(Long signerId);

    /**
     * 契約IDで署名一覧を取得
     * 
     * @param contractId 契約ID
     * @return 署名一覧
     */
    List<DigitalSignature> findByContractId(Long contractId);

    /**
     * 署名ハッシュで署名を検索
     * 
     * @param signatureHash 署名ハッシュ
     * @return 署名（存在しない場合はnull）
     */
    DigitalSignature findBySignatureHash(String signatureHash);

    /**
     * 署名ステータスで署名一覧を取得
     * 
     * @param status 署名ステータス
     * @return 署名一覧
     */
    List<DigitalSignature> findByStatus(DigitalSignature.SignatureStatus status);
}
