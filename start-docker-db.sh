#!/bin/bash

echo "========================================"
echo "啟動 Oracle 資料庫容器"
echo "========================================"
echo ""

echo "正在啟動 Oracle Database Free..."
docker-compose -f docker-compose-oracle-free.yml up -d

echo ""
echo "等待資料庫啟動中（約 1-2 分鐘）..."
sleep 5

echo ""
echo "查看容器狀態："
docker ps | grep order-currency-oracle

echo ""
echo "查看啟動日誌（按 Ctrl+C 退出）："
docker logs -f order-currency-oracle







