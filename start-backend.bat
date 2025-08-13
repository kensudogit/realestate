@echo off
echo 不動産管理システム バックエンドを起動します...
echo.

cd /d "%~dp0backend"

echo 依存関係をインストール中...
call gradlew.bat clean

echo.
echo バックエンドを起動中...
echo リモートアクセス用に設定されています
echo ローカル: http://localhost:8082
echo リモート: http://[IPアドレス]:8082
echo API: http://[IPアドレス]:8082/api
echo H2コンソール: http://[IPアドレス]:8082/h2-console
echo.

call gradlew.bat bootRun

pause
