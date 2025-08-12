package com.realestate.controller;

import com.realestate.entity.BiometricData;
import com.realestate.service.BiometricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 生体認証コントローラークラス
 * 
 * 生体認証のREST APIを提供するコントローラークラスです。
 * 
 * @author システム開発チーム
 * @version 1.0.0
 * @since 2025-08-11
 */
@RestController
@RequestMapping("/api/biometric")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BiometricController {

    private final BiometricService biometricService;

    /**
     * 生体データを登録
     * 
     * @param request 生体データ登録リクエスト
     * @return 登録された生体データ
     */
    @PostMapping("/register")
    public ResponseEntity<BiometricData> registerBiometricData(@RequestBody RegisterBiometricRequest request) {
        try {
            // データ品質を評価
            Integer qualityScore = biometricService.evaluateBiometricQuality(request.getBiometricDataBase64());
            
            BiometricData biometricData = biometricService.registerBiometricData(
                request.getUserId(),
                request.getUserName(),
                request.getBiometricType(),
                request.getBiometricDataBase64(),
                qualityScore
            );
            return ResponseEntity.ok(biometricData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 生体認証を実行
     * 
     * @param request 生体認証リクエスト
     * @return 認証結果
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticateBiometric(@RequestBody AuthenticateBiometricRequest request) {
        try {
            boolean isAuthenticated = biometricService.authenticateBiometric(
                request.getUserId(),
                request.getBiometricType(),
                request.getBiometricDataBase64()
            );
            
            AuthenticationResponse response = new AuthenticationResponse();
            response.setAuthenticated(isAuthenticated);
            response.setMessage(isAuthenticated ? "認証が成功しました" : "認証が失敗しました");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * ユーザーの生体データ一覧を取得
     * 
     * @param userId ユーザーID
     * @return 生体データ一覧
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BiometricData>> getBiometricDataByUser(@PathVariable Long userId) {
        List<BiometricData> biometricData = biometricService.getBiometricDataByUser(userId);
        return ResponseEntity.ok(biometricData);
    }

    /**
     * 生体データを無効化
     * 
     * @param biometricDataId 生体データID
     * @return 無効化結果
     */
    @PostMapping("/{biometricDataId}/deactivate")
    public ResponseEntity<DeactivationResponse> deactivateBiometricData(@PathVariable Long biometricDataId) {
        boolean success = biometricService.deactivateBiometricData(biometricDataId);
        
        DeactivationResponse response = new DeactivationResponse();
        response.setSuccess(success);
        response.setMessage(success ? "生体データが無効化されました" : "生体データの無効化に失敗しました");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 生体データを削除
     * 
     * @param biometricDataId 生体データID
     * @return 削除結果
     */
    @DeleteMapping("/{biometricDataId}")
    public ResponseEntity<DeletionResponse> deleteBiometricData(@PathVariable Long biometricDataId) {
        boolean success = biometricService.deleteBiometricData(biometricDataId);
        
        DeletionResponse response = new DeletionResponse();
        response.setSuccess(success);
        response.setMessage(success ? "生体データが削除されました" : "生体データの削除に失敗しました");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 生体データの品質を評価
     * 
     * @param request 品質評価リクエスト
     * @return 品質評価結果
     */
    @PostMapping("/evaluate-quality")
    public ResponseEntity<QualityEvaluationResponse> evaluateQuality(@RequestBody EvaluateQualityRequest request) {
        try {
            Integer qualityScore = biometricService.evaluateBiometricQuality(request.getBiometricDataBase64());
            
            QualityEvaluationResponse response = new QualityEvaluationResponse();
            response.setQualityScore(qualityScore);
            response.setQualityLevel(getQualityLevel(qualityScore));
            response.setMessage("品質評価が完了しました");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 品質スコアから品質レベルを判定
     */
    private String getQualityLevel(Integer qualityScore) {
        if (qualityScore >= 80) return "EXCELLENT";
        if (qualityScore >= 60) return "GOOD";
        if (qualityScore >= 40) return "FAIR";
        if (qualityScore >= 20) return "POOR";
        return "UNACCEPTABLE";
    }

    // 内部クラス：リクエスト・レスポンス

    public static class RegisterBiometricRequest {
        private Long userId;
        private String userName;
        private BiometricData.BiometricType biometricType;
        private String biometricDataBase64;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public BiometricData.BiometricType getBiometricType() { return biometricType; }
        public void setBiometricType(BiometricData.BiometricType biometricType) { this.biometricType = biometricType; }

        public String getBiometricDataBase64() { return biometricDataBase64; }
        public void setBiometricDataBase64(String biometricDataBase64) { this.biometricDataBase64 = biometricDataBase64; }
    }

    public static class AuthenticateBiometricRequest {
        private Long userId;
        private BiometricData.BiometricType biometricType;
        private String biometricDataBase64;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public BiometricData.BiometricType getBiometricType() { return biometricType; }
        public void setBiometricType(BiometricData.BiometricType biometricType) { this.biometricType = biometricType; }

        public String getBiometricDataBase64() { return biometricDataBase64; }
        public void setBiometricDataBase64(String biometricDataBase64) { this.biometricDataBase64 = biometricDataBase64; }
    }

    public static class AuthenticationResponse {
        private boolean authenticated;
        private String message;

        // Getters and Setters
        public boolean isAuthenticated() { return authenticated; }
        public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class DeactivationResponse {
        private boolean success;
        private String message;

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class DeletionResponse {
        private boolean success;
        private String message;

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class EvaluateQualityRequest {
        private String biometricDataBase64;

        // Getters and Setters
        public String getBiometricDataBase64() { return biometricDataBase64; }
        public void setBiometricDataBase64(String biometricDataBase64) { this.biometricDataBase64 = biometricDataBase64; }
    }

    public static class QualityEvaluationResponse {
        private Integer qualityScore;
        private String qualityLevel;
        private String message;

        // Getters and Setters
        public Integer getQualityScore() { return qualityScore; }
        public void setQualityScore(Integer qualityScore) { this.qualityScore = qualityScore; }

        public String getQualityLevel() { return qualityLevel; }
        public void setQualityLevel(String qualityLevel) { this.qualityLevel = qualityLevel; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
