package com.realestate.service;

import com.realestate.entity.DigitalSignature;
import com.realestate.repository.DigitalSignatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * 電子署名サービスクラス
 * 
 * 電子署名の作成、検証、管理を行うサービスクラスです。
 * 暗号化、ハッシュ生成、署名検証などの機能を提供します。
 * 
 * @author システム開発チーム
 * @version 1.0.0
 * @since 2025-08-11
 */
@Service
@RequiredArgsConstructor
public class DigitalSignatureService {

    private static final Logger log = LoggerFactory.getLogger(DigitalSignatureService.class);

    private final DigitalSignatureRepository digitalSignatureRepository;

    /**
     * 電子署名を作成
     * 
     * @param signerId 署名者ID
     * @param signerName 署名者名
     * @param contractId 契約ID
     * @param documentType 文書タイプ
     * @param documentContent 署名対象の文書内容
     * @param privateKeyBase64 秘密鍵（Base64エンコード）
     * @return 作成された電子署名
     */
    public DigitalSignature createSignature(Long signerId, String signerName, Long contractId,
                                         DigitalSignature.DocumentType documentType, String documentContent,
                                         String privateKeyBase64) {
        try {
            // 文書のハッシュを生成
            String documentHash = generateHash(documentContent);
            
            // 秘密鍵で署名を作成
            String signatureData = signDocument(documentHash, privateKeyBase64);
            
            // 署名ハッシュを生成
            String signatureHash = generateHash(signatureData);
            
            // 電子署名エンティティを作成
            DigitalSignature signature = new DigitalSignature();
            signature.setSignerId(signerId);
            signature.setSignerName(signerName);
            signature.setContractId(contractId);
            signature.setDocumentType(documentType);
            signature.setSignatureData(signatureData);
            signature.setSignatureHash(signatureHash);
            signature.setSignedAt(LocalDateTime.now());
            signature.setExpiresAt(LocalDateTime.now().plusYears(10)); // 10年間有効
            signature.setStatus(DigitalSignature.SignatureStatus.SIGNED);
            
            // データベースに保存
            return digitalSignatureRepository.save(signature);
            
        } catch (Exception e) {
            log.error("電子署名の作成に失敗しました: {}", e.getMessage(), e);
            throw new RuntimeException("電子署名の作成に失敗しました", e);
        }
    }

    /**
     * 電子署名を検証
     * 
     * @param signatureId 署名ID
     * @param documentContent 署名対象の文書内容
     * @param publicKeyBase64 公開鍵（Base64エンコード）
     * @return 検証結果
     */
    public boolean verifySignature(Long signatureId, String documentContent, String publicKeyBase64) {
        try {
            Optional<DigitalSignature> signatureOpt = digitalSignatureRepository.findById(signatureId);
            if (signatureOpt.isEmpty()) {
                log.warn("署名が見つかりません: {}", signatureId);
                return false;
            }
            
            DigitalSignature signature = signatureOpt.get();
            
            // 署名の有効性をチェック
            if (signature.getStatus() != DigitalSignature.SignatureStatus.SIGNED &&
                signature.getStatus() != DigitalSignature.SignatureStatus.VERIFIED) {
                log.warn("署名が無効な状態です: {}", signature.getStatus());
                return false;
            }
            
            if (LocalDateTime.now().isAfter(signature.getExpiresAt())) {
                log.warn("署名の有効期限が切れています: {}", signature.getExpiresAt());
                return false;
            }
            
            // 文書のハッシュを生成
            String documentHash = generateHash(documentContent);
            
            // 署名を検証
            boolean isValid = verifyDocumentSignature(documentHash, signature.getSignatureData(), publicKeyBase64);
            
            // 検証結果を更新
            signature.setVerificationResult(isValid ? "VERIFIED" : "INVALID");
            if (isValid) {
                signature.setStatus(DigitalSignature.SignatureStatus.VERIFIED);
            }
            digitalSignatureRepository.save(signature);
            
            return isValid;
            
        } catch (Exception e) {
            log.error("署名の検証に失敗しました: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 署名者の署名一覧を取得
     * 
     * @param signerId 署名者ID
     * @return 署名一覧
     */
    public List<DigitalSignature> getSignaturesBySigner(Long signerId) {
        return digitalSignatureRepository.findBySignerId(signerId);
    }

    /**
     * 契約の署名一覧を取得
     * 
     * @param contractId 契約ID
     * @return 署名一覧
     */
    public List<DigitalSignature> getSignaturesByContract(Long contractId) {
        return digitalSignatureRepository.findByContractId(contractId);
    }

    /**
     * 署名を無効化
     * 
     * @param signatureId 署名ID
     * @return 無効化成功時true
     */
    public boolean revokeSignature(Long signatureId) {
        try {
            Optional<DigitalSignature> signatureOpt = digitalSignatureRepository.findById(signatureId);
            if (signatureOpt.isEmpty()) {
                return false;
            }
            
            DigitalSignature signature = signatureOpt.get();
            signature.setStatus(DigitalSignature.SignatureStatus.REVOKED);
            digitalSignatureRepository.save(signature);
            
            return true;
            
        } catch (Exception e) {
            log.error("署名の無効化に失敗しました: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 文書のハッシュを生成
     */
    private String generateHash(String content) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(content.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * 文書に署名を作成
     */
    private String signDocument(String documentHash, String privateKeyBase64) throws Exception {
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(documentHash.getBytes());
        
        byte[] signatureBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    /**
     * 署名を検証
     */
    private boolean verifyDocumentSignature(String documentHash, String signatureData, String publicKeyBase64) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(documentHash.getBytes());
        
        byte[] signatureBytes = Base64.getDecoder().decode(signatureData);
        return signature.verify(signatureBytes);
    }
}
