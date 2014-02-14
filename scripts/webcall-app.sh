#!/usr/bin/env sh

APP_NAME=webcall-app

exec java -jar /opt/alena/$APP_NAME/$APP_NAME.jar --config=/opt/alena/$APP_NAME/config.ini >> /var/log/alena/$APP_NAME.log
