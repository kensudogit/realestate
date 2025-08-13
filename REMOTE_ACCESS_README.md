# 不動産管理システム リモートアクセス設定

## 概要
このドキュメントでは、不動産管理システムをリモートPCからアクセスできるようにする設定手順を説明します。

## 設定済みファイル

### 1. フロントエンド設定
- **ファイル**: `frontend/vite.config.ts`
- **変更内容**: 
  - `host: '0.0.0.0'` でリモートアクセスを許可
  - `cors: true` でCORSを有効化
  - プロキシ設定を更新

### 2. バックエンド設定
- **ファイル**: `backend/src/main/resources/application.yml`
- **変更内容**:
  - `address: 0.0.0.0` でリモートアクセスを許可
  - `context-path: /api` でAPIパスを設定

### 3. API設定
- **ファイル**: `frontend/src/services/api.ts`
- **変更内容**:
  - 動的にホストを設定するように変更
  - リモートアクセスに対応

## 起動手順

### 1. ファイアウォール設定
```cmd
# 管理者権限で実行
setup-firewall.bat
```

### 2. バックエンド起動
```cmd
start-backend.bat
```

### 3. フロントエンド起動
```cmd
start-frontend.bat
```

## アクセスURL

### ローカルアクセス
- フロントエンド: http://localhost:3000
- バックエンド: http://localhost:8082
- API: http://localhost:8082/api
- H2コンソール: http://localhost:8082/h2-console

### リモートアクセス
- フロントエンド: http://[IPアドレス]:3000
- バックエンド: http://[IPアドレス]:8082
- API: http://[IPアドレス]:8082/api
- H2コンソール: http://[IPアドレス]:8082/h2-console

## 状態確認

### システム状態の確認
```cmd
check-status.bat
```

### 手動での確認
```cmd
# ポートの状態確認
netstat -an | findstr :3000
netstat -an | findstr :8082

# IPアドレスの確認
ipconfig

# ファイアウォールルールの確認
netsh advfirewall firewall show rule name="不動産管理システム*"
```

## トラブルシューティング

### よくある問題

#### 1. pingは通るがアクセスできない
- ファイアウォール設定を確認
- ポートが正しく開いているか確認
- アンチウイルスソフトの設定を確認

#### 2. ポートが開かない
- 管理者権限でスクリプトを実行
- 既存のルールを削除してから再設定
- Windows Defenderの設定を確認

#### 3. アプリケーションが起動しない
- 依存関係のインストールを確認
- ポートが他のアプリケーションで使用されていないか確認
- ログファイルを確認

### 解決方法

#### ファイアウォールの手動設定
```cmd
# TCP 3000番（フロントエンド）
netsh advfirewall firewall add rule name="フロントエンド" dir=in action=allow protocol=TCP localport=3000

# TCP 8082番（バックエンド）
netsh advfirewall firewall add rule name="バックエンド" dir=in action=allow protocol=TCP localport=8082
```

#### ポートの確認
```cmd
# ポートの使用状況確認
netstat -ano | findstr :3000
netstat -ano | findstr :8082

# プロセスの確認
tasklist /FI "PID eq [PID番号]"
```

## セキュリティの考慮事項

### 開発環境
- ローカルネットワーク内でのみアクセス許可
- 必要に応じてVPNの使用

### 本番環境
- 特定のIPアドレスのみアクセス許可
- HTTPSの使用を強く推奨
- 認証・認可の実装

## サポート

問題が発生した場合は、以下の手順で対処してください：

1. `check-status.bat` でシステム状態を確認
2. ログファイルを確認
3. ファイアウォール設定を再実行
4. アプリケーションを再起動

設定が完了すると、リモートPCから以下のURLでアクセスできるようになります：
- フロントエンド: http://[ホストPCのIP]:3000
- バックエンド: http://[ホストPCのIP]:8082
