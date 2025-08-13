@echo off
echo 不動産管理システムの状態を確認します...
echo.

echo ========================================
echo ネットワーク設定
echo ========================================
echo 現在のIPアドレス:
ipconfig | findstr "IPv4"
echo.

echo ========================================
echo ポートの状態
echo ========================================
echo フロントエンド（3000番）:
netstat -an | findstr ":3000"
echo.
echo バックエンド（8082番）:
netstat -an | findstr ":8082"
echo.

echo ========================================
echo ファイアウォールルール
echo ========================================
echo 設定されているルール:
netsh advfirewall firewall show rule name="不動産管理システム*"
echo.

echo ========================================
echo アクセスURL
echo ========================================
for /f "tokens=2 delims=:" %%i in ('ipconfig ^| findstr "IPv4"') do (
    set IP=%%i
    set IP=!IP: =!
    echo フロントエンド: http://!IP!:3000
    echo バックエンド: http://!IP!:8082
    echo API: http://!IP!:8082/api
    echo H2コンソール: http://!IP!:8082/h2-console
    goto :found_ip
)
:found_ip

echo.
echo 状態確認完了！
pause
