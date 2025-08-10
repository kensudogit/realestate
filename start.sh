#!/bin/bash

echo "不動産管理システムを起動しています..."

echo
echo "バックエンドを起動中..."
cd backend
./gradlew bootRun &
BACKEND_PID=$!

echo
echo "フロントエンドを起動中..."
cd ../frontend
npm run dev &
FRONTEND_PID=$!

echo
echo "システムが起動しました！"
echo "バックエンド: http://localhost:8081"
echo "フロントエンド: http://localhost:3000"
echo "H2コンソール: http://localhost:8081/h2-console"
echo
echo "停止するには Ctrl+C を押してください"
echo

# プロセスの終了を待つ
trap "echo 'システムを停止しています...'; kill $BACKEND_PID $FRONTEND_PID; exit" INT
wait
