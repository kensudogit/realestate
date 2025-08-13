@echo off
echo 不動産管理システム フロントエンドを起動します...
echo.

cd /d "%~dp0frontend"

echo 依存関係をインストール中...
call npm install

echo.
echo フロントエンドを起動中...
echo リモートアクセス用に設定されています
echo ローカル: http://localhost:3000
echo リモート: http://[IPアドレス]:3000
echo.

call npm run dev

pause
