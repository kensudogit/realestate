package com.realestate.controller;

import com.realestate.entity.DigitalSignature;
import com.realestate.service.DigitalSignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 電子署名コントローラークラス
 * 
 * 電子署名のREST APIを提供するコントローラークラスです。
 * 
 * @author システム開発チーム
 * @version 1.0.0
 * @since 2025-08-11
 */
@RestController
@RequestMapping("/api/signatures")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DigitalSignatureController {

    private final DigitalSignatureService digitalSignatureService;

    /**
     * 電子署名を作成
     * 
     * @param request 署名作成リクエスト
     * @return 作成された電子署名
     */
    @PostMapping
    public ResponseEntity<DigitalSignature> createSignature(@RequestBody CreateSignatureRequest request) {
        try {
            DigitalSignature signature = digitalSignatureService.createSignature(
                request.getSignerId(),
                request.getSignerName(),
                request.getContractId(),
                request.getDocumentType(),
                request.getDocumentContent(),
                request.getPrivateKeyBase64()
            );
            return ResponseEntity.ok(signature);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 電子署名を検証
     * 
     * @param signatureId 署名ID
     * @param request 署名検証リクエスト
     * @return 検証結果
     */
    @PostMapping("/{signatureId}/verify")
    public ResponseEntity<VerificationResponse> verifySignature(
            @PathVariable Long signatureId,
            @RequestBody VerifySignatureRequest request) {
        try {
            boolean isValid = digitalSignatureService.verifySignature(
                signatureId,
                request.getDocumentContent(),
                request.getPublicKeyBase64()
            );
            
            VerificationResponse response = new VerificationResponse();
            response.setValid(isValid);
            response.setMessage(isValid ? "署名が有効です" : "署名が無効です");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 署名者の署名一覧を取得
     * 
     * @param signerId 署名者ID
     * @return 署名一覧
     */
    @GetMapping("/signer/{signerId}")
    public ResponseEntity<List<DigitalSignature>> getSignaturesBySigner(@PathVariable Long signerId) {
        List<DigitalSignature> signatures = digitalSignatureService.getSignaturesBySigner(signerId);
        return ResponseEntity.ok(signatures);
    }

    /**
     * 契約の署名一覧を取得
     * 
     * @param contractId 契約ID
     * @return 署名一覧
     */
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<DigitalSignature>> getSignaturesByContract(@PathVariable Long contractId) {
        List<DigitalSignature> signatures = digitalSignatureService.getSignaturesByContract(contractId);
        return ResponseEntity.ok(signatures);
    }

    /**
     * 署名を無効化
     * 
     * @param signatureId 署名ID
     * @return 無効化結果
     */
    @PostMapping("/{signatureId}/revoke")
    public ResponseEntity<RevocationResponse> revokeSignature(@PathVariable Long signatureId) {
        boolean success = digitalSignatureService.revokeSignature(signatureId);
        
        RevocationResponse response = new RevocationResponse();
        response.setSuccess(success);
        response.setMessage(success ? "署名が無効化されました" : "署名の無効化に失敗しました");
        
        return ResponseEntity.ok(response);
    }

    // 内部クラス：リクエスト・レスポンス

    public static class CreateSignatureRequest {
        private Long signerId;
        private String signerName;
        private Long contractId;
        private DigitalSignature.DocumentType documentType;
        private String documentContent;
        private String privateKeyBase64;

        // Getters and Setters
        public Long getSignerId() { return signerId; }
        public void setSignerId(Long signerId) { this.signerId = signerId; }

        public String getSignerName() { return signerName; }
        public void setSignerName(String signerName) { this.signerName = signerName; }

        public Long getContractId() { return contractId; }
        public void setContractId(Long contractId) { this.contractId = contractId; }

        public DigitalSignature.DocumentType getDocumentType() { return documentType; }
        public void setDocumentType(DigitalSignature.DocumentType documentType) { this.documentType = documentType; }

        public String getDocumentContent() { return documentContent; }
        public void setDocumentContent(String documentContent) { this.documentContent = documentContent; }

        public String getPrivateKeyBase64() { return privateKeyBase64; }
        public void setPrivateKeyBase64(String privateKeyBase64) { this.privateKeyBase64 = privateKeyBase64; }
    }

    public static class VerifySignatureRequest {
        private String documentContent;
        private String publicKeyBase64;

        // Getters and Setters
        public String getDocumentContent() { return documentContent; }
        public void setDocumentContent(String documentContent) { this.documentContent = documentContent; }

        public String getPublicKeyBase64() { return publicKeyBase64; }
        public void setPublicKeyBase64(String publicKeyBase64) { this.publicKeyBase64 = publicKeyBase64; }
    }

    public static class VerificationResponse {
        private boolean valid;
        private String message;

        // Getters and Setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class RevocationResponse {
        private boolean success;
        private String message;

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
