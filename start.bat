@echo off
echo 不動産管理システムを起動しています...

echo.
echo バックエンドを起動中...
cd backend
start "Backend" cmd /k "gradlew bootRun"

echo.
echo フロントエンドを起動中...
cd ../frontend
start "Frontend" cmd /k "npm run dev"

echo.
echo システムが起動しました！
echo バックエンド: http://localhost:8081
echo フロントエンド: http://localhost:3000
echo H2コンソール: http://localhost:8081/h2-console
echo.
pause
