#!/bin/bash
code=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 https://therminator.moma.rs 2>/dev/null || echo 000)
if [ "$code" = "503" ]; then
  /boot/dietpi/dietpi-services restart
fi
exit 0
