#!/usr/bin/env sh

APP_NAME=media-controller
export PATH=/opt/alena/media-controller/agents:$PATH
export GST_DEBUG=0,python:0,gnl*:0
export GST_DEBUG_COLOR_MODE=off
export GST_PLUGIN_PATH=/opt/alena

exec python /opt/alena/media-controller/media_controller.py --config=/opt/alena/media-controller/config.ini >> /var/log/alena/$APP_NAME.log 2>&1
