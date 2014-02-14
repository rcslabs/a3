#!/usr/bin/env sh

APP_NAME=client-handler

exec node /opt/alena/client-handler/$APP_NAME.js --config=/opt/alena/client-handler/config.ini >> /var/log/alena/$APP_NAME.log 2>&1
