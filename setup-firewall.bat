@echo off
echo 不動産管理システムのファイアウォール設定を開始します...
echo.

echo 既存のルールを削除中...
netsh advfirewall firewall delete rule name="不動産管理システム フロントエンド" >nul 2>&1
netsh advfirewall firewall delete rule name="不動産管理システム バックエンド" >nul 2>&1
netsh advfirewall firewall delete rule name="不動産管理システム フロントエンド UDP" >nul 2>&1
netsh advfirewall firewall delete rule name="不動産管理システム バックエンド UDP" >nul 2>&1

echo 新しいファイアウォールルールを追加中...

echo フロントエンド用（TCP 3000番）
netsh advfirewall firewall add rule name="不動産管理システム フロントエンド" dir=in action=allow protocol=TCP localport=3000 remoteip=any

echo バックエンド用（TCP 8082番）
netsh advfirewall firewall add rule name="不動産管理システム バックエンド" dir=in action=allow protocol=TCP localport=8082 remoteip=any

echo フロントエンド用（UDP 3000番）
netsh advfirewall firewall add rule name="不動産管理システム フロントエンド UDP" dir=in action=allow protocol=UDP localport=3000 remoteip=any

echo バックエンド用（UDP 8082番）
netsh advfirewall firewall add rule name="不動産管理システム バックエンド UDP" dir=in action=allow protocol=UDP localport=8082 remoteip=any

echo.
echo ファイアウォール設定が完了しました！
echo.
echo 設定されたルール:
netsh advfirewall firewall show rule name="不動産管理システム*"
echo.
echo ポートの状態を確認中...
netstat -an | findstr ":3000"
netstat -an | findstr ":3000"
echo.
echo 設定完了！リモートPCからアクセスできるようになりました。
pause
