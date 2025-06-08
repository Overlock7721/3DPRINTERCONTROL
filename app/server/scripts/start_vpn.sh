#!/bin/bash
# Скрипт запуска WireGuard VPN на сервере

WG_CONFIG="/home/user/server/config/wireguard.conf"

if [ "$(id -u)" -ne 0 ]; then
  echo "Этот скрипт нужно запускать с правами root!"
  exit 1
fi

echo "Запуск WireGuard..."
wg-quick down $WG_CONFIG 2>/dev/null
wg-quick up $WG_CONFIG

echo "WireGuard VPN запущен."
